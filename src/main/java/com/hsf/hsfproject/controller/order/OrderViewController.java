package com.hsf.hsfproject.controller.order;

import com.hsf.hsfproject.model.Order;
import com.hsf.hsfproject.model.User;
import com.hsf.hsfproject.service.order.OrderService;
import com.hsf.hsfproject.service.user.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.hsf.hsfproject.constants.enums.OrderStatus;
import java.security.Principal;
import java.util.UUID;

@Controller
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderViewController {
    
    private final OrderService orderService;
    private final IUserService userService;
    
    // Helper method to add user info to model (theo style hiện tại)
    private void addUserInfo(Model model, Principal principal) {
        model.addAttribute("isLogin", principal != null);
        if (principal != null) {
            User user = userService.findByUsername(principal.getName());
            if (user != null) {
                model.addAttribute("user", user);
                model.addAttribute("username", user.getUsername());
            }
        }
    }
    
    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public String getUserOrders(Model model, Principal principal, Pageable pageable) {
        try {
            System.out.println("Getting user orders for: " + (principal != null ? principal.getName() : "null"));
            
            addUserInfo(model, principal);
            
            if (principal != null) {
                Page<Order> orders = orderService.getUserOrders(principal.getName(), pageable);
                System.out.println("Found " + (orders != null ? orders.getTotalElements() : 0) + " orders");
                model.addAttribute("orders", orders);
            } else {
                System.out.println("Principal is null");
            }
            
            return "order/index";
        } catch (Exception e) {
            System.err.println("Error getting user orders: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Failed to load orders: " + e.getMessage());
            return "order/index";
        }
    }
    
    @GetMapping("/{orderId}")
    @PreAuthorize("hasAuthority('ROLE_USER') or hasAuthority('ROLE_MANAGER') or hasAuthority('ROLE_ADMIN')")
    public String getOrderDetail(@PathVariable UUID orderId, Model model, Principal principal) {
        addUserInfo(model, principal);
        
        try {
            Order order = orderService.getOrderById(orderId);
            
            // Kiểm tra phân quyền: USER chỉ xem đơn hàng của mình
            if (principal != null) {
                User user = userService.findByUsername(principal.getName());
                boolean isManager = user.getRole().getName().equals("ROLE_MANAGER");
                boolean isAdmin = user.getRole().getName().equals("ROLE_ADMIN");
                
                if (!isManager && !isAdmin && !order.getUser().getUsername().equals(principal.getName())) {
                    return "redirect:/orders?error=access_denied";
                }
            }
            
            model.addAttribute("order", order);
            return "order/detail";
        } catch (Exception e) {
            return "redirect:/orders?error=order_not_found";
        }
    }
    
    @GetMapping("/admin")
    @PreAuthorize("hasAuthority('ROLE_MANAGER') or hasAuthority('ROLE_ADMIN')")
    public String getAllOrdersForAdmin(Model model, Principal principal, Pageable pageable) {
        addUserInfo(model, principal);
        
        Page<Order> orders = orderService.getAllOrders(pageable);
        model.addAttribute("orders", orders);
        model.addAttribute("isAdminView", true);
        
        return "admin/order";
    }
    
    @PostMapping("/{orderId}/status")
    @PreAuthorize("hasAuthority('ROLE_MANAGER') or hasAuthority('ROLE_ADMIN')")
    public String updateOrderStatus(@PathVariable UUID orderId,
                                   @RequestParam("status") OrderStatus status,
                                   Principal principal,
                                   RedirectAttributes redirectAttributes) {
        try {
            Order order = orderService.updateOrderStatus(orderId, status, principal.getName());
            redirectAttributes.addFlashAttribute("success", "Order status updated successfully to " + status.getDisplayName());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update order status: " + e.getMessage());
        }
        
        return "redirect:/orders/admin";
    }
} 