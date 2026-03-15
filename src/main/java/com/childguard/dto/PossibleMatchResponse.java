package com.childguard.dto;

import com.childguard.model.FoundChild;
import com.childguard.model.MissingChild;

public class PossibleMatchResponse {
    private MissingChild missingChild;
    private FoundChild foundChild;
    private Double similarity;
    private Integer ageDifference;
    private Double distanceKm;
    private String decision = "PENDING_REVIEW";

    public PossibleMatchResponse() {
    }

    public PossibleMatchResponse(
            MissingChild missingChild,
            FoundChild foundChild,
            Double similarity,
            Integer ageDifference,
            Double distanceKm
    ) {
        this.missingChild = missingChild;
        this.foundChild = foundChild;
        this.similarity = similarity;
        this.ageDifference = ageDifference;
        this.distanceKm = distanceKm;
    }

    public MissingChild getMissingChild() {
        return missingChild;
    }

    public void setMissingChild(MissingChild missingChild) {
        this.missingChild = missingChild;
    }

    public FoundChild getFoundChild() {
        return foundChild;
    }

    public void setFoundChild(FoundChild foundChild) {
        this.foundChild = foundChild;
    }

    public Double getSimilarity() {
        return similarity;
    }

    public void setSimilarity(Double similarity) {
        this.similarity = similarity;
    }

    public Integer getAgeDifference() {
        return ageDifference;
    }

    public void setAgeDifference(Integer ageDifference) {
        this.ageDifference = ageDifference;
    }

    public Double getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(Double distanceKm) {
        this.distanceKm = distanceKm;
    }

    public String getDecision() {
        return decision;
    }

    public void setDecision(String decision) {
        this.decision = decision;
    }
}
