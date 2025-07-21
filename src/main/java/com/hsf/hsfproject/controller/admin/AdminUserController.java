package com.hsf.hsfproject.controller.admin;

import com.hsf.hsfproject.model.User;
import com.hsf.hsfproject.service.user.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Controller
@RequestMapping("/admin/user")
@RequiredArgsConstructor
public class AdminUserController {
    private final IUserService userService;

    @GetMapping
    public String listUsers(Model model, @RequestParam(defaultValue = "0") int page) {
        Page<User> users = userService.getUsers(PageRequest.of(page, 10));
        model.addAttribute("users", users);
        return "admin/user";
    }

    @GetMapping("/{id}")
    public String userDetail(@PathVariable UUID id, Model model) {
        User user = userService.getUserById(id);
        model.addAttribute("userDetail", user);
        return "admin/user-detail";
    }

    @PostMapping("/{id}/edit")
    public String editUser(@PathVariable UUID id, @RequestParam String username, @RequestParam String email, @RequestParam String phoneNumber) {
        userService.updateUser(id, username, email, phoneNumber);
        return "redirect:/admin/user";
    }

    @PostMapping("/{id}/delete")
    public String deleteUser(@PathVariable UUID id) {
        userService.deleteUser(id);
        return "redirect:/admin/user";
    }
} 