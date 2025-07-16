package com.hsf.hsfproject.controller.order;

import com.hsf.hsfproject.dtos.request.OrderRequest;
import com.hsf.hsfproject.dtos.response.CartItemResponse;
import com.hsf.hsfproject.model.Cart;
import com.hsf.hsfproject.model.Order;
import com.hsf.hsfproject.model.User;
import com.hsf.hsfproject.service.order.IOrderService;
import com.hsf.hsfproject.service.user.IUserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class OrderController {

    private final IUserService userService;
    private final IOrderService orderService;

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

    // Display all orders for the user
    @GetMapping("/orders")
    public String getOrders(Model model, Principal principal) {
        addUserInfo(model, principal);
        // TODO: Add orders to model if needed
        return "order/index";
    }

    // Show order confirmation page
    @GetMapping("/confirmation")
    public String getConfirmationPage(Model model, Principal principal, HttpSession session) {
        addUserInfo(model, principal);

        Order order = (Order) session.getAttribute("pendingOrder");

        if (order == null) {
            // 👉 test tạm 1 UUID trong DB (ví dụ UUID có thật)
            UUID fixedId = UUID.fromString("9f5e9012-aea9-4fbc-8c30-b1e6f1a0a342");
            order = orderService.findById(fixedId); // hoặc orderRepository.findById(fixedId).orElse(null);
            session.setAttribute("pendingOrder", order);
        }

        if (order == null) {
            model.addAttribute("order", null);
            return "order/confirmation";
        }

        model.addAttribute("order", order);
        return "order/confirmation";
    }



    // Create a new order and store it in session
    @PostMapping("/orders")
    public String createOrder(@RequestParam("cartId") String cartId,
                              @RequestParam("userId") String userId,
                              HttpSession session,
                              Principal principal,
                              Model model) {
        OrderRequest request = OrderRequest.builder()
                .cartId(cartId)
                .userId(userId)
                .build();
        Order order = orderService.createOrder(request);
        if (order == null) {
            model.addAttribute("message", "Tạo đơn hàng thất bại. Vui lòng thử lại sau!");
            return "error";
        }
        session.setAttribute("pendingOrder", order);
        model.addAttribute("order", order);
        addUserInfo(model, principal);
        return "order/index";
    }

    // Confirm the order and redirect to confirmation page
//    @PostMapping("/order/confirm")
//    public String confirmOrder(@RequestParam("shippingAddress") String shippingAddress,
//                               HttpSession session,
//                               Model model) {
//        Order order = (Order) session.getAttribute("pendingOrder");
//        if (order == null) return "redirect:/cart";
//        Order confirmedOrder = orderService.acceptOrder(order, shippingAddress);
//        if (confirmedOrder == null) {
//            model.addAttribute("error", "Failed to confirm order. Please try again.");
//            return "order/index";
//        }
//        session.removeAttribute("pendingOrder");
//        // No need to add order to model since redirect
//        return "redirect:/order/confirmation";
//    }

}
