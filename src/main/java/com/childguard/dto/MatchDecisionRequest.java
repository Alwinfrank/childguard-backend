package com.childguard.dto;

public class MatchDecisionRequest {
    private Long missingChildId;
    private Long foundChildId;
    private String decision;

    public Long getMissingChildId() {
        return missingChildId;
    }

    public void setMissingChildId(Long missingChildId) {
        this.missingChildId = missingChildId;
    }

    public Long getFoundChildId() {
        return foundChildId;
    }

    public void setFoundChildId(Long foundChildId) {
        this.foundChildId = foundChildId;
    }

    public String getDecision() {
        return decision;
    }

    public void setDecision(String decision) {
        this.decision = decision;
    }
}
