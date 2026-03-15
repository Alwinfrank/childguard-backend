package com.childguard.controller;

import com.childguard.dto.GoogleLoginRequest;
import com.childguard.dto.LoginRequest;
import com.childguard.dto.LoginResponse;
import com.childguard.dto.RegisterRequest;
import com.childguard.model.User;
import com.childguard.security.JwtUtil;
import com.childguard.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final RestTemplate restTemplate;

    @Value("${google.client.id:}")
    private String googleClientId;

    public AuthController(UserService userService, JwtUtil jwtUtil, RestTemplate restTemplate) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        this.restTemplate = restTemplate;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        if (request.getEmail() == null || request.getPassword() == null || request.getRole() == null) {
            return ResponseEntity.badRequest().body("Email, password, and role are required.");
        }

        String normalizedRole = userService.normalizeRole(request.getRole());
        if (normalizedRole == null) {
            return ResponseEntity.badRequest().body("Role must be USER, POLICE, or ADMIN.");
        }

        try {
            User user = userService.register(
                    request.getEmail().trim(),
                    request.getPassword(),
                    normalizedRole
            );

            return ResponseEntity.ok(Map.of(
                    "id", user.getId(),
                    "email", user.getEmail(),
                    "role", user.getRole()
            ));
        } catch (Exception ex) {
            return ResponseEntity.status(409).body("User already exists or invalid request.");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        return loginWithRole(request, null);
    }

    @PostMapping("/login/user")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest request) {
        return loginWithRole(request, UserService.ROLE_USER);
    }

    @PostMapping("/login/police")
    public ResponseEntity<?> loginPolice(@RequestBody LoginRequest request) {
        return loginWithRole(request, UserService.ROLE_POLICE);
    }

    @PostMapping("/login/admin")
    public ResponseEntity<?> loginAdmin(@RequestBody LoginRequest request) {
        return loginWithRole(request, UserService.ROLE_ADMIN);
    }

    @PostMapping("/google")
    public ResponseEntity<?> googleLogin(@RequestBody GoogleLoginRequest request) {
        if (request.getIdToken() == null || request.getIdToken().isBlank()) {
            return ResponseEntity.badRequest().body("idToken is required.");
        }
        if (googleClientId == null || googleClientId.isBlank()) {
            return ResponseEntity.status(500).body("Google login is not configured on backend.");
        }

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> payload = restTemplate.getForObject(
                    "https://oauth2.googleapis.com/tokeninfo?id_token={idToken}",
                    Map.class,
                    request.getIdToken()
            );

            if (payload == null) {
                return ResponseEntity.status(401).body("Invalid Google token.");
            }

            String aud = payload.get("aud") == null ? null : payload.get("aud").toString();
            String email = payload.get("email") == null ? null : payload.get("email").toString();
            String emailVerified = payload.get("email_verified") == null ? "false" : payload.get("email_verified").toString();

            if (!googleClientId.equals(aud)) {
                return ResponseEntity.status(401).body("Google token audience mismatch.");
            }
            if (email == null || email.isBlank()) {
                return ResponseEntity.status(401).body("Google account email missing.");
            }
            if (!"true".equalsIgnoreCase(emailVerified)) {
                return ResponseEntity.status(401).body("Google email is not verified.");
            }

            User existing = userService.findByEmail(email);
            if (existing != null && !UserService.ROLE_USER.equals(existing.getRole())) {
                return ResponseEntity.status(403).body("Google login is allowed for USER role only.");
            }

            User user = userService.findOrCreateGoogleUser(email);
            String token = jwtUtil.generateToken(user.getEmail(), user.getRole());
            return ResponseEntity.ok(new LoginResponse(token, user.getRole()));
        } catch (Exception ex) {
            return ResponseEntity.status(401).body("Google token validation failed.");
        }
    }

    private ResponseEntity<?> loginWithRole(LoginRequest request, String expectedRole) {
        if (request.getEmail() == null || request.getPassword() == null) {
            return ResponseEntity.badRequest().body("Email and password are required.");
        }

        User user = expectedRole == null
                ? userService.authenticate(request.getEmail(), request.getPassword())
                : userService.authenticateByRole(request.getEmail(), request.getPassword(), expectedRole);

        if (user == null) {
            if (expectedRole != null) {
                return ResponseEntity.status(401).body("Invalid credentials or role mismatch.");
            }
            return ResponseEntity.status(401).body("Invalid email or password");
        }

        String token = jwtUtil.generateToken(
                user.getEmail(),
                user.getRole()
        );

        return ResponseEntity.ok(new LoginResponse(token, user.getRole()));
    }
}
