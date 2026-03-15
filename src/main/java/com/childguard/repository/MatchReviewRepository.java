package com.childguard.repository;

import com.childguard.model.MatchReview;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MatchReviewRepository extends JpaRepository<MatchReview, Long> {
    Optional<MatchReview> findByMissingChildIdAndFoundChildId(Long missingChildId, Long foundChildId);

    List<MatchReview> findAllByMissingChildIdIn(List<Long> missingChildIds);
}
