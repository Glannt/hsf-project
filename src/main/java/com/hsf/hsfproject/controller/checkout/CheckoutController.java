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
    private final IUserService userService;

    @Value("${stripe.public.key}")
    private String publicKey;

    @Value("${stripe.webhook.key}")
    private String stripeWebhookKey;

    

//    public CheckoutController(PaymentService stripeService) {
//        this.stripeService = stripeService;
//    }

    @PostMapping
    public String handleCheckout(@ModelAttribute("order") OrderDto order,
                                 @RequestParam("paymentMethod") String paymentMethod,
                                 Principal principal,
                                 HttpSession httpSession,
                                 RedirectAttributes redirectAttributes) throws StripeException {

        log.info("Processing checkout for payment method: {}", paymentMethod);

        // Get user information
        User user = userService.findByUsername(principal.getName());
        order.setUser(user);

        // Validate shipping address
        if (order.getShippingAddress() == null || order.getShippingAddress().trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Shipping address is required!");
            return "redirect:/order";
        }

        // Find the existing order from database
        Order existingOrder = orderService.findOrderByOrderNumber(order.getOrderNumber());
        if (existingOrder == null) {
            log.error("Order not found: {}", order.getOrderNumber());
            redirectAttributes.addFlashAttribute("error", "Order not found!");
            return "redirect:/checkout/error";
        }

        // Update the existing order with shipping address
        existingOrder.setShippingAddress(order.getShippingAddress());
        orderService.saveOrder(existingOrder);

        log.info("Processing payment method: {} for order: {}", paymentMethod, order.getOrderNumber());

        switch (paymentMethod.toLowerCase()) {
            case "stripe":
                return handleStripePayment(order, httpSession, redirectAttributes);

            case "cod":
                return handleCODPayment(existingOrder, order, httpSession, redirectAttributes);

            default:
                log.warn("Invalid payment method: {}", paymentMethod);
                redirectAttributes.addFlashAttribute("error", "Invalid payment method selected!");
                return "redirect:/checkout/error";
        }
    }

    private String handleStripePayment(OrderDto order, HttpSession httpSession, RedirectAttributes redirectAttributes) {
        try {
            log.info("Creating Stripe checkout session for order: {}", order.getOrderNumber());

            // Create Stripe session using the existing order
            Session session = stripeService.createCheckoutSession(order);
            httpSession.setAttribute("orderDto", order);

            log.info("Stripe session created successfully. Redirecting to: {}", session.getUrl());
            return "redirect:" + session.getUrl();

        } catch (StripeException e) {
            log.error("Stripe payment failed for order: {}", order.getOrderNumber(), e);
            redirectAttributes.addFlashAttribute("error", "Payment processing failed. Please try again.");
            return "redirect:/checkout/error";
        }
    }

    private String handleCODPayment(Order existingOrder, OrderDto orderDto, HttpSession httpSession, RedirectAttributes redirectAttributes) {
        try {
            log.info("Processing COD payment for order: {}", existingOrder.getOrderNumber());

            // Accept the order with COD payment method
            String transactionRef = "COD_" + System.currentTimeMillis();
            orderService.acceptOrder(existingOrder, existingOrder.getShippingAddress(), transactionRef);

            // Store order info in session for success page
            httpSession.setAttribute("orderDto", orderDto);

            log.info("COD order processed successfully: {}", existingOrder.getOrderNumber());
            redirectAttributes.addFlashAttribute("message", "Order placed successfully! You will pay upon delivery.");
            return "redirect:/checkout/success";

        } catch (Exception e) {
            log.error("COD payment processing failed for order: {}", existingOrder.getOrderNumber(), e);
            redirectAttributes.addFlashAttribute("error", "Order processing failed. Please try again.");
            return "redirect:/checkout/error";
        }
    }

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

    @GetMapping("/success")
    public String checkoutSuccess(HttpSession session, Model model, Principal principal) {
        addUserInfo(model, principal);
        OrderDto orderDto = (OrderDto) session.getAttribute("orderDto");
        if (orderDto == null) {
            model.addAttribute("message", "Không tìm thấy thông tin đơn hàng trong phiên làm việc. Vui lòng thử lại!");
            return "error";
        }
        Order order = orderService.findOrderByOrderNumber(orderDto.getOrderNumber());
        if (order != null) {
            model.addAttribute("order", order);
            session.removeAttribute("orderDto"); // Xóa sau khi dùng để tránh lặp lại
        }
        return "checkout/success";
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
                String stripeTransactionId = session.getPaymentIntent();
                // Lấy đơn hàng theo orderNumber
                Order order = orderService.findOrderByOrderNumber(orderNumber);

                if (order != null) {
                    log.info("Order found: {}", order);
                    orderService.acceptOrder(order, order.getShippingAddress(), stripeTransactionId);
                }
            }
        }

        return ResponseEntity.ok("Webhook received");
    }
}
