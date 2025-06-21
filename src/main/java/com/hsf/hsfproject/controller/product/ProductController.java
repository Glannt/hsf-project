package com.hsf.hsfproject.controller.product;

import com.hsf.hsfproject.model.ComputerItem;
import com.hsf.hsfproject.model.Image;
import com.hsf.hsfproject.model.PC;
import com.hsf.hsfproject.model.User;
import com.hsf.hsfproject.service.product.IProductService;
import com.hsf.hsfproject.service.user.IUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/product")
@RequiredArgsConstructor
@Slf4j(topic = "ProductController")
public class ProductController {
    private final IProductService productService;
    private final IUserService userService;
    public void addUserToModel(Model model, Principal principal) {
        if (principal != null && !model.containsAttribute("user")) {
            User user = userService.findByUsername(principal.getName());
            model.addAttribute("user", user);
            model.addAttribute("isLogin", true);
        } else {
            model.addAttribute("isLogin", false);
        }
    }

    @GetMapping("/{id}")
    public String viewProduct(@PathVariable("id") UUID id, Model model, Principal principal) {
        addUserToModel(model, principal);
        Object result = productService.getProduct(id);

        if (result instanceof PC) {
            model.addAttribute("productPC", (PC) result);

            return "product/detail";
        } else if (result instanceof ComputerItem) {
            model.addAttribute("productCI", (ComputerItem) result);
//            List<Object> suggestions = productService.getProductsByCategorySuggest( ((ComputerItem) result).getCategory().getName());
//            model.addAttribute("suggestions", suggestions);
            model.addAttribute("images", ((ComputerItem) result).getImages());
            return "product/detail";
        }
//        model.addAttribute("product", result);
        return "error/404";
    }


}
