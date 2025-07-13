package com.hsf.hsfproject.controller.manager;

import com.hsf.hsfproject.dtos.request.UpdateUserRoleRequest;
import com.hsf.hsfproject.model.Category;
import com.hsf.hsfproject.model.ComputerItem;
import com.hsf.hsfproject.model.PC;
import com.hsf.hsfproject.model.User;
import com.hsf.hsfproject.service.product.IProductService;
import com.hsf.hsfproject.service.user.IUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
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

    @GetMapping("/products/add-pc")
    public String showAddPcForm(Model model, Principal principal) {
        addUserToModel(model, principal);
        List<Category> categories = productService.getAllCategories();
        model.addAttribute("pc", new PC());
        model.addAttribute("categories", categories);
        model.addAttribute("computerItemsSelect", productService.getComputerItems());
        return "manager/add-pc";
    }

    @GetMapping("/products/add-item")
    public String showAddItemForm(Model model, Principal principal) {
        addUserToModel(model, principal);
        List<Category> categories = productService.getAllCategories();
        model.addAttribute("computerItem", new ComputerItem());
        model.addAttribute("categories", categories);
        return "manager/add-item";
    }

    // Computer Item Management
    @PostMapping("/products/item/save")
    public String addComputerItem(@ModelAttribute ComputerItem item, Model model) {
        try {
            log.info("Adding computer item: {}", item.getName());
            ComputerItem newItem = productService.addComputerItem(item);
            log.info("Successfully added computer item: {}", newItem.getName());
            return "redirect:/manager/products?success=item_added&message=" + newItem.getName();
        } catch (Exception e) {
            log.error("Error adding computer item: {}", e.getMessage());
            return "redirect:/manager/products/add-item?error=item_add_failed&message=" + e.getMessage();
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
    @ResponseBody
    public ResponseEntity<String> deleteComputerItem(@PathVariable UUID id) {
        try {
            log.info("=== Starting Computer Item deletion process ===");
            log.info("Deleting computer item with ID: {}", id);
            
            // Kiểm tra item có tồn tại không
            ComputerItem existingItem = productService.findItemById(id);
            log.info("Found item to delete: {}", existingItem.getName());
            
            // Thực hiện xóa
            productService.deleteComputerItem(id);
            log.info("Successfully deleted computer item with ID: {}", id);
            
            return ResponseEntity.ok("Computer item deleted successfully");
        } catch (Exception e) {
            log.error("=== Error during Computer Item deletion ===");
            log.error("Error deleting computer item with ID {}: {}", id, e.getMessage());
            log.error("Stack trace:", e);
            return ResponseEntity.badRequest().body("Error deleting computer item: " + e.getMessage());
        }
    }

    // PC Management
    @PostMapping("/products/pc/save")
    public String addPC(@ModelAttribute PC pc, Model model) {
        try {
            log.info("Adding PC: {}", pc.getName());
            PC newPc = productService.addPc(pc);
            log.info("Successfully added PC: {}", newPc.getName());
            return "redirect:/manager/products?success=pc_added&message=" + newPc.getName();
        } catch (Exception e) {
            log.error("Error adding PC: {}", e.getMessage());
            return "redirect:/manager/products/add-pc?error=pc_add_failed&message=" + e.getMessage();
        }
    }

    @PostMapping("/products/pc/edit/{id}")
    public String editPC(@ModelAttribute PC pc, @PathVariable UUID id, Model model) {
        try {
            log.info("Editing PC: {}", pc.getName());
            pc.setId(id);
            PC updatedPc = productService.updatePc(pc);
            log.info("Successfully updated PC: {}", updatedPc.getName());
            return "redirect:/manager/products?success=pc_updated";
        } catch (Exception e) {
            log.error("Error updating PC: {}", e.getMessage());
            return "redirect:/manager/products?error=pc_update_failed&message=" + e.getMessage();
        }
    }

    @PostMapping("/products/pc/delete/{id}")
    @ResponseBody
    public ResponseEntity<String> deletePC(@PathVariable UUID id) {
        try {
            log.info("=== Starting PC deletion process ===");
            log.info("Deleting PC with ID: {}", id);
            
            // Kiểm tra PC có tồn tại không
            PC existingPc = productService.findPcById(id);
            log.info("Found PC to delete: {}", existingPc.getName());
            
            // Thực hiện xóa
            productService.deletePc(id);
            log.info("Successfully deleted PC with ID: {}", id);
            
            return ResponseEntity.ok("PC deleted successfully");
        } catch (Exception e) {
            log.error("=== Error during PC deletion ===");
            log.error("Error deleting PC with ID {}: {}", id, e.getMessage());
            log.error("Stack trace:", e);
            return ResponseEntity.badRequest().body("Error deleting PC: " + e.getMessage());
        }
    }

    // User Management for Managers
    @GetMapping("/users")
    public String managerUsers(Model model, Principal principal,
                              @RequestParam(name = "page", defaultValue = "0") int page) {
        addUserToModel(model, principal);
        // Get all users with pagination (managers can only see regular users)
        Page<User> users = userService.getUsers(PageRequest.of(page, 10));
        model.addAttribute("users", users);
        return "manager/users";
    }
} 