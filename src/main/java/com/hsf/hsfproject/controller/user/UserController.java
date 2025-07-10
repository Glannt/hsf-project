package com.hsf.hsfproject.controller.user;

import com.hsf.hsfproject.dtos.request.CartItemRequest;
import com.hsf.hsfproject.model.User;
import com.hsf.hsfproject.service.cart.ICartService;
import com.hsf.hsfproject.service.user.IUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final IUserService userService;
    private final ICartService cartService;

    private void addUserToModel(Model model, Principal principal) {
        if (principal != null && !model.containsAttribute("user")) {
            User user = userService.findByUsername(principal.getName());
            model.addAttribute("user", user);
            model.addAttribute("isLogin", true);
        } else {
            model.addAttribute("isLogin", false);
        }
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public String userDashboard(Model model, Principal principal) {
        log.info("User dashboard accessed by: {}", principal != null ? principal.getName() : "Anonymous");
        addUserToModel(model, principal);
        return "user/dashboard";
    }

    @GetMapping("/debug-session")
    @ResponseBody
    public String debugSession(Principal principal) {
        if (principal == null) {
            return "No user logged in";
        }
        
        User user = userService.findByUsername(principal.getName());
        if (user == null) {
            return "User not found in database";
        }
        
        return String.format("User: %s, Role: %s, Cart: %s", 
            user.getUsername(), 
            user.getRole() != null ? user.getRole().getName() : "NULL",
            user.getCart() != null ? user.getCart().getId() : "NULL");
    }

    @GetMapping("/cart/test")
    @ResponseBody
    public String testCartEndpoint(Principal principal) {
        if (principal == null) {
            return "No user logged in";
        }
        return "Cart endpoint accessible for user: " + principal.getName();
    }

    @GetMapping("/test-cart")
    public String testCartPage() {
        return "test-cart";
    }

    @GetMapping("/debug-auth")
    @ResponseBody
    public String debugAuth(Principal principal) {
        return "Debug endpoint working! Principal: " + (principal != null ? principal.getName() : "null");
    }

    @GetMapping("/debug-user")
    @ResponseBody
    public String debugUser(Principal principal) {
        if (principal == null) {
            return "No user logged in";
        }
        
        User user = userService.findByUsername(principal.getName());
        if (user == null) {
            return "User not found in database";
        }
        
        return String.format("User: %s, Role: %s, Authorities: %s, Cart: %s", 
            user.getUsername(), 
            user.getRole() != null ? user.getRole().getName() : "NULL",
            user.getAuthorities(),
            user.getCart() != null ? user.getCart().getId() : "NULL");
    }

    @PostMapping("/cart/add")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public String addToCart(@RequestParam("id") String productId,
                           @RequestParam("cartId") String cartId,
                           @RequestParam("quantity") int quantity,
                           @RequestHeader(value = "Referer", required = false) String referer,
                           Principal principal) {
        
        // Debug: Kiểm tra authentication
        if (principal == null) {
            log.error("No user logged in - redirecting to login");
            return "redirect:/login";
        }
        
        log.info("User authenticated: {}", principal.getName());
        
        log.info("Add to cart request - ProductId: {}, CartId: {}, Quantity: {}, User: {}", 
                productId, cartId, quantity, principal != null ? principal.getName() : "Anonymous");
        
        try {
            CartItemRequest request = CartItemRequest.builder()
                    .productId(productId)
                    .cartId(cartId)
                    .quantity(quantity)
                    .build();
            
            cartService.addCartItemToCart(request);
            log.info("Successfully added product {} to cart", productId);
            
        } catch (Exception e) {
            log.error("Error adding product to cart: {}", e.getMessage(), e);
        }
        
        // Redirect back to the previous page or home
        return "redirect:" + (referer != null ? referer : "/");
    }
} 