package com.childguard.dto;

public class DashboardStatsResponse {

    private long totalMissing;
    private long foundChildren;
    private long matchedCases;
    private long pendingVerification;

    public DashboardStatsResponse(
            long totalMissing,
            long foundChildren,
            long matchedCases,
            long pendingVerification) {
        this.totalMissing = totalMissing;
        this.foundChildren = foundChildren;
        this.matchedCases = matchedCases;
        this.pendingVerification = pendingVerification;
    }

    public long getTotalMissing() {
        return totalMissing;
    }

    public long getFoundChildren() {
        return foundChildren;
    }

    public long getMatchedCases() {
        return matchedCases;
    }

    public long getPendingVerification() {
        return pendingVerification;
    }
}
