package com.hsf.hsfproject.service.user;

import com.hsf.hsfproject.dtos.CreateUserDTO;
import com.hsf.hsfproject.dtos.request.LoginRequest;
import com.hsf.hsfproject.dtos.response.LoginResponse;
import com.hsf.hsfproject.model.Role;
import com.hsf.hsfproject.model.User;
import com.hsf.hsfproject.repository.CartRepository;
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
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@Slf4j(topic = "UserService")
@RequiredArgsConstructor
public class UserService implements IUserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final CartRepository cartRepository;
    private final ICartService cartService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    @Override
    public User createUser(CreateUserDTO userDTO) {

        User user = userRepository.findByUsername(userDTO.getUsername());
        if (user != null) {
            log.info("User with username {} already exists" + userDTO.getUsername());
            throw new IllegalArgumentException("User with username " + userDTO.getUsername() + " already exists");
        }
        //build a new user from the DTO

        User newUser = User.builder()
                .username(userDTO.getUsername())
                .password(passwordEncoder.encode(userDTO.getPassword()))
                .email(userDTO.getEmail())
                .phoneNumber(userDTO.getPhoneNumber())
                .role(roleRepository.findByName("ROLE_USER"))
                .build();

        // Set the cart for the new user
        newUser.setCart(cartService.createCart(newUser));

        // Save the new user to the repository
        userRepository.save(newUser);

        // Assign the default role to the user
        Role role = roleRepository.findByName("ROLE_USER");
        role.setUsers(Set.of(newUser));
        return newUser;
    }

    @Override
    public void updateUser(UUID userId, String username, String password) {
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
        user.setUsername(username);
        if (password != null && !password.isEmpty()) {
            user.setPassword(passwordEncoder.encode(password));
        }
        userRepository.save(user);
        log.info("User with id {} updated successfully", userId);

    }

    @Override
    public void deleteUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Nếu có cart → xóa cart trước
        if (user.getCart() != null) {
            cartRepository.delete(user.getCart());
        }

        // Không cần xóa role, vì role là @ManyToOne – user trỏ tới role (không làm ảnh hưởng)
        userRepository.delete(user);
        log.info("User with id {} deleted successfully");
    }


    @Override
    public User getUserByName(String name) {
        User user = userRepository.findByUsername(name);
        if (user == null) {
            log.info("User with username {} not found", name);
            throw new IllegalArgumentException("User with username " + name + " not found");
        }
        return user;
    }

    @Override
    public Page<User> getUsers(Pageable pageable) {
        Page<User> users = userRepository.findAll(pageable);
        if (users.isEmpty()) {
            log.info("No users found");
            throw new IllegalArgumentException("No users found");
        }
        return users;
    }

    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public User getUserById(UUID id) {
        return userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));
    }

    @Override
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }



    public void updateUser(UUID id, String username, String email, String phoneNumber,
                           String password, User currentUser) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setUsername(username);
        user.setEmail(email);
        user.setPhoneNumber(phoneNumber);

        // Chỉ đổi mật khẩu nếu người dùng nhập mới
        if (password != null && !password.isBlank()) {
            if (password.length() > 72) { // BCrypt has a 72 byte limit
                throw new IllegalArgumentException("Password is too long");
            }
            try {
                user.setPassword(passwordEncoder.encode(password));
            } catch (Exception e) {
                log.error("Failed to encode password", e);
                throw new RuntimeException("Error while processing password");
            }
        }
        
        userRepository.save(user);
    }

}
