package com.yumeinaruu.nseutask.config;

import com.yumeinaruu.nseutask.entity.User;
import com.yumeinaruu.nseutask.entity.enums.Role;
import com.yumeinaruu.nseutask.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@EnableConfigurationProperties(AdminProperties.class)
public class AdminInitializer implements ApplicationRunner {

    private final AdminProperties adminProperties;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminInitializer(
            AdminProperties adminProperties,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.adminProperties = adminProperties;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments arguments) {
        if (userRepository.existsByUsername(adminProperties.username())) {
            return;
        }
        log.info("Starting admin creation: username={}", adminProperties.username());
        User admin = new User();
        admin.setUsername(adminProperties.username());
        admin.setEmail(adminProperties.email());
        admin.setPassword(passwordEncoder.encode(adminProperties.password()));
        admin.setRole(Role.ADMIN);
        try {
            User savedAdmin = userRepository.saveAndFlush(admin);
            log.info("Completed admin creation: userId={}", savedAdmin.getId());
        } catch (DataIntegrityViolationException exception) {
            log.warn("Skipped admin creation: username={}, type={}",
                    adminProperties.username(), exception.getClass().getSimpleName());
        }
    }

}
