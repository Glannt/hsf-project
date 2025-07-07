package com.hsf.hsfproject.controller.order;

import com.hsf.hsfproject.dtos.request.CreateOrderRequest;
import com.hsf.hsfproject.model.Cart;
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

import java.security.Principal;
import java.util.ArrayList;
import java.util.UUID;

@Controller
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final CartService cartService;
    private final IUserService userService;
    
    // Helper method to add user info to model
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
    
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public String createOrderFromCart(@RequestParam("cartId") UUID cartId, 
                                    @RequestParam("userId") UUID userId,
                                    Model model, 
                                    Principal principal) {
        try {
            addUserInfo(model, principal);
            
            System.out.println("Creating order from cart. CartId: " + cartId + ", UserId: " + userId);
            
            // Lấy cart
            Cart cart = cartService.getCartByUserId(userId.toString());
            if (cart == null || cart.getCartItems().isEmpty()) {
                System.out.println("Cart is null or empty");
                return "redirect:/cart?error=empty_cart";
            }
            
            System.out.println("Cart found with " + cart.getCartItems().size() + " items");
            
            // Tạo CreateOrderRequest từ cart
            CreateOrderRequest request = new CreateOrderRequest();
            request.setShippingAddress("Default Address"); // Có thể thêm form để nhập địa chỉ
            request.setOrderItems(new ArrayList<>());
            
            cart.getCartItems().forEach(item -> {
                CreateOrderRequest.OrderItemRequest orderItem = new CreateOrderRequest.OrderItemRequest();
                orderItem.setQuantity(item.getQuantity());
                
                if (item.getComputerItem() != null) {
                    orderItem.setProductType("COMPUTER_ITEM");
                    orderItem.setComputerItemId(item.getComputerItem().getId());
                    System.out.println("Adding computer item: " + item.getComputerItem().getName());
                } else if (item.getPc() != null) {
                    orderItem.setProductType("PC");
                    orderItem.setPcId(item.getPc().getId());
                    System.out.println("Adding PC: " + item.getPc().getName());
                }
                
                request.getOrderItems().add(orderItem);
            });
            
            System.out.println("Created order request with " + request.getOrderItems().size() + " items");
            
            // Tạo order
            Order order = orderService.createOrder(principal.getName(), request);
            
            System.out.println("Order created successfully with ID: " + order.getId());
            
            // Xóa cart sau khi tạo order thành công
            cartService.clearCart(userId);
            
            return "redirect:/orders/" + order.getId() + "?success=order_created";
            
        } catch (Exception e) {
            System.err.println("Error creating order: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/cart?error=order_creation_failed";
        }
    }
}
