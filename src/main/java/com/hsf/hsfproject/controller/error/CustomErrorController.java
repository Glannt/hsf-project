package com.hsf.hsfproject.controller.error;

import com.hsf.hsfproject.model.User;
import com.hsf.hsfproject.service.user.IUserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class CustomErrorController implements ErrorController {

    private final IUserService userService;

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model, Principal principal) {
        // Ensure isLogin attribute is always set
        if (principal != null) {
            User user = userService.findByUsername(principal.getName());
            model.addAttribute("user", user);
            model.addAttribute("isLogin", true);
        } else {
            model.addAttribute("isLogin", false);
        }

        // Get error details
        Integer statusCode = (Integer) request.getAttribute("javax.servlet.error.status_code");
        String errorMessage = (String) request.getAttribute("javax.servlet.error.message");
        
        if (statusCode != null) {
            model.addAttribute("errorCode", statusCode);
        }
        
        if (errorMessage != null && !errorMessage.isEmpty()) {
            model.addAttribute("message", errorMessage);
        }

        return "error";
    }
}
