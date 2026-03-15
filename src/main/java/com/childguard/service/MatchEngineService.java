package com.childguard.service;

import com.childguard.dto.FaceBatchMatchRequest;
import com.childguard.dto.FaceCandidate;
import com.childguard.dto.FaceMatchResult;
import com.childguard.dto.MatchDecisionRequest;
import com.childguard.dto.PossibleMatchResponse;
import com.childguard.model.FoundChild;
import com.childguard.model.MatchReview;
import com.childguard.model.MissingChild;
import com.childguard.repository.FoundChildRepository;
import com.childguard.repository.MatchReviewRepository;
import com.childguard.repository.MissingChildRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class MatchEngineService {
    private static final Logger log = LoggerFactory.getLogger(MatchEngineService.class);
    private static final double EARTH_RADIUS_KM = 6371.0;
    private static final int DEFAULT_MAX_AGE_DIFF = 3;
    private static final double DEFAULT_MAX_DISTANCE_KM = 5.0;

    private final MissingChildRepository missingChildRepository;
    private final FoundChildRepository foundChildRepository;
    private final MatchReviewRepository matchReviewRepository;
    private final AuditLogService auditLogService;
    private final RestTemplate restTemplate;
    private final String faceEngineUrl;
    private final boolean faceEngineRequired;

    public MatchEngineService(
            MissingChildRepository missingChildRepository,
            FoundChildRepository foundChildRepository,
            MatchReviewRepository matchReviewRepository,
            AuditLogService auditLogService,
            RestTemplate restTemplate,
            @Value("${face.engine.url:http://localhost:5000/match/batch}") String faceEngineUrl,
            @Value("${face.engine.required:false}") boolean faceEngineRequired
    ) {
        this.missingChildRepository = missingChildRepository;
        this.foundChildRepository = foundChildRepository;
        this.matchReviewRepository = matchReviewRepository;
        this.auditLogService = auditLogService;
        this.restTemplate = restTemplate;
        this.faceEngineUrl = faceEngineUrl;
        this.faceEngineRequired = faceEngineRequired;
    }

    public List<PossibleMatchResponse> findPossibleMatches() {
        List<MissingChild> missingChildren = missingChildRepository.findAll();
        List<FoundChild> foundChildren = foundChildRepository.findAll();

        if (missingChildren.isEmpty() || foundChildren.isEmpty()) {
            return List.of();
        }

        List<PossibleMatchResponse> matches;
        try {
            matches = findUsingFaceEngine(missingChildren, foundChildren);
        } catch (Exception ex) {
            if (faceEngineRequired) {
                throw new RuntimeException("Face CNN engine is unavailable. Cannot compute matches.", ex);
            }
            log.warn("Face engine unavailable, using rule-based fallback: {}", ex.getMessage());
            matches = findUsingRuleBasedFallback(missingChildren, foundChildren);
        }

        applyStoredDecisions(matches);
        return matches;
    }

    public Map<String, Object> getEngineStatus() {
        boolean up = isFaceEngineUp();
        String mode = up ? "CNN" : "RULE_BASED_FALLBACK";

        Map<String, Object> status = new HashMap<>();
        status.put("mode", mode);
        status.put("faceEngineUp", up);
        status.put("faceEngineUrl", faceEngineUrl);
        status.put("faceEngineRequired", faceEngineRequired);
        return status;
    }

    public Map<String, Object> applyDecision(MatchDecisionRequest request) {
        if (request.getMissingChildId() == null || request.getFoundChildId() == null || request.getDecision() == null) {
            throw new RuntimeException("missingChildId, foundChildId, and decision are required.");
        }

        String normalizedDecision = request.getDecision().trim().toUpperCase(Locale.ROOT);
        if (!Set.of("APPROVED", "REJECTED", "PENDING_REVIEW").contains(normalizedDecision)) {
            throw new RuntimeException("decision must be APPROVED, REJECTED, or PENDING_REVIEW.");
        }

        MissingChild missingChild = missingChildRepository.findById(request.getMissingChildId())
                .orElseThrow(() -> new RuntimeException("Missing child not found."));

        foundChildRepository.findById(request.getFoundChildId())
                .orElseThrow(() -> new RuntimeException("Found child not found."));

        MatchReview review = matchReviewRepository
                .findByMissingChildIdAndFoundChildId(request.getMissingChildId(), request.getFoundChildId())
                .orElseGet(MatchReview::new);

        review.setMissingChildId(request.getMissingChildId());
        review.setFoundChildId(request.getFoundChildId());
        review.setDecision(normalizedDecision);
        matchReviewRepository.save(review);

        if ("APPROVED".equals(normalizedDecision)) {
            missingChild.setStatus("MATCHED");
        } else if ("PENDING_REVIEW".equals(normalizedDecision)) {
            missingChild.setStatus("PENDING_VERIFICATION");
        } else {
            missingChild.setStatus("MISSING");
        }
        missingChildRepository.save(missingChild);

        Map<String, Object> response = new HashMap<>();
        response.put("missingChildId", request.getMissingChildId());
        response.put("foundChildId", request.getFoundChildId());
        response.put("decision", normalizedDecision);
        response.put("missingStatus", missingChild.getStatus());

        auditLogService.log(
                "MATCH_DECISION",
                "SYSTEM_OR_AUTHENTICATED_USER",
                "missingChildId=" + request.getMissingChildId()
                        + ", foundChildId=" + request.getFoundChildId()
                        + ", decision=" + normalizedDecision
                        + ", missingStatus=" + missingChild.getStatus()
        );

        return response;
    }

    private List<PossibleMatchResponse> findUsingFaceEngine(
            List<MissingChild> missingChildren,
            List<FoundChild> foundChildren
    ) {
        FaceBatchMatchRequest request = new FaceBatchMatchRequest();
        request.setMaxAgeDiff(DEFAULT_MAX_AGE_DIFF);
        request.setMaxDistanceKm(DEFAULT_MAX_DISTANCE_KM);
        request.setMissingChildren(missingChildren.stream()
                .map(m -> new FaceCandidate(
                        m.getId(),
                        m.getAge(),
                        m.getLatitude(),
                        m.getLongitude(),
                        m.getPhotoUrl()
                ))
                .collect(Collectors.toList()));
        request.setFoundChildren(foundChildren.stream()
                .map(f -> new FaceCandidate(
                        f.getId(),
                        f.getApproxAge(),
                        f.getLatitude(),
                        f.getLongitude(),
                        f.getPhotoUrl()
                ))
                .collect(Collectors.toList()));

        ResponseEntity<FaceMatchResult[]> response = restTemplate.postForEntity(
                faceEngineUrl,
                request,
                FaceMatchResult[].class
        );

        FaceMatchResult[] body = response.getBody();
        if (body == null || body.length == 0) {
            return List.of();
        }

        Map<Long, MissingChild> missingById = missingChildren.stream()
                .collect(Collectors.toMap(MissingChild::getId, c -> c, (a, b) -> a, HashMap::new));
        Map<Long, FoundChild> foundById = foundChildren.stream()
                .collect(Collectors.toMap(FoundChild::getId, c -> c, (a, b) -> a, HashMap::new));

        List<PossibleMatchResponse> matches = new ArrayList<>();
        for (FaceMatchResult result : body) {
            MissingChild missing = missingById.get(result.getMissingId());
            FoundChild found = foundById.get(result.getFoundId());
            if (missing == null || found == null) {
                continue;
            }

            matches.add(new PossibleMatchResponse(
                    missing,
                    found,
                    result.getSimilarity(),
                    result.getAgeDifference(),
                    result.getDistanceKm()
            ));
        }

        return matches.stream()
                .sorted(Comparator.comparing(PossibleMatchResponse::getSimilarity).reversed())
                .collect(Collectors.toList());
    }

    private List<PossibleMatchResponse> findUsingRuleBasedFallback(
            List<MissingChild> missingChildren,
            List<FoundChild> foundChildren
    ) {
        List<PossibleMatchResponse> matches = new ArrayList<>();

        for (MissingChild missing : missingChildren) {
            if (missing.getLatitude() == null || missing.getLongitude() == null) {
                continue;
            }

            for (FoundChild found : foundChildren) {
                if (found.getLatitude() == null || found.getLongitude() == null || found.getApproxAge() == null) {
                    continue;
                }

                int ageDiff = Math.abs(missing.getAge() - found.getApproxAge());
                if (ageDiff >= DEFAULT_MAX_AGE_DIFF) {
                    continue;
                }

                double distance = haversine(
                        missing.getLatitude(),
                        missing.getLongitude(),
                        found.getLatitude(),
                        found.getLongitude()
                );

                if (distance >= DEFAULT_MAX_DISTANCE_KM) {
                    continue;
                }

                // Fallback confidence when CNN service is unavailable.
                double similarity = Math.max(0.0, 1.0 - ((double) ageDiff / DEFAULT_MAX_AGE_DIFF));
                matches.add(new PossibleMatchResponse(missing, found, similarity, ageDiff, distance));
            }
        }

        return matches.stream()
                .sorted(Comparator.comparing(PossibleMatchResponse::getSimilarity).reversed())
                .collect(Collectors.toList());
    }

    private double haversine(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }

    private boolean isFaceEngineUp() {
        try {
            @SuppressWarnings("unchecked")
            ResponseEntity<Map> response = restTemplate.getForEntity(getFaceEngineHealthUrl(), Map.class);
            Object status = response.getBody() != null ? response.getBody().get("status") : null;
            return response.getStatusCode().is2xxSuccessful()
                    && status != null
                    && "ok".equalsIgnoreCase(status.toString().toLowerCase(Locale.ROOT));
        } catch (Exception ex) {
            return false;
        }
    }

    private String getFaceEngineHealthUrl() {
        if (faceEngineUrl.endsWith("/match/batch")) {
            return faceEngineUrl.substring(0, faceEngineUrl.length() - "/match/batch".length()) + "/health";
        }
        return faceEngineUrl;
    }

    private void applyStoredDecisions(List<PossibleMatchResponse> matches) {
        if (matches.isEmpty()) {
            return;
        }

        List<Long> missingIds = matches.stream()
                .map(m -> m.getMissingChild().getId())
                .distinct()
                .collect(Collectors.toList());

        Map<String, String> decisionByPair = matchReviewRepository.findAllByMissingChildIdIn(missingIds).stream()
                .collect(Collectors.toMap(
                        r -> pairKey(r.getMissingChildId(), r.getFoundChildId()),
                        MatchReview::getDecision,
                        (a, b) -> b,
                        HashMap::new
                ));

        for (PossibleMatchResponse match : matches) {
            String key = pairKey(match.getMissingChild().getId(), match.getFoundChild().getId());
            String decision = decisionByPair.getOrDefault(key, "PENDING_REVIEW");
            match.setDecision(decision);
        }
    }

    private String pairKey(Long missingId, Long foundId) {
        return missingId + ":" + foundId;
    }
}
