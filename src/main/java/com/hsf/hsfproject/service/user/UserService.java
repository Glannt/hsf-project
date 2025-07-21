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



    // Triển khai cho admin
    @Override
    public User getUserById(UUID id) {
        return userRepository.findById(id).orElse(null);
    }

    @Override
    public void updateUser(UUID id, String username, String email, String phoneNumber) {
        User user = userRepository.findById(id).orElse(null);
        if (user != null) {
            user.setUsername(username);
            user.setEmail(email);
            user.setPhoneNumber(phoneNumber);
            userRepository.save(user);
        }
    }

    @Override
    public void deleteUser(UUID id) {
        userRepository.deleteById(id);
    }

    @Override
    public Page<User> getUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    @Override
    public User getUserByName(String name) {
        return userRepository.findByUsername(name);
    }

    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
}
