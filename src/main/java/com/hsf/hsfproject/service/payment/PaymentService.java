package com.hsf.hsfproject.service.payment;

import com.hsf.hsfproject.constants.enums.OrderStatus;
import com.hsf.hsfproject.dtos.request.OrderDto;
import com.hsf.hsfproject.model.Order;
import com.hsf.hsfproject.model.OrderDetail;
import com.hsf.hsfproject.service.order.IOrderService;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.hsf.hsfproject.configuration.VnPayConfig;
import com.hsf.hsfproject.dtos.request.VnPayRequest;

import org.springframework.beans.factory.annotation.Autowired;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private @Autowired VnPayConfig vnPayConfig;

    private final IOrderService orderService;


    public String createPaymentUrl(HttpServletRequest req, VnPayRequest order) throws UnsupportedEncodingException {
        String vnp_Version = "2.1.0";
        String vnp_Command = "pay";
        String orderType = "other";
        long amount = order.getAmount()* 100; // Convert to VND (assumed amount is in thousands)

        String vnp_TxnRef = VnPayConfig.getRandomNumber(8);
        String vnp_IpAddr = VnPayConfig.getIpAddress(req);

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", vnp_Version);
        vnp_Params.put("vnp_Command", vnp_Command);
        vnp_Params.put("vnp_TmnCode", vnPayConfig.getVnpTmnCode());
        vnp_Params.put("vnp_Amount", String.valueOf(amount));
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", "Thanh toan don hang:" + vnp_TxnRef);
        vnp_Params.put("vnp_OrderType", orderType);

        String locate = order.getLanguage();
        vnp_Params.put("vnp_Locale", (locate != null && !locate.isEmpty()) ? locate : "vn");
        vnp_Params.put("vnp_ReturnUrl", vnPayConfig.getVnpReturnUrl());
        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        cld.add(Calendar.MINUTE, 15);
        String vnp_ExpireDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();

        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = vnp_Params.get(fieldName);
            if (fieldValue != null && fieldValue.length() > 0) {
                // For hash: encode value
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                // For query: encode key and value
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()));
                query.append('=');
                query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                if (itr.hasNext()) {
                    hashData.append('&');
                    query.append('&');
                }
            }
        }
        String vnp_SecureHash = VnPayConfig.hmacSHA512(vnPayConfig.getSecretKey(), hashData.toString());
        query.append("&vnp_SecureHash=").append(vnp_SecureHash);
        return vnPayConfig.getVnpPayUrl() + "?" + query.toString();
    }

    public Session createCheckoutSession(OrderDto order) throws StripeException {
//        Order newOrder = new Order();
//        newOrder.setOrderNumber(order.getOrderNumber());
//        newOrder.setStatus(order.getStatus());
//        newOrder.setShippingAddress(order.getShippingAddress());
//        newOrder.setTotalPrice(order.getTotalPrice());
//        newOrder.setUser(order.getUser());
//        // Map by product name
//        Set<OrderDetail> orderItems = order.getOrderItemList().stream()
//                .map(item -> {
//                    OrderDetail orderDetail = new OrderDetail();
//                    orderDetail.setProductName(item.getProductName());
//                    orderDetail.setQuantity(item.getQuantity());
//                    orderDetail.setSubtotal(item.getSubtotal());
//                    return orderDetail;
//                })
//                .collect(Collectors.toSet());
//        newOrder.setOrderItems(orderItems);
        Order newOrder = Order.builder()
                .shippingAddress(order.getShippingAddress())
                .user(order.getUser())
                .status(OrderStatus.valueOf(order.getStatus()))
                .totalPrice(order.getTotalPrice())
                .orderNumber(order.getOrderNumber())
                .orderItems(order.getOrderItems().stream()
                        .map(item -> OrderDetail.builder()
                                .productName(item.getProductName())
                                .quantity(item.getQuantity())
                                .unitPrice(item.getUnitPrice())
                                .subtotal(item.getSubtotal())
                                .build())
                        .collect(Collectors.toSet()))
                .build();
        List<SessionCreateParams.LineItem> lineItems = newOrder.getOrderItems().stream()
                .map(item -> SessionCreateParams.LineItem.builder()
                        .setQuantity((long) item.getQuantity())
                        .setPriceData(
                                SessionCreateParams.LineItem.PriceData.builder()
                                        .setCurrency("vnd")
                                        .setUnitAmount(((Double) (item.getUnitPrice() * 100)).longValue()) // Multiply by 100 for Stripe minor units
                                        .setProductData(
                                                SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                        .setName(item.getProductName())
                                                        .build())
                                        .build())
                        .build())
                .toList();

        SessionCreateParams params = SessionCreateParams.builder()
                .addAllLineItem(lineItems)
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl("http://localhost:8080/checkout/success")
                .setCancelUrl("http://localhost:8080/checkout/cancel")
                .putMetadata("orderNumber", newOrder.getOrderNumber())
                .putMetadata("username", newOrder.getUser().getUsername())
                .build();

        return Session.create(params);
    }
}

