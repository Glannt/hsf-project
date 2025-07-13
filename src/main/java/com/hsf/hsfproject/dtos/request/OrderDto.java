package com.hsf.hsfproject.dtos.request;

import com.hsf.hsfproject.model.OrderDetail;
import com.hsf.hsfproject.model.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderDto {
    private String orderNumber;
    private String status;
    private String shippingAddress;
    private Double totalPrice;

    private User user; // nested DTO cho user
    private List<OrderDetail> orderItems = new ArrayList<>();
}
