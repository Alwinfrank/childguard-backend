package com.childguard.dto;

public class FaceMatchResult {
    private Long missingId;
    private Long foundId;
    private Double similarity;
    private Integer ageDifference;
    private Double distanceKm;

    public Long getMissingId() {
        return missingId;
    }

    public void setMissingId(Long missingId) {
        this.missingId = missingId;
    }

    public Long getFoundId() {
        return foundId;
    }

    public void setFoundId(Long foundId) {
        this.foundId = foundId;
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
}
