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

    @Override
    public void updateUser(Long userId, String username, String password) {

    }

    @Override
    public void deleteUser(Long userId) {

    }

    @Override
    public User getUserByName(String name) {
        return null;
    }

    @Override
    public Page<User> getUsers(Pageable pageable) {
        return null;
    }



    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
}
