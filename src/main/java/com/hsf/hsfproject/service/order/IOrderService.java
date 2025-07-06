package com.hsf.hsfproject.service.order;

import com.hsf.hsfproject.dtos.request.OrderRequest;
import com.hsf.hsfproject.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IOrderService {

    // Define methods for order management here
    // For example:
    Order createOrder(OrderRequest request);

    Order getOrderById(String orderId);

    List<Order> getOrdersByUserId(String userId);
    
    Page<Order> getOrdersByUserId(String userId, Pageable pageable);

    void cancelOrder(String orderId);

    List<Order> getAllOrders();

    Order acceptOrder(Order order, String shippingAddress);

    Order saveOrder(Order order);
}
