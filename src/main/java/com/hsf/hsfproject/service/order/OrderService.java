package com.hsf.hsfproject.service.order;

import com.hsf.hsfproject.dtos.request.OrderDto;
import com.hsf.hsfproject.dtos.request.OrderRequest;
import com.hsf.hsfproject.mapper.Mapper;
import com.hsf.hsfproject.model.*;
import com.hsf.hsfproject.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService implements IOrderService {
    private static final String ORDER_NUMBER_PREFIX = "ORD-";

    private final OrderRepository orderRepository; // Assuming you have an OrderRepository for database operations
    private final OrderDetailRepository orderDetailRepository;
    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final Mapper mapper; // Assuming you have a Mapper class for converting between entities
    private final TransactionRepository transactionRepository;

    // Implement the methods defined in IOrderService interface
    @Override
    public Order createOrder(OrderRequest request) {
        User user = userRepository.findById(UUID.fromString(request.getUserId()))
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Cart cart = cartRepository.findById(UUID.fromString(request.getCartId()))
                .orElseThrow(() -> new IllegalArgumentException("Cart not found"));

        Set<OrderDetail> orderDetails = mapper.maptoOrderDetail(cart.getCartItems());

        Order order = Order.builder()
                .user(user)
                .orderItems(orderDetails)
                .status("PENDING")
                .totalPrice(cart.getTotalPrice())
                .orderNumber(ORDER_NUMBER_PREFIX + System.currentTimeMillis())
                .build();

//        Order savedOrder = orderRepository.save(order);

        // Optionally save order details if they require a reference to the saved order
//        orderDetails.forEach(detail -> {
//            detail.setOrder(savedOrder);
//            orderDetailRepository.save(detail);
//        });
        orderRepository.save(order);

        return order;
    }

    @Override
    public Order getOrderById(String orderId) {
//        Order order = orderRepository.fin
        return null; // Replace with actual implementation
    }

    @Override
    public List<Order> getOrdersByUserId(String userId) {
        // Logic to retrieve orders by user ID
        return null; // Replace with actual implementation
    }

    @Override
    public void cancelOrder(String orderId) {
        // Logic to cancel an order
    }

    @Override
    public List<Order> getAllOrders() {
        // Logic to retrieve all orders
        return null; // Replace with actual implementation
    }

    @Override
    public Order acceptOrder(Order order, String shippingAddress) {
//        Order existingOrder = orderRepository.findOrderByOrderNumber(order.getOrderNumber())
//                .orElseThrow(() -> new IllegalArgumentException("Order not found with order number: " + order.getOrderNumber()));
//        Order newOrder = Order.builder()
//                .shippingAddress(order.getShippingAddress())
//                .user(order.getUser())
//                .status(order.getStatus())
//                .totalPrice(order.getTotalPrice())
//                .orderNumber(order.getOrderNumber())
//                .orderItems(order.getOrderItems().stream()
//                        .map(item -> OrderDetail.builder()
//                                .productName(item.getProductName())
//                                .quantity(item.getQuantity())
//                                .subtotal(item.getSubtotal())
//                                .build())
//                        .collect(Collectors.toSet()))
//                .build();


        order.setStatus("ACCEPTED");
        order.setShippingAddress(shippingAddress);
        orderRepository.save(order);
        order.getOrderItems().forEach(detail -> {
            detail.setOrder(order);
            orderDetailRepository.save(detail);
        });
        Cart cart = order.getUser().getCart();
        if (cart != null) {
            cart.getCartItems().clear();
            cart.setTotalPrice(0d); // Optionally reset total price
            cart.setItemCount(0);
            cartRepository.save(cart);
        }
        Transaction transaction = Transaction.builder()
                .order(order)
                .totalAmount(order.getTotalPrice())
                .transactionDate(String.valueOf(System.currentTimeMillis()))
                .status("PAYED")
                .paymentMethod("CASH_ON_DELIVERY") // Assuming a default payment method
                .build();

        transactionRepository.save(transaction);

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
}
