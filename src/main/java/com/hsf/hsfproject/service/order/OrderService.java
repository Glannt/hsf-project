package com.hsf.hsfproject.service.order;

import com.hsf.hsfproject.constants.enums.OrderStatus;
import com.hsf.hsfproject.dtos.request.CreateOrderRequest;
import com.hsf.hsfproject.model.*;
import com.hsf.hsfproject.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final UserRepository userRepository;
    private final PCRepository pcRepository;
    private final ComputerItemRepository computerItemRepository;
    
    public Order createOrder(String username, CreateOrderRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Tạo order mới
        Order order = Order.builder()
                .orderNumber(generateOrderNumber())
                .status(OrderStatus.PENDING)
                .shippingAddress(request.getShippingAddress())
                .totalPrice(0.0)
                .orderDate(LocalDateTime.now())
                .updatedDate(LocalDateTime.now())
                .user(user)
                .build();

        order = orderRepository.save(order);

        double totalPrice = 0.0;
        
        // Tạo order details và tính tổng tiền
        for (CreateOrderRequest.OrderItemRequest item : request.getOrderItems()) {
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrder(order);
            orderDetail.setQuantity(item.getQuantity());
            
            if ("PC".equals(item.getProductType()) && item.getPcId() != null) {
                PC pc = pcRepository.findById(item.getPcId())
                        .orElseThrow(() -> new RuntimeException("PC not found"));
                
                orderDetail.setPc(pc);
                orderDetail.setProductName(pc.getName());
                orderDetail.setUnitPrice(pc.getPrice());
                orderDetail.setSubtotal(pc.getPrice() * item.getQuantity());
                
            } else if ("COMPUTER_ITEM".equals(item.getProductType()) && item.getComputerItemId() != null) {
                ComputerItem computerItem = computerItemRepository.findById(item.getComputerItemId())
                        .orElseThrow(() -> new RuntimeException("Computer item not found"));
                
                orderDetail.setComputerItem(computerItem);
                orderDetail.setProductName(computerItem.getName());
                orderDetail.setUnitPrice(computerItem.getPrice());
                orderDetail.setSubtotal(computerItem.getPrice() * item.getQuantity());
            } else {
                throw new RuntimeException("Invalid product type or product ID");
            }
            
            totalPrice += orderDetail.getSubtotal();
            orderDetailRepository.save(orderDetail);
        }
        
        // Cập nhật tổng tiền
        order.setTotalPrice(totalPrice);
        return orderRepository.save(order);
    }
    
    public Order updateOrderStatus(UUID orderId, OrderStatus status, String updatedBy) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        
        order.setStatus(status);
        order.setUpdatedDate(LocalDateTime.now());
        
        return orderRepository.save(order);
    }
    
    public Page<Order> getUserOrders(String username, Pageable pageable) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return orderRepository.findByUserOrderByOrderDateDesc(user, pageable);
    }
    
    public Page<Order> getAllOrders(Pageable pageable) {
        return orderRepository.findAllByOrderByOrderDateDesc(pageable);
        }
    
    public Order getOrderById(UUID orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
    }

    private String generateOrderNumber() {
        return "ORD-" + System.currentTimeMillis();
    }
}
