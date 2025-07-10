package com.hsf.hsfproject.controller.admin;

import com.hsf.hsfproject.model.User;
import com.hsf.hsfproject.service.user.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.hsf.hsfproject.service.user.UserManagementService;
import java.security.Principal;
import java.util.UUID;

@Controller
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class UserViewController {
    
    private final IUserService userService;
    private final UserManagementService userManagementService;
    
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
    
    @PostMapping("/{userId}/role")
    public String updateUserRole(@PathVariable UUID userId,
                                @RequestParam("roleName") String roleName,
                                RedirectAttributes redirectAttributes) {
        try {
            userManagementService.updateUserRole(userId, roleName);
            redirectAttributes.addFlashAttribute("success", "User role updated successfully to " + roleName);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update user role: " + e.getMessage());
        }
        
        return "redirect:/admin/users";
    }
    
    @PostMapping("/{userId}/delete")
    public String deleteUser(@PathVariable UUID userId,
                            RedirectAttributes redirectAttributes) {
        try {
            userManagementService.deleteUser(userId);
            redirectAttributes.addFlashAttribute("success", "User deleted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to delete user: " + e.getMessage());
        }
        
        return "redirect:/admin/users";
    }
} 