package com.hsf.hsfproject.controller.auth;

import com.hsf.hsfproject.dtos.CreateUserDTO;
import com.hsf.hsfproject.dtos.request.LoginRequest;
import com.hsf.hsfproject.dtos.response.LoginResponse;
import com.hsf.hsfproject.model.User;
import com.hsf.hsfproject.service.user.IUserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
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
                                Model model) {
        if(error != null) {
            model.addAttribute("loginError", "Invalid username or password");
        }
        return "login/index"; // Thymeleaf template: login/index.html
    }

//    @PostMapping("/login")
//    public String login(Principal principal,HttpSession session, Model model) {
//        if (principal == null) {
//            model.addAttribute("loginError", "Invalid credentials");
//            return "login/index";
//        }
//        User user = userService.findByUsername(principal.getName());
//        log.info("User found: {}", user);
//        log.info("Đã vào đây");
//        if (user == null) {
//            model.addAttribute("loginError", "User not found");
//            return "login/index";
//        }
//        session.setAttribute("username", user.getUsername());
//        session.setAttribute("role", user.getRole()); // Store user role in session
//        System.out.println("Session username: " + session.getAttribute("username"));
//        return "redirect:/"; // Redirect to homepage
//
//    }
}
