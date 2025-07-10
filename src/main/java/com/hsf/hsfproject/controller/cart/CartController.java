package com.hsf.hsfproject.controller.cart;

import com.hsf.hsfproject.dtos.request.CartItemRequest;
import com.hsf.hsfproject.model.Cart;
import com.hsf.hsfproject.model.CartItem;
import com.hsf.hsfproject.model.User;
import com.hsf.hsfproject.service.cart.ICartService;
import com.hsf.hsfproject.service.user.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final ICartService cartService;
    private final IUserService userService;

    @GetMapping("")
    public String getCartPage(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login"; // Nếu chưa đăng nhập thì chuyển về login
        }
        System.out.println("Logged in username: " + principal.getName());
        User user = userService.findByUsername(principal.getName());
        
        if (user == null || user.getCart() == null) {
            return "redirect:/login?error=user_not_found";
        }
        
        Cart cart = user.getCart();
        // Tính lại tổng tiền
        double total = 0.0;
        if (cart.getCartItems() != null) {
            for (CartItem item : cart.getCartItems()) {
                total += item.getQuantity() * item.getUnitPrice();
            }
        }
        cart.setTotalPrice(total);

        if (model.containsAttribute("cart")) {
            model.asMap().remove("cart");
        }

        model.addAttribute("user", user);
        model.addAttribute("cart", cart);
        return "cart/index";
    }


    @GetMapping("/debug")
    @ResponseBody
    public String debugUser(Principal principal) {
        if (principal == null) {
            return "No user logged in";
        }
        
        User user = userService.findByUsername(principal.getName());
        if (user == null) {
            return "User not found in database";
        }
        
        return String.format("User: %s, Role: %s, Authorities: %s", 
            user.getUsername(), 
            user.getRole() != null ? user.getRole().getName() : "NULL",
            user.getAuthorities());
    }

    @GetMapping("/debug-security")
    @ResponseBody
    public String debugSecurity(Principal principal) {
        if (principal == null) {
            return "No user logged in";
        }
        
        return String.format("Principal: %s, Class: %s", 
            principal.getName(), 
            principal.getClass().getSimpleName());
    }

    @PostMapping("/add")
    // @PreAuthorize("hasAuthority('ROLE_USER')")  // Tạm thời comment để test
    public String addToCart(@RequestParam("productId") String productId,
                            @RequestParam("cartId") String cartId,
                            @RequestParam("quantity") int quantity,
                            @RequestHeader(value = "Referer", required = false) String referer,
                            Model model,
                            Principal principal
    ) {
        System.out.println("=== DEBUG: User attempting to add to cart ===");
        System.out.println("Principal: " + (principal != null ? principal.getName() : "NULL"));
        if (principal != null) {
            System.out.println("User authorities: " + principal.getClass().getSimpleName());
        }
        System.out.println("Add to cart request - ProductId: " + productId + ", CartId: " + cartId + ", Quantity: " + quantity);
        try {
            System.out.println("Adding to cart - Product ID: " + productId + ", Cart ID: " + cartId + ", Quantity: " + quantity);
            
            CartItemRequest request = CartItemRequest.builder()
                    .cartId(cartId)
                    .productId(productId)
                    .quantity(quantity)
                    .build();
            
            cartService.addCartItemToCart(request);
            
            // Always redirect to products page
            return "redirect:/products?success=added_to_cart";
        } catch (Exception e) {
            System.err.println("Error adding to cart: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Failed to add item to cart: " + e.getMessage());
            return "redirect:/products?error=add_to_cart_failed";
        }
    }

    @PostMapping("/remove")
    @ResponseBody
    public ResponseEntity<String> removeFromCart(@RequestParam("itemId") String itemId, Principal principal) {
        try {
            if (principal == null) {
                return ResponseEntity.status(401).body("User not authenticated");
            }
            
            cartService.removeFromCart(principal.getName(), UUID.fromString(itemId));
            return ResponseEntity.ok("Item removed successfully");
        } catch (Exception e) {
            System.err.println("Error removing item from cart: " + e.getMessage());
            return ResponseEntity.badRequest().body("Failed to remove item: " + e.getMessage());
        }
    }

    @PostMapping("/update")
    public String updateCartItem(@RequestParam("itemIds") List<String> itemIds,
                                 @RequestParam("quantities") List<Integer> quantities,
                                 Model model) {
        try {
            System.out.println("Updating cart - Item IDs: " + itemIds + ", Quantities: " + quantities);
            
            for (int i = 0; i < itemIds.size(); i++) {
                cartService.updateCartItem(itemIds.get(i), quantities.get(i));
            }
            
            System.out.println("Cart updated successfully");
            return "redirect:/cart?success=cart_updated";
        } catch (Exception e) {
            System.err.println("Error updating cart: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/cart?error=update_failed";
        }
    }

    @PostMapping("/update-quantity")
    @ResponseBody
    public ResponseEntity<String> updateQuantity(@RequestParam("itemId") String itemId, 
                                                @RequestParam("quantity") int quantity, 
                                                Principal principal) {
        try {
            if (principal == null) {
                return ResponseEntity.status(401).body("User not authenticated");
            }
            
            cartService.updateQuantity(principal.getName(), UUID.fromString(itemId), quantity);
            return ResponseEntity.ok("Quantity updated successfully");
        } catch (Exception e) {
            System.err.println("Error updating quantity: " + e.getMessage());
            return ResponseEntity.badRequest().body("Failed to update quantity: " + e.getMessage());
        }
    }
}