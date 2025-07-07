package com.hsf.hsfproject.controller.checkout;

import com.hsf.hsfproject.dtos.request.OrderDto;
import com.hsf.hsfproject.model.Order;
import com.hsf.hsfproject.model.User;
import com.hsf.hsfproject.service.order.OrderService;
import com.hsf.hsfproject.service.payment.PaymentService;
import com.hsf.hsfproject.service.user.IUserService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

@Controller
@RequestMapping("/checkout")
@RequiredArgsConstructor
public class CheckoutController {
    private static final Logger log = LoggerFactory.getLogger(CheckoutController.class);
    private final PaymentService stripeService;
    private final OrderService orderService;

    @Value("${stripe.public.key}")
    private String publicKey;

    @Value("${stripe.webhook.key}")
    private String stripeWebhookKey;

    private final IUserService userService;

//    public CheckoutController(PaymentService stripeService) {
//        this.stripeService = stripeService;
//    }

    @PostMapping
    public String handleCheckout(@ModelAttribute("order") OrderDto order,
                                 @RequestParam("paymentMethod") String paymentMethod,
                                 Principal principal,
                                 RedirectAttributes redirectAttributes) throws StripeException {
        // Kiểm tra thông tin đơn hàngc
        User user = userService.findByUsername(principal.getName());
        order.setUser(user);
        System.out.println("Order details: " + order);
        switch (paymentMethod.toLowerCase()) {
            case "stripe":
                Session session = stripeService.createCheckoutSession(order);
                return "redirect:" + session.getUrl();
            case "cod":
            case "bank":
            case "paypal":
            case "check":
//                orderService.save(order); // xử lý đơn hàng nội bộ
//                orderService.acceptOrder(order, paymentMethod);
                redirectAttributes.addFlashAttribute("message", "Đơn hàng đã được đặt thành công!");
                return "redirect:/checkout/success";
            default:
                redirectAttributes.addFlashAttribute("error", "Phương thức thanh toán không hợp lệ!");
                return "redirect:/checkout";
        }
    }

    @GetMapping("/success")
    public String checkoutSuccess() {
        return "checkout/success"; // HTML: thông báo thanh toán thành công
    }

    @GetMapping("/cancel")
    public String checkoutCancel() {
        return "redirect:cart/index"; // HTML: giao dịch bị hủy
    }

    @PostMapping("/webhook/stripe")
    public ResponseEntity<String> handleStripeWebhook(HttpServletRequest request) {
        String payload;
        String sigHeader = request.getHeader("Stripe-Signature");
        Event event;

        try (Scanner s = new Scanner(request.getInputStream(), "UTF-8").useDelimiter("\\A")) {
            payload = s.hasNext() ? s.next() : "";
        } catch (IOException e) {
            return ResponseEntity.badRequest().body("Invalid payload");
        }

        try {
            event = Webhook.constructEvent(
                    payload, sigHeader, stripeWebhookKey // lấy từ Stripe Dashboard
            );
        } catch (SignatureVerificationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Signature verification failed");
        }

        // Kiểm tra loại sự kiện
        if ("checkout.session.completed".equals(event.getType())) {
            Session session = (Session) event.getDataObjectDeserializer()
                    .getObject().orElse(null);

            if (session != null) {
                String orderNumber = session.getMetadata().get("orderNumber");
                String username = session.getMetadata().get("username");
                System.out.println("Order Number: " + orderNumber);
                System.out.println("Username: " + username);
                // Lấy đơn hàng theo orderNumber
                Order order = orderService.findOrderByOrderNumber(orderNumber);

                if (order != null) {
                    log.info("Order found: {}", order);
                    orderService.acceptOrder(order, "STRIPE");
                }
            }
        }

        return ResponseEntity.ok("Webhook received");
}
}
