package com.childguard.service;

import com.childguard.repository.MissingChildRepository;
import com.childguard.repository.FoundChildRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class DashboardService {

    private final MissingChildRepository missingRepo;
    private final FoundChildRepository foundRepo;

    public DashboardService(MissingChildRepository missingRepo,
                            FoundChildRepository foundRepo) {
        this.missingRepo = missingRepo;
        this.foundRepo = foundRepo;
    }

    public Map<String, Long> getStats() {

        Map<String, Long> stats = new HashMap<>();

        stats.put("totalMissing", missingRepo.count());
        stats.put("foundChildren", foundRepo.count());
        stats.put("matchedCases", missingRepo.countByStatus("MATCHED"));
        stats.put("pendingVerification", missingRepo.countByStatus("PENDING_VERIFICATION"));

        return stats;
    }
}
