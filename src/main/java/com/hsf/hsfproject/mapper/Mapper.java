package com.hsf.hsfproject.mapper;

import com.hsf.hsfproject.model.CartItem;
import com.hsf.hsfproject.model.OrderDetail;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;


@Component
public class Mapper {
    public Set<OrderDetail> maptoOrderDetail(Set<CartItem> request) {
        return request.stream()
                .map(cartItem -> OrderDetail.builder()
                        .productName(cartItem.getProductName())
                        .computerItem(cartItem.getComputerItem())
                        .pc(cartItem.getPc())
                        .quantity(cartItem.getQuantity())
                        .unitPrice(cartItem.getUnitPrice())
                        .subtotal(cartItem.getSubtotal())
                        .build())
                .collect(Collectors.toSet());
    }
}
