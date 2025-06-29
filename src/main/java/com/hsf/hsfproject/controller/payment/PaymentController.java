package com.hsf.hsfproject.controller.payment;

import com.hsf.hsfproject.dtos.request.VnPayRequest;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/vn-pay")
public class PaymentController {
    @Autowired
    private com.hsf.hsfproject.service.payment.PaymentService paymentService;

    @PostMapping("/create-payment")
    public Map<String, Object> createPayment(HttpServletRequest request, @RequestBody VnPayRequest order) {
        Map<String, Object> result = new HashMap<>();
        try {
            String paymentUrl = paymentService.createPaymentUrl(request, order);
            result.put("code", "00");
            result.put("message", "success");
            result.put("data", paymentUrl);
            return result;
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
