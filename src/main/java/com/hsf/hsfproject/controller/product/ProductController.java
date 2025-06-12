package com.hsf.hsfproject.controller.product;

import com.hsf.hsfproject.model.ComputerItem;
import com.hsf.hsfproject.model.PC;
import com.hsf.hsfproject.service.product.IProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/products")
@RequiredArgsConstructor
@Slf4j(topic = "ProductController")
public class ProductController {
    private final IProductService productService;

//    @GetMapping("/api/v1/products")
//    public String getPcList(
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "10") int limit,
//            Model model) {
//        Page<PC> pcPage = productService.getPcList(page, limit);
//        model.addAttribute("pcPage", pcPage);
//        return "product/pcList"; // Thymeleaf template at src/main/resources/templates/product/list.html
//    }
//

}
