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

import java.security.Principal;
import java.util.List;
import java.util.UUID;

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
    public String createPlan(@RequestParam("orderId") String orderId,
                             @RequestParam("typeId") String typeId,
                             Principal principal) {
        if (principal == null) return "redirect:/login";
        Order order = orderRepository.findById(UUID.fromString(orderId))
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        InstalllmentType type = installmentTypeRepository.findById(UUID.fromString(typeId))
                .orElseThrow(() -> new IllegalArgumentException("Type not found"));
        installmentService.createInstallmentPlan(order, type);
        return "redirect:/installments";
    }
    @GetMapping("/view/{planId}")
    public String viewPlanPayments(@PathVariable UUID planId, Model model, Principal principal) {
        if (principal == null) return "redirect:/login";
        addUserInfo(model, principal);
        InstallmentPlan plan = installmentPlanRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("Plan not found"));
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
}
