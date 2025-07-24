package com.hsf.hsfproject.controller.installment;

import com.hsf.hsfproject.model.*;
import com.hsf.hsfproject.repository.InstalllmentTypeRepository;
import com.hsf.hsfproject.repository.InstallmentPlanRepository;
import com.hsf.hsfproject.repository.InstallmentRepository;
import com.hsf.hsfproject.repository.OrderRepository;
import com.hsf.hsfproject.service.cart.ICartService;
import com.hsf.hsfproject.service.installment.InstallmentService;
import com.hsf.hsfproject.service.user.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.hsf.hsfproject.constants.enums.InstallmentStatus;

import java.security.Principal;
import java.time.LocalDate;
import java.util.*;

@Controller
@RequestMapping("/installments")
@RequiredArgsConstructor
public class InstallmentController {
    @Autowired
    private final InstallmentService installmentService;
    @Autowired
    private final IUserService userService;
    @Autowired
    private final OrderRepository orderRepository;
    @Autowired
    private final InstalllmentTypeRepository installmentTypeRepository;
    @Autowired
    private final InstallmentPlanRepository installmentPlanRepository;
    @Autowired
    private final InstallmentRepository installmentRepository;
    @Autowired
    private final ICartService cartService;

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

    @GetMapping("/plan")
    public String showInstallmentPlan(Model model, Principal principal) {
        if (principal == null) return "redirect:/login";

        User user = userService.findByUsername(principal.getName());
        addUserInfo(model, principal);

        // Get user's active cart
        Cart cart = cartService.getCartByUserId(user.getId().toString());
        if (cart == null || cart.getCartItems().isEmpty()) {
            return "redirect:/cart";
        }

        // Create a temporary order object with cart total price for calculation
        Order tempOrder = new Order();
        tempOrder.setTotalPrice(cart.getTotalPrice());

        List<InstalllmentType> types = installmentTypeRepository.findAll();
        Collections.sort(types, Comparator.comparingInt(InstalllmentType::getMonths));
        Collections.sort(types, Comparator.comparingInt(InstalllmentType::getMonths));
        model.addAttribute("order", tempOrder);
        model.addAttribute("types", types);
        model.addAttribute("cart", cart);

        return "installment/plan";
    }

    @GetMapping
    public String userInstallments(Model model, Principal principal) {
        if (principal == null) return "redirect:/login";
        addUserInfo(model, principal);
        User user = userService.findByUsername(principal.getName());
        List<InstallmentPlan> plans = installmentService.getUserInstallmentPlans(user.getId().toString());
        model.addAttribute("plans", plans);
        return "installment/index";
    }

    @PostMapping("/create")
    public String createPlan(@RequestParam(value = "orderId", required = false) String orderId,
                             @RequestParam("typeId") String typeId,
                             @RequestParam(value = "cartId", required = false) String cartId,
                             Principal principal) {
        if (principal == null) return "redirect:/login";

        Order order = null;
        InstalllmentType type = installmentTypeRepository.findById(UUID.fromString(typeId))
                .orElseThrow(() -> new IllegalArgumentException("Type not found"));

        if (orderId != null && !orderId.isEmpty()) {
            // If order ID is provided, use it
            order = orderRepository.findById(UUID.fromString(orderId))
                    .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        } else if (cartId != null && !cartId.isEmpty()) {
            // If cart ID is provided, create an order from the cart
            User user = userService.findByUsername(principal.getName());
            Cart cart = cartService.getCartByUserId(user.getId().toString());

            // Create a new order based on the cart
            order = new Order();
            order.setUser(user);
            order.setOrderDate(new Date());
            order.setStatus("PENDING");
            order.setTotalPrice(cart.getTotalPrice());
            // Save the order to generate an ID
            order = orderRepository.save(order);
        } else {
            throw new IllegalArgumentException("Either orderId or cartId must be provided");
        }

        // Create the installment plan using the order and selected type
        installmentService.createInstallmentPlan(order, type);
        return "redirect:/installments";
    }

    @GetMapping("/view/{planId}")
    public String viewPlanPayments(@PathVariable UUID planId, Model model, Principal principal) {
        if (principal == null) return "redirect:/login";
        addUserInfo(model, principal);
        InstallmentPlan plan = installmentPlanRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("Plan not found"));
        if (plan.getInstallments() != null) {
            plan.getInstallments().sort(Comparator.comparing(Installment::getDueDate));
        }
        model.addAttribute("plan", plan);
        return "installment/payments";
    }

    @PostMapping("/pay")
    public String payInstallment(@RequestParam("installmentId") String installmentId, Principal principal) {
        if (principal == null) return "redirect:/login";
        Installment installment = installmentRepository.findById(UUID.fromString(installmentId))
                .orElseThrow(() -> new IllegalArgumentException("Installment not found"));
        installmentService.makeInstallmentPayment(installment.getId(), installment.getAmountDue(), "MANUAL");
        return "redirect:/installments/view/" + installment.getInstallmentPlan().getId();
    }

    @GetMapping("/payment/{id}")
    public String showPaymentPage(@PathVariable("id") String installmentId, Model model, Principal principal) {
        if (principal == null) return "redirect:/login";

        Installment installment = installmentRepository.findById(UUID.fromString(installmentId)).orElse(null);
        if (installment == null) return "redirect:/installments";

        InstallmentPlan plan = installment.getInstallmentPlan();
        Order order = plan != null ? plan.getOrder() : null;
        User user = userService.findByUsername(principal.getName());

        model.addAttribute("installment", installment);
        model.addAttribute("plan", plan);
        model.addAttribute("order", order);
        model.addAttribute("user", user);

        // Đảm bảo có thông tin sản phẩm để hiển thị
        if (order != null) {
            if (order.getOrderItems() != null && !order.getOrderItems().isEmpty()) {
                model.addAttribute("cartItems", order.getOrderItems());
            } else {
                // Nếu không có orderItems, tạo một item tạm thời để hiển thị
                List<Object> tempItems = new ArrayList<>();
                // Bạn có thể tạo một DTO để hiển thị thông tin cơ bản
                model.addAttribute("cartItems", tempItems);
            }
        }

        // Payment methods
        List<String> paymentMethods = Arrays.asList("MOMO", "VNPay", "Bank Transfer");
        model.addAttribute("paymentMethods", paymentMethods);

        addUserInfo(model, principal);
        return "installment/payment";
    }

    @PostMapping("/payment/place")
    public String placeOrder(@RequestParam("installmentId") String installmentId,
                             @RequestParam("paymentMethod") String paymentMethod) {
        Installment installment = installmentRepository.findById(UUID.fromString(installmentId)).orElse(null);
        if (installment != null && installment.getStatus() == InstallmentStatus.PENDING) {
            // Cập nhật trạng thái installment
            installment.setStatus(InstallmentStatus.PAID);
            installment.setPaidDate(LocalDate.now());
            installmentRepository.save(installment);

            // Có thể thêm logic xử lý thanh toán theo paymentMethod nếu cần
            // processPayment(installment, paymentMethod);
        }

        // Redirect về trang view plan thay vì /installments/payments
        if (installment != null && installment.getInstallmentPlan() != null) {
            return "redirect:/installments/view/" + installment.getInstallmentPlan().getId();
        }
        return "redirect:/installments";
    }
}
