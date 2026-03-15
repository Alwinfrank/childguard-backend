package com.childguard.controller;

import com.childguard.model.AuditLog;
import com.childguard.service.AuditLogService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/admin/audit")
@CrossOrigin(origins = "*")
public class AdminAuditController {

    private final AuditLogService auditLogService;

    public AdminAuditController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @GetMapping
    public List<AuditLog> getRecentAuditLogs() {
        return auditLogService.getRecentLogs();
    }

    @GetMapping("/csv")
    public ResponseEntity<byte[]> downloadAuditLogsCsv() {
        List<AuditLog> logs = auditLogService.getRecentLogs();

        StringBuilder csv = new StringBuilder();
        csv.append("id,createdAt,action,actorEmail,details\n");

        for (AuditLog log : logs) {
            csv.append(log.getId()).append(",");
            csv.append(escapeCsv(log.getCreatedAt() != null ? log.getCreatedAt().toString() : "")).append(",");
            csv.append(escapeCsv(log.getAction())).append(",");
            csv.append(escapeCsv(log.getActorEmail())).append(",");
            csv.append(escapeCsv(log.getDetails())).append("\n");
        }

        String filename = "audit-logs-" + LocalDateTime.now().toLocalDate() + ".csv";
        byte[] content = csv.toString().getBytes(StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(content);
    }

    private String escapeCsv(String value) {
        String safe = value == null ? "" : value;
        safe = safe.replace("\"", "\"\"");
        return "\"" + safe + "\"";
    }
}
