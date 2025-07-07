package com.hsf.hsfproject.controller.manager;

import com.hsf.hsfproject.model.Category;
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
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/manager")
@RequiredArgsConstructor
@Slf4j
public class ManagerController {

    private final IUserService userService;
    private final IProductService productService;

    private void addUserToModel(Model model, Principal principal) {
        if (principal != null && !model.containsAttribute("user")) {
            User user = userService.findByUsername(principal.getName());
            model.addAttribute("user", user);
            model.addAttribute("isLogin", true);
        } else {
            model.addAttribute("isLogin", false);
        }
    }

    @GetMapping("/products")
    public String managerProducts(Model model,
                                 @RequestParam(name = "pcPage", defaultValue = "0") int pcPage,
                                 @RequestParam(name = "computerItemPage", defaultValue = "0") int computerItemPage,
                                 Principal principal) {
        addUserToModel(model, principal);
        List<Category> categories = productService.getAllCategories();
        Page<PC> pcList = productService.getPcList(pcPage, 10);
        Page<ComputerItem> computerItems = productService.getComputerItemList(computerItemPage, 10);
        model.addAttribute("pcs", pcList);
        model.addAttribute("computerItems", computerItems);
        model.addAttribute("categories", categories);
        model.addAttribute("computerItemsSelect", productService.getComputerItems());
        return "manager/products";
    }

    // Computer Item Management
    @PostMapping("/products/item/save")
    public String addComputerItem(@ModelAttribute ComputerItem item, Model model) {
        try {
            log.info("Adding computer item: {}", item.getName());
            ComputerItem newItem = productService.addComputerItem(item);
            log.info("Successfully added computer item: {}", newItem.getName());
            return "redirect:/manager/products?success=item_added";
        } catch (Exception e) {
            log.error("Error adding computer item: {}", e.getMessage());
            return "redirect:/manager/products?error=item_add_failed&message=" + e.getMessage();
        }
    }

    @PostMapping("/products/item/edit/{id}")
    public String editComputerItem(@ModelAttribute ComputerItem item, @PathVariable UUID id, Model model) {
        try {
            log.info("Editing computer item: {}", item.getName());
            item.setId(id);
            ComputerItem updatedItem = productService.updateComputerItem(item);
            log.info("Successfully updated computer item: {}", updatedItem.getName());
            return "redirect:/manager/products?success=item_updated";
        } catch (Exception e) {
            log.error("Error updating computer item: {}", e.getMessage());
            return "redirect:/manager/products?error=item_update_failed&message=" + e.getMessage();
        }
    }

    @PostMapping("/products/item/delete/{id}")
    public String deleteComputerItem(@PathVariable UUID id) {
        try {
            log.info("Deleting computer item with ID: {}", id);
            productService.deleteComputerItem(id);
            log.info("Successfully deleted computer item with ID: {}", id);
            return "redirect:/manager/products?success=item_deleted";
        } catch (Exception e) {
            log.error("Error deleting computer item: {}", e.getMessage());
            return "redirect:/manager/products?error=item_delete_failed&message=" + e.getMessage();
        }
    }

    // PC Management
    @PostMapping("/products/pc/save")
    public String addPC(@ModelAttribute PC pc, Model model) {
        try {
            log.info("Adding PC: {}", pc.getName());
            PC newPc = productService.addPc(pc);
            log.info("Successfully added PC: {}", newPc.getName());
            return "redirect:/manager/products?success=pc_added";
        } catch (Exception e) {
            log.error("Error adding PC: {}", e.getMessage());
            return "redirect:/manager/products?error=pc_add_failed&message=" + e.getMessage();
        }
    }

    @PostMapping("/products/pc/delete/{id}")
    public String deletePC(@PathVariable UUID id) {
        try {
            log.info("Deleting PC with ID: {}", id);
            productService.deletePc(id);
            log.info("Successfully deleted PC with ID: {}", id);
            return "redirect:/manager/products?success=pc_deleted";
        } catch (Exception e) {
            log.error("Error deleting PC: {}", e.getMessage());
            return "redirect:/manager/products?error=pc_delete_failed&message=" + e.getMessage();
        }
    }
} 