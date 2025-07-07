package com.hsf.hsfproject.controller.auth;

import com.hsf.hsfproject.dtos.CreateUserDTO;
import com.hsf.hsfproject.model.User;
import com.hsf.hsfproject.service.user.IUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@Slf4j(topic = "AuthController")
public class AuthController {
    private final IUserService userService;

    @GetMapping("/register")
    public String showRegisterForm() {
        return "register"; // Thymeleaf template: register.html
    }

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
        return "login/index";
    }

    //Show login form

    @GetMapping("/login")
    public String showLoginForm(@RequestParam(value = "error", required = false) String error,
//                                @RequestParam(value = "logout", required = false) String logout,
                                Model model) {
        if(error != null) {
            model.addAttribute("loginError", "Invalid username or password");
        }
        return "login/index"; // Thymeleaf template: login/index.html
    }


}
