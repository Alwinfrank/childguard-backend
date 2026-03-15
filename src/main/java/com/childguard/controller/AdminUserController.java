package com.childguard.controller;

import com.childguard.dto.AdminCreateUserRequest;
import com.childguard.dto.AdminUpdateUserRequest;
import com.childguard.dto.AdminUserResponse;
import com.childguard.service.AuditLogService;
import com.childguard.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@CrossOrigin(origins = "*")
public class AdminUserController {

    private final UserService userService;
    private final AuditLogService auditLogService;

    public AdminUserController(UserService userService, AuditLogService auditLogService) {
        this.userService = userService;
        this.auditLogService = auditLogService;
    }

    @GetMapping
    public List<AdminUserResponse> getAllUsers() {
        return userService.getAllUsersForAdmin();
    }

    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody AdminCreateUserRequest request, Authentication authentication) {
        try {
            AdminUserResponse created = userService.createUserByAdmin(request);
            String currentAdminEmail = authentication != null ? authentication.getName() : null;
            auditLogService.log(
                    "ADMIN_CREATE_USER",
                    currentAdminEmail,
                    "Created user id=" + created.getId() + ", email=" + created.getEmail() + ", role=" + created.getRole()
            );
            return ResponseEntity.ok(created);
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(
            @PathVariable Long id,
            @RequestBody AdminUpdateUserRequest request,
            Authentication authentication
    ) {
        try {
            String currentAdminEmail = authentication != null ? authentication.getName() : null;
            AdminUserResponse updated = userService.updateUserByAdmin(id, request, currentAdminEmail);
            auditLogService.log(
                    "ADMIN_UPDATE_USER",
                    currentAdminEmail,
                    "Updated user id=" + updated.getId() + ", email=" + updated.getEmail() + ", role=" + updated.getRole() + ", active=" + updated.isActive()
            );
            return ResponseEntity.ok(updated);
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(
            @PathVariable Long id,
            Authentication authentication
    ) {
        try {
            String currentAdminEmail = authentication != null ? authentication.getName() : null;
            userService.deleteUserByAdmin(id, currentAdminEmail);
            auditLogService.log("ADMIN_DELETE_USER", currentAdminEmail, "Deleted user id=" + id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }
}
