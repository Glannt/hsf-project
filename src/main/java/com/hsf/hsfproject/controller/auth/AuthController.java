package com.hsf.hsfproject.controller.auth;

import com.hsf.hsfproject.dtos.CreateUserDTO;
import com.hsf.hsfproject.dtos.request.LoginRequest;
import com.hsf.hsfproject.dtos.response.LoginResponse;
import com.hsf.hsfproject.model.Role;
import com.hsf.hsfproject.model.User;
import com.hsf.hsfproject.repository.RoleRepository;
import com.hsf.hsfproject.repository.UserRepository;
import com.hsf.hsfproject.service.user.IUserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@Slf4j(topic = "AuthController")
public class AuthController {
    private final IUserService userService;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;


    @GetMapping("/register")
    public String showRegisterForm() {
        return "register"; // Thymeleaf template: register.html
    }

    @PostMapping("/register")
    public String register(@ModelAttribute CreateUserDTO registerRequest, Model model) {
        try {
            log.info("Attempting to register user: {}", registerRequest.getUsername());

            if (registerRequest.getUsername() == null || registerRequest.getPassword() == null) {
                model.addAttribute("registerError", "Username and password are required");
                return "register";
            }

            User user = userService.createUser(registerRequest);

            // Kiểm tra và gán role nếu cần
            if (user.getRole() == null) {
                log.info("Assigning default role to user: {}", user.getUsername());
                Role role = roleRepository.findByName("ROLE_USER");
                if (role == null) {
                    log.warn("ROLE_USER not found, creating it");
                    role = Role.builder()
                            .name("ROLE_USER")
                            .description("Standard user role")
                            .build();
                    roleRepository.save(role);
                }
                user.setRole(role);
                userRepository.save(user);
            }

            log.info("User registered successfully: {}", user.getUsername());
            model.addAttribute("registerSuccess", "Registration successful! Please log in.");
            return "redirect:/login?register=success";
        } catch (Exception e) {
            log.error("Error registering user: {}", e.getMessage(), e);
            model.addAttribute("registerError", "Registration failed: " + e.getMessage());
            return "register";
        }
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
