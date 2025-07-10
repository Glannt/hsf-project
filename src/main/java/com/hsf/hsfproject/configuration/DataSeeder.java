package com.hsf.hsfproject.configuration;

import com.hsf.hsfproject.model.Cart;
import com.hsf.hsfproject.model.Role;
import com.hsf.hsfproject.model.User;
import com.hsf.hsfproject.repository.CartRepository;
import com.hsf.hsfproject.repository.RoleRepository;
import com.hsf.hsfproject.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {
    
    @Value("${app.data-seeder.enabled:false}")
    private boolean dataSeederEnabled;
    
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) throws Exception {
        if (dataSeederEnabled) {
        seedRoles();
        seedTestUsers();
            log.info("DataSeeder enabled - data seeded successfully");
        } else {
            log.info("DataSeeder disabled - set app.data-seeder.enabled=true to enable");
        }
    }
    
    private void seedRoles() {
        createRoleIfNotExists("ROLE_USER", "Regular user with basic access");
        createRoleIfNotExists("ROLE_MANAGER", "Manager with order management access");
        createRoleIfNotExists("ROLE_ADMIN", "Administrator with full system access");
        
        log.info("Roles seeded successfully");
    }
    
    private Role createRoleIfNotExists(String roleName, String description) {
        return roleRepository.findByName(roleName)
                .orElseGet(() -> {
            Role role = Role.builder()
                    .name(roleName)
                    .description(description)
                    .build();
                    Role savedRole = roleRepository.save(role);
            log.info("Created role: {}", roleName);
                    return savedRole;
                });
    }
    
    private void seedTestUsers() {
        // Always create/update test user with current password
            Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseGet(() -> {
                    log.warn("Role USER not found, creating it now");
                    return createRoleIfNotExists("ROLE_USER", "Regular user with basic access");
                });
            
        User existingUser = userRepository.findByUsername("user").orElse(null);
        if (existingUser == null) {
            // Create new user
            User user = User.builder()
                    .username("user")
                    .password(passwordEncoder.encode("password"))
                    .email("user@test.com")
                    .phoneNumber("0123456789")
                    .role(userRole)
                    .build();
            
            // Create cart for user
            Cart cart = Cart.builder().build();
            cartRepository.save(cart);
            user.setCart(cart);
            
            userRepository.save(user);
            log.info("Created test user: user/password");
        } else {
            // Update existing user's password
            existingUser.setPassword(passwordEncoder.encode("password"));
            userRepository.save(existingUser);
            log.info("Updated test user password: user/password");
        }
        
        // Create test admin if not exists
        if (userRepository.findByUsername("admin").isEmpty()) {
            Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                    .orElseGet(() -> {
                        log.warn("Role ADMIN not found, creating it now");
                        return createRoleIfNotExists("ROLE_ADMIN", "Administrator with full system access");
                    });
            
            User admin = User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin"))
                    .email("admin@test.com")
                    .phoneNumber("0987654321")
                    .role(adminRole)
                    .build();
            
            // Create cart for admin
            Cart cart = Cart.builder().build();
            cartRepository.save(cart);
            admin.setCart(cart);
            
            userRepository.save(admin);
            log.info("Created test admin: admin/admin");
        }
        
        // Create another admin with email login
        if (userRepository.findByUsername("admin@hsf.com").isEmpty()) {
            Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                    .orElseGet(() -> {
                        log.warn("Role ADMIN not found, creating it now");
                        return createRoleIfNotExists("ROLE_ADMIN", "Administrator with full system access");
                    });
            
            User adminEmail = User.builder()
                    .username("admin@hsf.com")
                    .password(passwordEncoder.encode("123456"))
                    .email("admin@hsf.com")
                    .phoneNumber("0987654322")
                    .role(adminRole)
                    .build();
            
            // Create cart for admin
            Cart cart = Cart.builder().build();
            cartRepository.save(cart);
            adminEmail.setCart(cart);
            
            userRepository.save(adminEmail);
            log.info("Created test admin: admin@hsf.com/123456");
        }
        
        // Create test manager if not exists
        if (userRepository.findByUsername("manager").isEmpty()) {
            Role managerRole = roleRepository.findByName("ROLE_MANAGER")
                    .orElseGet(() -> {
                        log.warn("Role MANAGER not found, creating it now");
                        return createRoleIfNotExists("ROLE_MANAGER", "Manager with order management access");
                    });
            
            User manager = User.builder()
                    .username("manager")
                    .password(passwordEncoder.encode("manager"))
                    .email("manager@test.com")
                    .phoneNumber("0123456788")
                    .role(managerRole)
                    .build();
            
            // Create cart for manager
            Cart cart = Cart.builder().build();
            cartRepository.save(cart);
            manager.setCart(cart);
            
            userRepository.save(manager);
            log.info("Created test manager: manager/manager");
        }
    }
} 