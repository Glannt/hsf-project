package com.hsf.hsfproject.controller.admin;

import com.hsf.hsfproject.constants.enums.OrderStatus;
import com.hsf.hsfproject.dtos.request.CreatePCRequest;
import com.hsf.hsfproject.dtos.request.UpdateUserRoleRequest;
import com.hsf.hsfproject.model.Category;
import com.hsf.hsfproject.model.ComputerItem;
import com.hsf.hsfproject.model.Order;
import com.hsf.hsfproject.model.PC;
import com.hsf.hsfproject.model.User;
import com.hsf.hsfproject.service.order.OrderService;
import com.hsf.hsfproject.service.product.IProductService;
import com.hsf.hsfproject.service.user.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final IUserService userService;
    private final IProductService productService;
    private final OrderService orderService;

    // Inner class for order status update request
    public static class OrderStatusUpdateRequest {
        private String status;
        
        public String getStatus() {
            return status;
        }
        
        public void setStatus(String status) {
            this.status = status;
        }
    }

    public void addUserToModel(Model model, Principal principal) {
        if (principal != null && !model.containsAttribute("user")) {
            User user = userService.findByUsername(principal.getName());
            model.addAttribute("user", user);
            model.addAttribute("isLogin", true);
        } else {
            model.addAttribute("isLogin", false);
        }
    }
    @GetMapping("/product")
    public String adminProduct(Model model,
                                 @RequestParam(name = "pcPage", defaultValue = "0") int pcPage,
                                 @RequestParam(name = "computerItemPage", defaultValue = "0") int computerItemPage,
                                 Principal principal) {
        addUserToModel(model,principal);
        List<Category> categories = productService.getAllCategories();
        Page<PC> pcList = productService.getPcList(pcPage, 2);
        Page<ComputerItem> computerItems = productService.getComputerItemList(computerItemPage, 2);
        model.addAttribute("pcs", pcList);
        model.addAttribute("computerItems", computerItems);
        model.addAttribute("categories", categories);
        model.addAttribute("computerItemsSelect", productService.getComputerItems());
        return "admin/product";
    }

    @GetMapping("/product/add-pc")
    public String showAddPcForm(Model model, Principal principal) {
        addUserToModel(model, principal);
        List<Category> categories = productService.getAllCategories();
        model.addAttribute("pc", new PC());
        model.addAttribute("categories", categories);
        model.addAttribute("computerItemsSelect", productService.getComputerItems());
        return "admin/add-pc";
    }

    @GetMapping("/product/add-item")
    public String showAddItemForm(Model model, Principal principal) {
        addUserToModel(model, principal);
        List<Category> categories = productService.getAllCategories();
        model.addAttribute("computerItem", new ComputerItem());
        model.addAttribute("categories", categories);
        return "admin/add-item";
    }

    @GetMapping("")
    public String adminDashboard(Model model, Principal principal,
                                @RequestParam(name = "userPage", defaultValue = "0") int userPage,
                                @RequestParam(name = "pcPage", defaultValue = "0") int pcPage,
                                @RequestParam(name = "computerItemPage", defaultValue = "0") int computerItemPage) {
        addUserToModel(model, principal);
        // Tổng số user
        long totalUsers = userService.count();
        // Tổng số đơn hàng
        long totalOrders = productService.countOrders();
        // Tổng doanh thu
        Double totalRevenue = productService.sumOrderRevenue();
        // Tổng số sản phẩm (PC + linh kiện)
        long totalProducts = productService.countAllProducts();
        
        // Danh sách users cho quản lý
        Page<User> users = userService.getUsers(PageRequest.of(userPage, 10));
        
        // Danh sách PC và ComputerItems cho quản lý
        Page<PC> pcs = productService.getPcList(pcPage, 5);
        Page<ComputerItem> computerItems = productService.getComputerItemList(computerItemPage, 5);
        
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("totalOrders", totalOrders);
        model.addAttribute("totalRevenue", totalRevenue);
        model.addAttribute("totalProducts", totalProducts);
        model.addAttribute("users", users);
        model.addAttribute("pcs", pcs);
        model.addAttribute("computerItems", computerItems);
        return "admin/index";
    }

