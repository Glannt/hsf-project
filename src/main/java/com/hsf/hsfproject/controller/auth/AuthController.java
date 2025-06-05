package com.hsf.hsfproject.controller.auth;

import com.hsf.hsfproject.dtos.CreateUserDTO;
import com.hsf.hsfproject.dtos.request.LoginRequest;
import com.hsf.hsfproject.dtos.response.LoginResponse;
import com.hsf.hsfproject.model.User;
import com.hsf.hsfproject.service.user.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final IUserService userService;


    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody CreateUserDTO registerRequest) {
        // TODO: Implement registration logic

            if (registerRequest.getUsername() == null || registerRequest.getPassword() == null) {
                return ResponseEntity.badRequest().body("Username and password are required");
            }
            User user = userService.createUser(registerRequest);
            if (user == null) {
                return ResponseEntity.badRequest().body("User registration failed");
            }
        return ResponseEntity.ok(user);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        LoginResponse loginResponse = userService.login(loginRequest);
        if (loginResponse == null) {
            return ResponseEntity.status(404).body("User not found or invalid credentials");
        }
        return ResponseEntity.ok(loginResponse);
    }
}
