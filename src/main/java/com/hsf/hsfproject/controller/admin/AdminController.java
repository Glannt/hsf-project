package com.hsf.hsfproject.controller.admin;

import com.hsf.hsfproject.constants.enums.OrderStatus;
import com.hsf.hsfproject.dtos.request.CreatePCRequest;
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
    public String addComputerItem(@ModelAttribute ComputerItem item,Model model) {
        System.out.println("Adding item: " + item.getName());
        ComputerItem newItem = productService.addComputerItem(item);
        model.addAttribute("message", "Item added successfully: " + newItem.getName());
        return "redirect:/admin/product";
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
        System.out.println("Adding item: " + item.getComputerItems());
       for (ComputerItem computerItem : item.getComputerItems()) {
            System.out.println("Computer Item: " + computerItem.getName());
        }
        // Assuming productService.addPc() returns the newly created PC
        PC newItem = productService.addPc(item);
        model.addAttribute("message", "Item added successfully: " + newItem.getName());
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

}
