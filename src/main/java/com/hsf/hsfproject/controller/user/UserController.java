package com.hsf.hsfproject.controller.user;

import com.hsf.hsfproject.model.Order;
import com.hsf.hsfproject.model.Transaction;
import com.hsf.hsfproject.model.User;
import com.hsf.hsfproject.service.order.IOrderService;
import com.hsf.hsfproject.service.payment.PaymentService;
import com.hsf.hsfproject.service.user.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    
    private final IUserService userService;
    private final IOrderService orderService;
    private final PaymentService paymentService;
    
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
    
    @GetMapping("/orders")
    public String getUserOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Principal principal,
            Model model) {
        
        if (principal == null) {
            return "redirect:/login";
        }
        
        User currentUser = userService.findByUsername(principal.getName());
        if (currentUser == null) {
            return "redirect:/login";
        }
        
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            Page<Order> orderPage = orderService.getOrdersByUserId(currentUser.getId().toString(), pageable);
            
            model.addAttribute("orders", orderPage);
            model.addAttribute("user", currentUser);
            model.addAttribute("isLogin", true);
            return "user/orders";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to load orders: " + e.getMessage());
            return "user/orders";
        }
    }
    
    @GetMapping("/orders/{orderId}")
    public String getOrderDetails(@PathVariable String orderId, Principal principal, Model model) {
        if (principal == null) {
            return "redirect:/login";
        }
        
        User currentUser = userService.findByUsername(principal.getName());
        if (currentUser == null) {
            return "redirect:/login";
        }
        
        try {
            Order order = orderService.getOrderById(orderId);
            if (order == null || !order.getUser().getId().equals(currentUser.getId())) {
                model.addAttribute("error", "Order not found or access denied");
                return "user/orders";
            }
            
            // Get transaction details
            Transaction transaction = null;
            try {
                transaction = paymentService.findTransactionByOrderId(order.getId());
            } catch (Exception e) {
                // Transaction might not exist for some orders (e.g., COD orders without payment processing)
                // This is acceptable, we'll just show order details without transaction info
            }
            
            // Add order and transaction to model
            model.addAttribute("order", order);
            if (transaction != null) {
                model.addAttribute("transaction", transaction);
            }
            
            // Add user info
            addUserInfo(model, principal);
            
            return "user/order-details";
            
        } catch (Exception e) {
            model.addAttribute("error", "Error retrieving order details: " + e.getMessage());
            return "user/orders";
        }
    }
}