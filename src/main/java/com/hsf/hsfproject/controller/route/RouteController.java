package com.hsf.hsfproject.controller.route;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RouteController {
    @GetMapping("/")
    public String home(Model model) {
        // Optionally add products to the model here
        return "index";
    }

    // Show registration form
    @GetMapping("/register")
    public String showRegisterForm() {
        return "register"; // Thymeleaf template: register.html
    }

    //Show login form

    @GetMapping("/login")
    public String showLoginForm() {
        return "login"; // Thymeleaf template: login.html
    }
}
