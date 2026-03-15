package com.childguard.dto;

import java.util.List;

public class FaceBatchMatchRequest {
    private List<FaceCandidate> missingChildren;
    private List<FaceCandidate> foundChildren;
    private Integer maxAgeDiff = 3;
    private Double maxDistanceKm = 5.0;

    public List<FaceCandidate> getMissingChildren() {
        return missingChildren;
    }

    public void setMissingChildren(List<FaceCandidate> missingChildren) {
        this.missingChildren = missingChildren;
    }

    public List<FaceCandidate> getFoundChildren() {
        return foundChildren;
    }

    public void setFoundChildren(List<FaceCandidate> foundChildren) {
        this.foundChildren = foundChildren;
    }

    public Integer getMaxAgeDiff() {
        return maxAgeDiff;
    }

    public void setMaxAgeDiff(Integer maxAgeDiff) {
        this.maxAgeDiff = maxAgeDiff;
    }

    public Double getMaxDistanceKm() {
        return maxDistanceKm;
    }

    public void setMaxDistanceKm(Double maxDistanceKm) {
        this.maxDistanceKm = maxDistanceKm;
    }
}
