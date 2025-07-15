package com.hsf.hsfproject.controller.user;

import java.util.UUID;

import com.hsf.hsfproject.model.User;
import jakarta.servlet.http.HttpSession;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.data.domain.Page;
import com.hsf.hsfproject.dtos.CreateUserDTO;
import com.hsf.hsfproject.service.user.IUserService;
import com.hsf.hsfproject.model.*; // ✅ ĐÚNG


import org.springframework.ui.Model;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class UserController {

    private final IUserService userService;
    

    // ✅ Hiển thị danh sách user (có phân trang)
    @GetMapping
    public String listUsers(Model model,
                            @RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "10") int size) {
        Page<User> users = userService.getUsers(PageRequest.of(page, size));

        // ⚠️ THÊM DÒNG DƯỚI NẾU USER.ID BỊ NULL KHI LOAD
        users.getContent().forEach(u -> u.getId().toString()); // ép Hibernate load ID

        model.addAttribute("users", users);
        return "admin/user/user_list";
    }


    // ✅ Hiển thị form tạo mới
@GetMapping("/create")
public String createForm(Model model) {
    model.addAttribute("user", new CreateUserDTO());
    return "admin/user/user_form";
}


    // ✅ Submit tạo user
    @PostMapping("/create")
    public String createUser(@ModelAttribute CreateUserDTO userDTO) {
        userService.createUser(userDTO);
        return "redirect:/admin/users";
    }

    // ✅ Hiển thị form cập nhật user
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable UUID id, Model model) {
        User user = userService.getUserById(id);
        if (user == null) {
            throw new RuntimeException("Không tìm thấy user với ID: " + id);
        }

        // Tạo chuỗi UUID để hiển thị tạm, không ảnh hưởng model
        String uuidView = id.toString(); // hoặc HEX(id) nếu bạn cần

        model.addAttribute("user", user);
        model.addAttribute("uuidView", uuidView); // 👉 thêm dòng này
        return "admin/user/user_edit_form";
    }



    @PostMapping("/update")
    public String updateUser(@RequestParam UUID id,
                             @RequestParam String username,
                             @RequestParam String email,
                             @RequestParam String phoneNumber,
                             @RequestParam(required = false) String password,
                             HttpSession session) {

        User currentUser = (User) session.getAttribute("user");

        userService.updateUser(id, username, email, phoneNumber, password, currentUser);

        return "redirect:/admin/users";
    }


    // ✅ Xoá user
    @GetMapping("/delete/{id}")
    public String deleteUser(@PathVariable UUID id) {
        userService.deleteUser(id);
        return "redirect:/admin/users";
    }

}