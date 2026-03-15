package com.childguard.controller;

import com.childguard.dto.MatchDecisionRequest;
import com.childguard.dto.PossibleMatchResponse;
import com.childguard.service.MatchEngineService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/matches")
@CrossOrigin(origins = "*")
public class MatchController {
    private final MatchEngineService matchEngineService;

    public MatchController(MatchEngineService matchEngineService) {
        this.matchEngineService = matchEngineService;
    }

    @GetMapping
    public List<PossibleMatchResponse> getMatches() {
        return matchEngineService.findPossibleMatches();
    }

    @GetMapping("/status")
    public Map<String, Object> getMatchEngineStatus() {
        return matchEngineService.getEngineStatus();
    }

    @PostMapping("/decision")
    public ResponseEntity<?> submitDecision(@RequestBody MatchDecisionRequest request) {
        try {
            return ResponseEntity.ok(matchEngineService.applyDecision(request));
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }
}
