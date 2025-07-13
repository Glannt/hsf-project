package com.hsf.hsfproject.controller.admin;

import com.hsf.hsfproject.dtos.request.UpdateUserRoleRequest;
import com.hsf.hsfproject.model.User;
import com.hsf.hsfproject.service.user.IUserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
@Slf4j
public class UserManagementController {
    
    private final IUserService userService;
    
    @GetMapping
    public ResponseEntity<Page<User>> getAllUsers(Pageable pageable) {
        Page<User> users = userService.getUsers(pageable);
        return ResponseEntity.ok(users);
    }
    
    @PutMapping("/{userId}/role")
    public ResponseEntity<String> updateUserRole(
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateUserRoleRequest request
    ) {
        try {
            userService.updateUserRole(userId, request.getRoleName());
            return ResponseEntity.ok("User role updated successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error updating user role: " + e.getMessage());
        }
    }
    
    @DeleteMapping("/{userId}")
    public ResponseEntity<String> deleteUser(@PathVariable UUID userId, Principal principal) {
        try {
            log.info("Admin attempting to delete user: {} by user: {}", userId, principal.getName());
            userService.deleteUser(userId, principal.getName());
            log.info("User {} deleted successfully by admin {}", userId, principal.getName());
            return ResponseEntity.ok("User deleted successfully");
        } catch (Exception e) {
            log.error("Error deleting user {} by admin {}: {}", userId, principal.getName(), e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error deleting user: " + e.getMessage());
        }
    }
    
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetUserPassword(@RequestParam String username, @RequestParam String newPassword) {
        try {
            userService.updateUserPassword(username, newPassword);
            return ResponseEntity.ok("Password reset successfully for user: " + username);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error resetting password: " + e.getMessage());
        }
    }
} 