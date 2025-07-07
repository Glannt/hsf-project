package com.hsf.hsfproject.controller.product;

import com.hsf.hsfproject.model.ComputerItem;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;

@Controller
@RequestMapping("/products")
@RequiredArgsConstructor
@Slf4j
public class ProductsController {

    private final IProductService productService;
    private final IUserService userService;

    private void addUserToModel(Model model, Principal principal) {
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
    public String showAllProducts(
            Model model,
            Principal principal,
            @RequestParam(name = "pcPage", defaultValue = "0") int pcPage,
            @RequestParam(name = "computerItemPage", defaultValue = "0") int computerItemPage) {
        
        log.info("Products page accessed by: {}", principal != null ? principal.getName() : "Anonymous");
        addUserToModel(model, principal);
        
        Page<PC> pcList = productService.getPcList(pcPage, 6);
        Page<ComputerItem> computerItems = productService.getComputerItemList(computerItemPage, 6);
        
        model.addAttribute("pcs", pcList);
        model.addAttribute("computerItems", computerItems);
        
        return "product/index";
    }
} 