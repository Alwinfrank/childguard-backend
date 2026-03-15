package com.childguard.service;

import com.childguard.dto.AdminCreateUserRequest;
import com.childguard.dto.AdminUpdateUserRequest;
import com.childguard.dto.AdminUserResponse;
import com.childguard.model.User;
import com.childguard.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService {
    public static final String ROLE_USER = "USER";
    public static final String ROLE_POLICE = "POLICE";
    public static final String ROLE_ADMIN = "ADMIN";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User register(String email, String password, String role) {
        String normalizedRole = normalizeRole(role);
        if (normalizedRole == null) {
            throw new RuntimeException("Invalid role");
        }
        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(normalizedRole);

        return userRepository.save(user);
    }

    public User authenticate(String email, String rawPassword) {

        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return null;
        }

        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            return null;
        }
        if (!user.isActive()) {
            return null;
        }

        return user;
    }

    public User authenticateByRole(String email, String rawPassword, String requiredRole) {
        User user = authenticate(email, rawPassword);
        if (user == null) {
            return null;
        }
        if (!requiredRole.equalsIgnoreCase(user.getRole())) {
            return null;
        }
        return user;
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    public User findOrCreateGoogleUser(String email) {
        User existing = userRepository.findByEmail(email).orElse(null);
        if (existing != null) {
            return existing;
        }

        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
        user.setRole(ROLE_USER);
        return userRepository.save(user);
    }

    public String normalizeRole(String role) {
        if (role == null) {
            return null;
        }

        String normalized = role.trim().toUpperCase();
        if (ROLE_USER.equals(normalized) || ROLE_POLICE.equals(normalized) || ROLE_ADMIN.equals(normalized)) {
            return normalized;
        }
        return null;
    }

    public List<AdminUserResponse> getAllUsersForAdmin() {
        return userRepository.findAll().stream()
                .map(this::toAdminUserResponse)
                .collect(Collectors.toList());
    }

    public AdminUserResponse createUserByAdmin(AdminCreateUserRequest request) {
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new RuntimeException("Email is required.");
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new RuntimeException("Password is required.");
        }
        if (request.getRole() == null || request.getRole().isBlank()) {
            throw new RuntimeException("Role is required.");
        }

        User created = register(request.getEmail().trim(), request.getPassword(), request.getRole());
        return toAdminUserResponse(created);
    }

    public AdminUserResponse updateUserByAdmin(Long id, AdminUpdateUserRequest request, String currentAdminEmail) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found."));

        if (request.getRole() != null && !request.getRole().isBlank()) {
            String normalizedRole = normalizeRole(request.getRole());
            if (normalizedRole == null) {
                throw new RuntimeException("Role must be USER, POLICE, or ADMIN.");
            }
            if (ROLE_ADMIN.equalsIgnoreCase(user.getRole())
                    && !ROLE_ADMIN.equalsIgnoreCase(normalizedRole)
                    && currentAdminEmail != null
                    && currentAdminEmail.equalsIgnoreCase(user.getEmail())) {
                throw new RuntimeException("You cannot remove ADMIN role from your own account.");
            }
            user.setRole(normalizedRole);
        }

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        if (request.getActive() != null) {
            if (currentAdminEmail != null
                    && currentAdminEmail.equalsIgnoreCase(user.getEmail())
                    && !request.getActive()) {
                throw new RuntimeException("You cannot deactivate your own account.");
            }
            user.setActive(request.getActive());
        }

        User updated = userRepository.save(user);
        return toAdminUserResponse(updated);
    }

    public void deleteUserByAdmin(Long id, String currentAdminEmail) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found."));

        if (currentAdminEmail != null && currentAdminEmail.equalsIgnoreCase(user.getEmail())) {
            throw new RuntimeException("You cannot delete your own account.");
        }

        userRepository.delete(user);
    }

    private AdminUserResponse toAdminUserResponse(User user) {
        return new AdminUserResponse(user.getId(), user.getEmail(), user.getRole(), user.isActive());
    }
}
