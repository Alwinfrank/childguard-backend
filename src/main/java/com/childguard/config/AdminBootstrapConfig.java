package com.childguard.config;

import com.childguard.model.User;
import com.childguard.repository.UserRepository;
import com.childguard.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AdminBootstrapConfig {
    private static final Logger log = LoggerFactory.getLogger(AdminBootstrapConfig.class);

    @Bean
    public CommandLineRunner bootstrapAdminUser(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            @Value("${bootstrap.admin.enabled:true}") boolean enabled,
            @Value("${bootstrap.admin.email:admin@childguard.com}") String email,
            @Value("${bootstrap.admin.password:Admin@123}") String password
    ) {
        return args -> {
            if (!enabled) {
                return;
            }

            User user = userRepository.findByEmail(email).orElse(null);
            if (user == null) {
                User admin = new User();
                admin.setEmail(email);
                admin.setPassword(passwordEncoder.encode(password));
                admin.setRole(UserService.ROLE_ADMIN);
                admin.setActive(true);
                userRepository.save(admin);
                log.info("Bootstrap admin user created: {}", email);
                return;
            }

            user.setRole(UserService.ROLE_ADMIN);
            user.setActive(true);
            user.setPassword(passwordEncoder.encode(password));
            userRepository.save(user);
            log.info("Bootstrap admin user updated: {}", email);
        };
    }
}
