package com.hsf.hsfproject.configuration;

import com.hsf.hsfproject.model.User;
import com.hsf.hsfproject.service.user.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalModelAttribute {

    private final IUserService userService;

    @ModelAttribute
    public void addUserToModel(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.isAuthenticated() &&
            !"anonymousUser".equals(authentication.getPrincipal())) {
            
            model.addAttribute("isLogin", true);
            
            // Kiểm tra xem user đã có trong model chưa
            if (!model.containsAttribute("user")) {
                try {
                    User user = userService.findByUsername(authentication.getName());
                    if (user != null) {
                        model.addAttribute("user", user);
                    }
                } catch (Exception e) {
                    // Log lỗi nhưng không làm crash ứng dụng
                    System.err.println("Error getting user in GlobalModelAttribute: " + e.getMessage());
                }
            }
        } else {
            model.addAttribute("isLogin", false);
        }
    }
}
