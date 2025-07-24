package com.hsf.hsfproject.config;

import com.hsf.hsfproject.model.Role;
import com.hsf.hsfproject.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) {
        initRoles();
    }

    private void initRoles() {
        // Kiểm tra và tạo role nếu chưa tồn tại
        if (roleRepository.findByName("ROLE_USER") == null) {
            Role userRole = Role.builder()
                    .name("ROLE_USER")
                    .description("Regular user with standard privileges")
                    .build();
            roleRepository.save(userRole);
            log.info("Created ROLE_USER");
        }

        if (roleRepository.findByName("ROLE_ADMIN") == null) {
            Role adminRole = Role.builder()
                    .name("ROLE_ADMIN")
                    .description("Administrator with full privileges")
                    .build();
            roleRepository.save(adminRole);
            log.info("Created ROLE_ADMIN");
        }
    }
}
