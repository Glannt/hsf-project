package com.hsf.hsfproject.controller.cart;

import com.hsf.hsfproject.dtos.request.CartItemRequest;
import com.hsf.hsfproject.model.Cart;
import com.hsf.hsfproject.model.User;
import com.hsf.hsfproject.service.cart.ICartService;
import com.hsf.hsfproject.service.order.IOrderService;
import com.hsf.hsfproject.service.user.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("")
@RequiredArgsConstructor
public class CartController {

    private final ICartService cartService;
    private final IUserService userService;


    // Helper method to add user info to the model
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

    @GetMapping("cart")
    public String getCartPage(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login"; // Nếu chưa đăng nhập thì chuyển về login
        }
        addUserInfo(model, principal);
        System.out.println("Logged in username: " + principal.getName());
        User user = userService.findByUsername(principal.getName());
        Cart cart = cartService.getCartByUserId(user.getId().toString());

        if (model.containsAttribute("cart")) {
            model.asMap().remove("cart");
        }


        model.addAttribute("user", user);
        model.addAttribute("cart", cart);
        return "cart/index";
    }


    @PostMapping("/user/cart/add")
    public String addToCart(@RequestParam("id") String productId,
                            @RequestParam("cartId") String cartId,
                            @RequestParam("quantity") int quantity,
                            @RequestHeader(value = "Referer", required = false) String referer
    ) {

        CartItemRequest request = CartItemRequest.builder()
                .cartId(cartId)
                .productId(productId)
                .quantity(quantity)
                .build();
        cartService.addCartItemToCart(request);
        return "redirect:" + (referer != null ? referer : "/");
    }

    @PostMapping("user/cart/remove")
    public String removeCartItem(@RequestParam("cartItemId") String cartItemId) {
        System.out.println("Removing cart item with ID: " + cartItemId);
        cartService.removeCartItemById(cartItemId);
        return "redirect:/cart"; // Sau khi xóa, quay lại trang giỏ hàng
    }

    @PostMapping("user/cart/update")
    public String updateCartItem(@RequestParam("itemIds") List<String> itemIds,
                                 @RequestParam("quantities") List<Integer> quantities) {
        for (int i = 0; i < itemIds.size(); i++) {
            cartService.updateCartItem(itemIds.get(i), quantities.get(i));
        }
        return "redirect:/cart"; // Sau khi cập nhật, quay lại trang giỏ hàng
    }
}