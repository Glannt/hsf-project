package com.hsf.hsfproject.controller.order;

import com.hsf.hsfproject.dtos.request.CreateOrderRequest;
import com.hsf.hsfproject.model.Order;
import com.hsf.hsfproject.model.User;
import com.hsf.hsfproject.service.cart.CartService;
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
import com.hsf.hsfproject.model.Cart;

@Controller
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderViewController {
    
    private final OrderService orderService;
    private final IUserService userService;
    private final CartService cartService;
    
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
    
    @GetMapping("/management")
    @PreAuthorize("hasAuthority('ROLE_MANAGER') or hasAuthority('ROLE_ADMIN')")
    public String getAllOrdersForAdmin(Model model, Principal principal, Pageable pageable) {
        addUserInfo(model, principal);
        
        Page<Order> orders = orderService.getAllOrders(pageable);
        model.addAttribute("orders", orders);
        model.addAttribute("isAdminView", true);
        
        return "order/admin";
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
        
        return "redirect:/orders/management";
    }
    
    // Handle order creation from cart
    @PostMapping("")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public String createOrderFromCart(@RequestParam("cartId") String cartId,
                                     @RequestParam("userId") String userId,
                                     Principal principal,
                                     RedirectAttributes redirectAttributes) {
        try {
            // Get user from principal
            User user = userService.findByUsername(principal.getName());
            if (user == null) {
                redirectAttributes.addFlashAttribute("error", "User not found");
                return "redirect:/cart";
            }
            
            // Get cart
            Cart cart = cartService.getCartById(UUID.fromString(cartId));
            if (cart == null || cart.getCartItems().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Cart is empty or not found");
                return "redirect:/cart";
            }
            
            // Create order request from cart
            CreateOrderRequest orderRequest = CreateOrderRequest.builder()
                    .shippingAddress("Default Address") // You can add a form to collect this
                    .orderItems(cart.getCartItems().stream()
                            .map(item -> {
                                CreateOrderRequest.OrderItemRequest orderItem = new CreateOrderRequest.OrderItemRequest();
                                if (item.getComputerItem() != null) {
                                    orderItem.setComputerItemId(item.getComputerItem().getId());
                                    orderItem.setProductType("COMPUTER_ITEM");
                                } else if (item.getPc() != null) {
                                    orderItem.setPcId(item.getPc().getId());
                                    orderItem.setProductType("PC");
                                }
                                orderItem.setQuantity(item.getQuantity());
                                return orderItem;
                            })
                            .collect(java.util.stream.Collectors.toList()))
                    .build();
            
            // Create order
            Order order = orderService.createOrder(principal.getName(), orderRequest);
            
            // Clear cart after successful order creation
            cartService.clearCart(cart.getId());
            
            redirectAttributes.addFlashAttribute("success", "Order created successfully! Order number: " + order.getOrderNumber());
            return "redirect:/orders/" + order.getId();
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to create order: " + e.getMessage());
            return "redirect:/cart";
        }
    }
} 