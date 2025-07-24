package com.hsf.hsfproject.controller.admin;

import com.hsf.hsfproject.model.Order;
import com.hsf.hsfproject.model.User;
import com.hsf.hsfproject.service.order.IOrderService;
import com.hsf.hsfproject.service.user.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;

@Controller
@RequestMapping("/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private final IOrderService orderService;
    private final IUserService userService;

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
    public String getOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model,
            Principal principal
    ) {
        addUserInfo(model, principal);
        Page<Order> orders = orderService.getOrderList(page, size);

        // Kiểm tra null và xử lý an toàn
        model.addAttribute("orders", orders);
        model.addAttribute("totalElements", orders != null ? orders.getTotalElements() : 0);
        model.addAttribute("totalPages", orders != null ? orders.getTotalPages() : 0);
        model.addAttribute("currentPage", page);

        // Đếm số đơn hàng theo trạng thái - xử lý an toàn với null
        int pendingCount = 0;
        int acceptedCount = 0;
        int completedCount = 0;
        int cancelledCount = 0;

        if (orders != null && orders.getContent() != null) {
            for (Order order : orders.getContent()) {
                if (order != null && order.getStatus() != null) {
                    switch(order.getStatus().toUpperCase()) {
                        case "PENDING":
                            pendingCount++;
                            break;
                        case "ACCEPTED":
                            acceptedCount++;
                            break;
                        case "COMPLETED":
                            completedCount++;
                            break;
                        case "CANCELLED":
                            cancelledCount++;
                            break;
                    }
                }
            }
        }

        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("acceptedCount", acceptedCount);
        model.addAttribute("completedCount", completedCount);
        model.addAttribute("cancelledCount", cancelledCount);

        return "admin/order";
    }
}
