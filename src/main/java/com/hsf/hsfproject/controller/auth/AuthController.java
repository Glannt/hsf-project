package com.hsf.hsfproject.controller.auth;

import com.hsf.hsfproject.dtos.CreateUserDTO;
import com.hsf.hsfproject.dtos.request.LoginRequest;
import com.hsf.hsfproject.dtos.response.LoginResponse;
import com.hsf.hsfproject.model.User;
import com.hsf.hsfproject.service.user.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final IUserService userService;




    @PostMapping("/register")
    public String register(@ModelAttribute CreateUserDTO registerRequest, Model model) {
        if (registerRequest.getUsername() == null || registerRequest.getPassword() == null) {
            model.addAttribute("registerError", "Username and password are required");
            return "register";
        }
        User user = userService.createUser(registerRequest);
        if (user == null) {
            model.addAttribute("registerError", "User registration failed");
            return "register";
        }
        model.addAttribute("registerSuccess", "Registration successful! Please log in.");
        return "login";
    }

    //Show login form

    @GetMapping("/login")
    public String showLoginForm() {
        return "login"; // Thymeleaf template: login.html
    }

    @PostMapping("/login")
    public String login(@ModelAttribute LoginRequest loginRequest, Model model) {
        LoginResponse loginResponse = userService.login(loginRequest);
        if (loginResponse == null) {
            model.addAttribute("loginError", "User not found or invalid credentials");
            return "login";
        }
        model.addAttribute("loginSuccess", "Login successful!");
        // You may want to set session attributes here
        return "redirect:/"; // Redirect to homepage after successful login
    }
}
