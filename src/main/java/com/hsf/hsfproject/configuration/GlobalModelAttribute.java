package com.hsf.hsfproject.configuration;

import com.hsf.hsfproject.model.User;
import com.hsf.hsfproject.service.user.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.security.Principal;

@Component
@RequiredArgsConstructor
public class GlobalModelAttribute {

    private final IUserService userService;


    @ModelAttribute
    public void addUserToModel(Model model, Principal principal) {
        if (principal != null && !model.containsAttribute("user")) {
            User user = userService.findByUsername(principal.getName());
            model.addAttribute("user", user);
            model.addAttribute("isLogin", true);
        } else {
            model.addAttribute("isLogin", false);
        }
    }
}
