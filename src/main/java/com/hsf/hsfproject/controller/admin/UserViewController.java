package com.hsf.hsfproject.controller.admin;

import com.hsf.hsfproject.model.User;
import com.hsf.hsfproject.service.user.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;

@Controller
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ADMIN')")
public class UserViewController {
    
    private final IUserService userService;
    
    // Helper method to add user info to model (theo style hiện tại)
    private void addUserInfo(Model model, Principal principal) {
        model.addAttribute("isLogin", principal != null);
        if (principal != null) {
            User user = userService.findByUsername(principal.getName());
            if (user != null) {
                model.addAttribute("user", user);
                model.addAttribute("username", user.getUsername());
            }
        }
    }
    
    @GetMapping
    public String getAllUsers(Model model, Principal principal, Pageable pageable) {
        addUserInfo(model, principal);
        
        Page<User> users = userService.getUsers(pageable);
        model.addAttribute("users", users);
        
        return "admin/user";
    }
} 