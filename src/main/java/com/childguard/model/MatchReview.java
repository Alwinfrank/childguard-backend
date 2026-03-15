package com.childguard.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "match_review",
        uniqueConstraints = @UniqueConstraint(columnNames = {"missingChildId", "foundChildId"})
)
public class MatchReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long missingChildId;
    private Long foundChildId;
    private String decision;
    private LocalDateTime reviewedAt = LocalDateTime.now();

    public Long getId() {
        return id;
    }

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

    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(LocalDateTime reviewedAt) {
        this.reviewedAt = reviewedAt;
    }
}