//    Computer Item
    @PostMapping("/product/item/save")
    public String addComputerItem(@ModelAttribute ComputerItem item, Model model) {
        try {
            System.out.println("Adding item: " + item.getName());
            ComputerItem newItem = productService.addComputerItem(item);
            return "redirect:/admin/product?success=item_added&message=" + newItem.getName();
        } catch (Exception e) {
            System.out.println("Error adding item: " + e.getMessage());
            return "redirect:/admin/product/add-item?error=item_add_failed&message=" + e.getMessage();
        }
    }

    @PostMapping("/product/item/edit/{id}")
    public String editPcForm(@ModelAttribute ComputerItem item, Model model) {
        System.out.println("Editing item: " + item.getName());
        ComputerItem updatedItem = productService.updateComputerItem(item);
        return "redirect:/admin/product";
    }

    @PostMapping("/product/item/delete/{id}")
    public String deleteComputerItem(@PathVariable UUID id) {
        productService.deleteComputerItem(id); // Assuming this method deletes the item
        return "redirect:/admin/product";
    }


//    PC
    @PostMapping("/product/pc/save")
    public String addNewPC(@ModelAttribute PC item, Model model) {
        try {
            System.out.println("Adding PC: " + item.getName());
            if (item.getComputerItems() != null) {
                for (ComputerItem computerItem : item.getComputerItems()) {
                    System.out.println("Computer Item: " + computerItem.getName());
                }
            }
            // Assuming productService.addPc() returns the newly created PC
            PC newItem = productService.addPc(item);
            return "redirect:/admin/product?success=pc_added&message=" + newItem.getName();
        } catch (Exception e) {
            System.out.println("Error adding PC: " + e.getMessage());
            return "redirect:/admin/product/add-pc?error=pc_add_failed&message=" + e.getMessage();
        }
    }

    @PostMapping("/product/pc/edit/{id}")
    public String editPC(@ModelAttribute PC pc, Model model) {
        System.out.println("Editing PC: " + pc.getName());
        PC updatedPc = productService.updatePc(pc);
        return "redirect:/admin/product";
    }

    @PostMapping("/product/pc/delete/{id}")
    public String deletePC(@PathVariable UUID id) {
        productService.deletePc(id); // Assuming this method deletes the item
        return "redirect:/admin/product";
    }

    @PostMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable UUID id) {
        userService.deleteUser(id);
        return "redirect:/admin";
    }

    @GetMapping("/orders")
    public String adminOrders(Model model, Principal principal,
                             @RequestParam(name = "page", defaultValue = "0") int page) {
        addUserToModel(model, principal);
        
        // Lấy danh sách tất cả đơn hàng với pagination
        Page<Order> orders = orderService.getAllOrders(PageRequest.of(page, 10));
        model.addAttribute("orders", orders);
        
        return "admin/order";
    }

    @PutMapping("/api/orders/{orderId}/status")
    @ResponseBody
    public String updateOrderStatus(@PathVariable UUID orderId, 
                                   @RequestBody OrderStatusUpdateRequest request) {
        try {
            OrderStatus status = OrderStatus.valueOf(request.getStatus());
            orderService.updateOrderStatus(orderId, status, "admin");
            return "Order status updated successfully";
        } catch (Exception e) {
            return "Error updating order status: " + e.getMessage();
        }
    }

    // User Management endpoints
    @GetMapping("/users")
    public String adminUsers(Model model, Principal principal,
                            @RequestParam(name = "page", defaultValue = "0") int page) {
        addUserToModel(model, principal);
        
        // Lấy danh sách tất cả users với pagination
        Page<User> users = userService.getUsers(PageRequest.of(page, 10));
        model.addAttribute("users", users);
        
        return "admin/users";
    }

    @PutMapping("/api/users/{userId}/role")
    @ResponseBody
    public String updateUserRole(@PathVariable UUID userId, 
                                @RequestBody UpdateUserRoleRequest request) {
        try {
            userService.updateUserRole(userId, request.getRoleName());
            return "User role updated successfully";
        } catch (Exception e) {
            return "Error updating user role: " + e.getMessage();
        }
    }

    @DeleteMapping("/api/users/{userId}")
    @ResponseBody
    public String deleteUserApi(@PathVariable UUID userId) {
        try {
            userService.deleteUser(userId);
            return "User deleted successfully";
        } catch (Exception e) {
            return "Error deleting user: " + e.getMessage();
        }
    }

}
