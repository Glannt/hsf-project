package com.hsf.hsfproject.service.order;

import com.hsf.hsfproject.dtos.request.OrderDto;
import com.hsf.hsfproject.dtos.request.OrderRequest;
import com.hsf.hsfproject.mapper.Mapper;
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
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.HashSet;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService implements IOrderService {

    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final UserRepository userRepository;
    private final PCRepository pcRepository;
    private final ComputerItemRepository computerItemRepository;

    // Method for creating order with username and CreateOrderRequest (new signature)
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
                .orderItems(new HashSet<>())
                .build();

        // Lưu order trước
        order = orderRepository.save(order);

        // Tạo order details và tính tổng tiền
        double totalPrice = 0.0;
        Set<OrderDetail> orderDetails = new HashSet<>();

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
            orderDetail = orderDetailRepository.save(orderDetail);
            orderDetails.add(orderDetail);
        }

        // Cập nhật tổng tiền và order items
        order.setTotalPrice(totalPrice);
        order.setOrderItems(orderDetails);
        order = orderRepository.save(order);

        return order;
    }

    @Override
    public Order createOrder(OrderRequest request) {
        // Implementation for interface method - delegate to new method
        return createOrder(request.getUserId(), new CreateOrderRequest());
    }

    @Override
    public Order getOrderById(String orderId) {
        try {
            UUID uuid = UUID.fromString(orderId);
            return orderRepository.findById(uuid)
                    .orElseThrow(() -> new RuntimeException("Order not found"));
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid order ID format");
        }
    }

    @Override
    public List<Order> getOrdersByUserId(String userId) {
        // Implementation for getting orders by user ID
        return orderRepository.findByUserOrderByOrderDateDesc(
                userRepository.findById(UUID.fromString(userId))
                        .orElseThrow(() -> new RuntimeException("User not found")),
                Pageable.unpaged()
        ).getContent();
    }

    @Override
    public void cancelOrder(String orderId) {
        Order order = getOrderById(orderId);
        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
    }

    @Override
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @Override
    @Transactional
    public Order acceptOrder(Order order, String shippingAddress, String stripeTransactionId) {
        order.setStatus(OrderStatus.CONFIRMED);
        order.setShippingAddress(shippingAddress);
        orderRepository.save(order);
        
        // Clear cart if exists
        if (order.getUser().getCart() != null) {
            Cart cart = order.getUser().getCart();
            cart.getCartItems().clear();
            cart.setTotalPrice(0d);
            cart.setItemCount(0);
            // Note: You need to inject CartRepository if you want to save cart
        }
        
        return order;
    }

    @Override
    public Order saveOrder(Order order) {
        return orderRepository.save(order);
    }

    @Override
    public Order findOrderByOrderNumber(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new IllegalArgumentException("Order not found with order number: " + orderNumber));
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
