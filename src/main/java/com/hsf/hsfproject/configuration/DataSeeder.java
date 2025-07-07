package com.hsf.hsfproject.configuration;

import com.hsf.hsfproject.model.Cart;
import com.hsf.hsfproject.model.Role;
import com.hsf.hsfproject.model.User;
import com.hsf.hsfproject.repository.CartRepository;
import com.hsf.hsfproject.repository.RoleRepository;
import com.hsf.hsfproject.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {
    
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) throws Exception {
        seedRoles();
        seedTestUsers();
    }
    
    private void seedRoles() {
        createRoleIfNotExists("ROLE_USER", "Regular user with basic access");
        createRoleIfNotExists("ROLE_MANAGER", "Manager with order management access");
        createRoleIfNotExists("ROLE_ADMIN", "Administrator with full system access");
        
        log.info("Roles seeded successfully");
    }
    
    private void createRoleIfNotExists(String roleName, String description) {
        if (roleRepository.findByName(roleName).isEmpty()) {
            Role role = Role.builder()
                    .name(roleName)
                    .description(description)
                    .build();
            roleRepository.save(role);
            log.info("Created role: {}", roleName);
        }
    }
    
    private void seedTestUsers() {
        // Create test user if not exists
        if (userRepository.findByUsername("user").isEmpty()) {
            Role userRole = roleRepository.findByName("ROLE_USER")
                    .orElseThrow(() -> new RuntimeException("Role USER not found"));
            
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
        }
        
        // Create test admin if not exists
        if (userRepository.findByUsername("admin").isEmpty()) {
            Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                    .orElseThrow(() -> new RuntimeException("Role ADMIN not found"));
            
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
                    .orElseThrow(() -> new RuntimeException("Role ADMIN not found"));
            
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
                    .orElseThrow(() -> new RuntimeException("Role MANAGER not found"));
            
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