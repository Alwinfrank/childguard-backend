package com.childguard.service;

import com.childguard.model.AuditLog;
import com.childguard.repository.AuditLogRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void log(String action, String actorEmail, String details) {
        AuditLog log = new AuditLog();
        log.setAction(action);
        log.setActorEmail(actorEmail == null || actorEmail.isBlank() ? "UNKNOWN" : actorEmail);
        log.setDetails(details);
        auditLogRepository.save(log);
    }

    public List<AuditLog> getRecentLogs() {
        return auditLogRepository.findTop100ByOrderByCreatedAtDesc();
    }
}
