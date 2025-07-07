package com.hsf.hsfproject.controller.home;

import com.hsf.hsfproject.model.ComputerItem;
import com.hsf.hsfproject.model.PC;
import com.hsf.hsfproject.model.User;
import com.hsf.hsfproject.service.product.IProductService;
import com.hsf.hsfproject.service.user.IUserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;

@Controller
@RequestMapping("/")
@RequiredArgsConstructor
public class HomeController {
    private final IUserService userService;
    private final IProductService productService;

    public void addUserToModel(Model model, Principal principal) {
        if (principal != null && !model.containsAttribute("user")) {
            User user = userService.findByUsername(principal.getName());
            model.addAttribute("user", user);
            model.addAttribute("username", user.getUsername());
            model.addAttribute("isLogin", true);
        } else {
            model.addAttribute("isLogin", false);
        }
    }

    @GetMapping
    public String home(
            Principal principal,
            HttpSession session,
            @RequestParam(name = "pcPage", defaultValue = "0") int pcPage,
                        @RequestParam(name = "computerItemPage", defaultValue = "0") int computerItemPage,
                        Model model) {
        addUserToModel(model, principal);
//        model.addAttribute("isLogin", principal != null);
//        if (principal != null) {
//            User user = userService.findByUsername(principal.getName());
//            if (user != null) {
//                model.addAttribute("user", user);
//                model.addAttribute("username", user.getUsername());
//                session.setAttribute("user", user);
//            }
//
//        }
        System.out.println("Logged in username: " + (principal != null ? principal.getName() : "Anonymous"));
        Page<PC> pcList = productService.getPcList(pcPage, 2);
        Page<ComputerItem> computerItems = productService.getComputerItemList(computerItemPage, 2);
        model.addAttribute("pcs", pcList);
        model.addAttribute("computerItems", computerItems);

        return "index";
    }


}
