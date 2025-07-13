package com.hsf.hsfproject.service.user;

import com.hsf.hsfproject.dtos.CreateUserDTO;
import com.hsf.hsfproject.dtos.request.LoginRequest;
import com.hsf.hsfproject.dtos.response.LoginResponse;
import com.hsf.hsfproject.model.Role;
import com.hsf.hsfproject.model.User;
import com.hsf.hsfproject.repository.RoleRepository;
import com.hsf.hsfproject.repository.UserRepository;
import com.hsf.hsfproject.service.cart.ICartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

@Service
@Slf4j(topic = "UserService")
@RequiredArgsConstructor
public class UserService implements IUserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ICartService cartService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    @Override
    public User createUser(CreateUserDTO userDTO) {

        User user = userRepository.findByUsername(userDTO.getUsername()).orElse(null);
        if (user != null) {
            log.info("User with username {} already exists" + userDTO.getUsername());
            throw new IllegalArgumentException("User with username " + userDTO.getUsername() + " already exists");
        }
        //build a new user from the DTO

        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Role USER not found"));

        User newUser = User.builder()
                .username(userDTO.getUsername())
                .password(passwordEncoder.encode(userDTO.getPassword()))
                .email(userDTO.getEmail())
                .phoneNumber(userDTO.getPhoneNumber())
                .role(userRole)
                .build();

        // Set the cart for the new user
        newUser.setCart(cartService.createCart(newUser));

        // Save the new user to the repository
        userRepository.save(newUser);

        // Assign the default role to the user
        // Note: Role relationship is already set via the User entity
        return newUser;
    }

    @Override
    public void updateUser(Long userId, String username, String password) {
        // Implementation for updating user
    }
    
    public void updateUserPassword(String username, String newPassword) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        log.info("Password updated for user: {}", username);
    }

    @Override
    public void deleteUser(UUID id, String currentUsername) {
        try {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            // Prevent self-deletion
            if (user.getUsername().equals(currentUsername)) {
                throw new IllegalArgumentException("Cannot delete your own account while logged in");
            }
            
            log.info("Attempting to delete user: {} (ID: {}) by user: {}", user.getUsername(), id, currentUsername);
            
            // Check if user has any orders
            if (user.getOrders() != null && !user.getOrders().isEmpty()) {
                log.warn("User {} has {} orders, deleting them first", user.getUsername(), user.getOrders().size());
                // You might want to handle orders differently - either delete them or prevent user deletion
            }
            
            userRepository.delete(user);
            log.info("User deleted successfully: {} by {}", user.getUsername(), currentUsername);
        } catch (Exception e) {
            log.error("Error deleting user with ID {} by user {}: {}", id, currentUsername, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public User getUserByName(String name) {
        return null;
    }

    @Override
    public Page<User> getUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }



    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsernameWithCart(username).orElse(null);
    }

    @Override
    public long count() {
        return userRepository.count();
    }

    @Override
    public void updateUserRole(UUID userId, String roleName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found"));
        
        user.setRole(role);
        userRepository.save(user);
        log.info("Role updated for user: {} to role: {}", user.getUsername(), roleName);
    }
}
