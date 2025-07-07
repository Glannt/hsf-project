package com.hsf.hsfproject.controller.payment;
import com.hsf.hsfproject.constants.enums.OrderStatus;
import com.hsf.hsfproject.constants.enums.PaymentMethod;
import com.hsf.hsfproject.constants.enums.TransactionStatus;
import com.hsf.hsfproject.dtos.request.VnPayRequest;
import com.hsf.hsfproject.model.Order;
import com.hsf.hsfproject.model.Transaction;
import com.hsf.hsfproject.service.order.IOrderService;
import com.hsf.hsfproject.service.payment.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final IOrderService orderService;

    @PostMapping("/payment/process")
    public String processPayment(@RequestParam("paymentMethod") String paymentMethod,
                                 @RequestParam("shippingAddress") String shippingAddress,
                                 HttpSession session,
                                 Model model) {
        Order order = (Order) session.getAttribute("pendingOrder");
        if (order == null) {
            return "redirect:/cart";
        }

        order.setShippingAddress(shippingAddress);

        if (PaymentMethod.CASH_ON_DELIVERY.getCode().equals(paymentMethod)) {
            return processCODPayment(order, session, model);
        } else if (PaymentMethod.VNPAY.getCode().equals(paymentMethod)) {
            return processVNPayPayment(order, session, model);
        } else {
            model.addAttribute("error", "Invalid payment method selected");
            return "order/index";
        }
    }

    private String processCODPayment(Order order, HttpSession session, Model model) {
        try {
            // Update order status
            order.setStatus(OrderStatus.CONFIRMED.getCode());
            Order savedOrder = orderService.saveOrder(order);

            // Create transaction for COD with COMPLETED status (since no online payment is required)
            Transaction transaction = paymentService.createTransaction(savedOrder, PaymentMethod.CASH_ON_DELIVERY);
            // For COD, set transaction status to COMPLETED immediately as payment will be collected on delivery
            paymentService.updateTransactionStatus(savedOrder, TransactionStatus.COMPLETED, "COD-" + System.currentTimeMillis());

            // Clear cart
            paymentService.clearUserCart(savedOrder.getUser());

            session.removeAttribute("pendingOrder");
            session.setAttribute("completedOrder", savedOrder);

            return "redirect:/payment/success";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to process COD payment: " + e.getMessage());
            return "order/index";
        }
    }

    private String processVNPayPayment(Order order, HttpSession session, Model model) {
        try {
            // Save order first
            order.setStatus(OrderStatus.PENDING.getCode());
            Order savedOrder = orderService.saveOrder(order);

            // Create pending transaction
            paymentService.createTransaction(savedOrder, PaymentMethod.VNPAY);

            session.setAttribute("pendingVNPayOrder", savedOrder);
            session.removeAttribute("pendingOrder");

            return "redirect:/payment/vnpay/redirect?orderId=" + savedOrder.getId();
        } catch (Exception e) {
            model.addAttribute("error", "Failed to initiate VNPay payment: " + e.getMessage());
            return "order/index";
        }
    }

    @GetMapping("/payment/vnpay/redirect")
    public String redirectToVNPay(@RequestParam("orderId") String orderId,
                                  HttpServletRequest request,
                                  HttpSession session) {
        try {
            Order order = orderService.getOrderById(orderId);
            if (order == null) {
                return "redirect:/cart";
            }

            VnPayRequest vnPayRequest = VnPayRequest.builder()
                    .amount((long) order.getTotalPrice())
                    .orderId(order.getId().toString())
                    .orderInfo("Thanh toan don hang: " + order.getOrderNumber())
                    .language("vn")
                    .userId(order.getUser().getId().toString())
                    .shippingAddress(order.getShippingAddress())
                    .build();

            String paymentUrl = paymentService.createPaymentUrl(request, vnPayRequest);
            return "redirect:" + paymentUrl;
        } catch (UnsupportedEncodingException e) {
            return "redirect:/payment/error";
        }
    }

    @GetMapping("/payment/vnpay/return")
    public String vnpayReturn(HttpServletRequest request, HttpSession session, Model model) {
        try {
            // Extract all parameters from VNPay response
            Map<String, String> params = new HashMap<>();
            Enumeration<String> paramNames = request.getParameterNames();
            while (paramNames.hasMoreElements()) {
                String fieldName = paramNames.nextElement();
                String fieldValue = request.getParameter(fieldName);
                if (fieldValue != null && fieldValue.length() > 0) {
                    params.put(fieldName, fieldValue);
                }
            }

            // Validate VNPay response signature
            boolean isValidSignature = paymentService.validateVNPayResponse(params);
            String responseCode = params.get("vnp_ResponseCode");
            String txnRef = params.get("vnp_TxnRef");
            String orderInfo = params.get("vnp_OrderInfo");
            String amount = params.get("vnp_Amount");
            String payDate = params.get("vnp_PayDate");
            String transactionNo = params.get("vnp_TransactionNo");

            // Get pending order from session
            Order order = (Order) session.getAttribute("pendingVNPayOrder");

            if (isValidSignature) {
                if ("00".equals(responseCode)) {
                    // Payment successful
                    if (order != null) {
                        order.setStatus(OrderStatus.CONFIRMED.getCode());
                        orderService.saveOrder(order);

                        // Update transaction status
                        paymentService.updateTransactionStatus(order, TransactionStatus.COMPLETED, transactionNo);

                        // Clear cart
                        paymentService.clearUserCart(order.getUser());

                        // Add payment details to model
                        model.addAttribute("order", order);
                        model.addAttribute("orderInfo", orderInfo);
                        model.addAttribute("totalPrice", amount);
                        model.addAttribute("paymentTime", payDate);
                        model.addAttribute("transactionId", transactionNo);

                        session.removeAttribute("pendingVNPayOrder");
                        session.setAttribute("completedOrder", order);

                        return "redirect:/payment/success";
                    } else {
                        // Order not found in session, try to find by transaction reference
                        try {
                            Optional<Transaction> transactionOpt = paymentService.findTransactionByTxnRef(txnRef);
                            if (transactionOpt.isPresent()) {
                                Transaction transaction = transactionOpt.get();
                                order = transaction.getOrder();
                                order.setStatus(OrderStatus.CONFIRMED.getCode());
                                orderService.saveOrder(order);

                                // Update transaction status
                                paymentService.updateTransactionStatus(order, TransactionStatus.COMPLETED, transactionNo);

                                // Clear cart
                                paymentService.clearUserCart(order.getUser());

                                session.setAttribute("completedOrder", order);
                                return "redirect:/payment/success";
                            }
                        } catch (Exception e) {
                            // Log error but continue to show error page
                        }
                        model.addAttribute("message", "Order not found. Payment was successful but order could not be processed.");
                        return "redirect:/payment/error";
                    }
                } else {
                    // Payment failed with specific error code
                    if (order != null) {
                        // Use rollback mechanism for failed payments
                        paymentService.rollbackFailedVNPayTransaction(txnRef);
                        session.removeAttribute("pendingVNPayOrder");
                    }

                    // Add error details to model with specific error messages
                    model.addAttribute("errorCode", responseCode);
                    model.addAttribute("errorMessage", getVNPayErrorMessage(responseCode));
                    model.addAttribute("orderInfo", orderInfo);

                    return "redirect:/payment/failed";
                }
            } else {
                // Invalid signature - possible tampering
                if (order != null) {
                    // Use rollback mechanism for invalid signature
                    paymentService.rollbackFailedVNPayTransaction(txnRef);
                    session.removeAttribute("pendingVNPayOrder");
                }

                model.addAttribute("message", "Invalid payment response signature. This may indicate tampering with the payment data.");
                return "redirect:/payment/error";
            }
        } catch (Exception e) {
            model.addAttribute("message", "Error processing payment response: " + e.getMessage());
            return "redirect:/payment/error";
        }
    }

    @GetMapping("/payment/success")
    public String paymentSuccess(HttpSession session, Model model, Principal principal) {
        Order order = (Order) session.getAttribute("completedOrder");
        if (order != null) {
            model.addAttribute("order", order);
            session.removeAttribute("completedOrder");
        }

        // Add login status to model
        model.addAttribute("isLogin", principal != null);

        return "payment/success";
    }

    @GetMapping("/payment/failed")
    public String paymentFailed(Model model, Principal principal) {
        model.addAttribute("message", "Payment was not successful. Please try again.");
        model.addAttribute("isLogin", principal != null);
        return "payment/failed";
    }

    @GetMapping("/payment/error")
    public String paymentError(Model model, Principal principal) {
        model.addAttribute("message", "An error occurred during payment processing.");
        model.addAttribute("isLogin", principal != null);
        return "payment/error";
    }

    // VNPay IPN (Instant Payment Notification) endpoint
    @PostMapping("/payment/vnpay/ipn")
    @ResponseBody
    public Map<String, String> vnpayIPN(HttpServletRequest request) {
        Map<String, String> result = new HashMap<>();
        try {
            // Extract all parameters from VNPay IPN request
            Map<String, String> fields = new HashMap<>();
            Enumeration<String> paramNames = request.getParameterNames();
            while (paramNames.hasMoreElements()) {
                String fieldName = paramNames.nextElement();
                String fieldValue = request.getParameter(fieldName);
                if (fieldValue != null && fieldValue.length() > 0) {
                    fields.put(fieldName, fieldValue);
                }
            }

            String vnp_SecureHash = request.getParameter("vnp_SecureHash");
            if (fields.containsKey("vnp_SecureHashType")) {
                fields.remove("vnp_SecureHashType");
            }
            if (fields.containsKey("vnp_SecureHash")) {
                fields.remove("vnp_SecureHash");
            }

            // Check checksum
            boolean isValidSignature = paymentService.validateVNPayResponse(fields);
            if (isValidSignature && vnp_SecureHash != null) {
                String vnp_TxnRef = request.getParameter("vnp_TxnRef");
                String vnp_Amount = request.getParameter("vnp_Amount");
                String vnp_ResponseCode = request.getParameter("vnp_ResponseCode");
                String vnp_TransactionNo = request.getParameter("vnp_TransactionNo");

                // Check if order exists (vnp_TxnRef)
                boolean checkOrderId = paymentService.checkOrderExists(vnp_TxnRef);

                if (checkOrderId) {
                    // Check amount validity
                    boolean checkAmount = paymentService.validateAmount(vnp_TxnRef, vnp_Amount);

                    if (checkAmount) {
                        // Check if order status is pending (not already processed)
                        boolean checkOrderStatus = paymentService.isOrderPending(vnp_TxnRef);

                        if (checkOrderStatus) {
                            if ("00".equals(vnp_ResponseCode)) {
                                // Payment successful - update status to completed
                                paymentService.updateTransactionStatusByTxnRef(vnp_TxnRef, TransactionStatus.COMPLETED, vnp_TransactionNo);
                                paymentService.updateOrderStatusByTxnRef(vnp_TxnRef, OrderStatus.CONFIRMED);
                            } else {
                                // Payment failed - update status to failed
                                paymentService.updateTransactionStatusByTxnRef(vnp_TxnRef, TransactionStatus.FAILED, vnp_TransactionNo);
                                paymentService.updateOrderStatusByTxnRef(vnp_TxnRef, OrderStatus.CANCELLED);
                            }
                            result.put("RspCode", "00");
                            result.put("Message", "Confirm Success");
                        } else {
                            result.put("RspCode", "02");
                            result.put("Message", "Order already confirmed");
                        }
                    } else {
                        result.put("RspCode", "04");
                        result.put("Message", "Invalid Amount");
                    }
                } else {
                    result.put("RspCode", "01");
                    result.put("Message", "Order not Found");
                }
            } else {
                result.put("RspCode", "97");
                result.put("Message", "Invalid Checksum");
            }
        } catch (Exception e) {
            result.put("RspCode", "99");
            result.put("Message", "Unknown error");
        }
        return result;
    }

    // API endpoint for AJAX calls
    @PostMapping("/api/payment/vnpay/create")
    @ResponseBody
    public Map<String, Object> createVNPayPayment(HttpServletRequest request, @RequestBody VnPayRequest vnPayRequest) {
        Map<String, Object> result = new HashMap<>();
        try {
            String paymentUrl = paymentService.createPaymentUrl(request, vnPayRequest);
            result.put("code", "00");
            result.put("message", "success");
            result.put("data", paymentUrl);
            return result;
        } catch (UnsupportedEncodingException e) {
            result.put("code", "99");
            result.put("message", "Error creating payment URL");
            return result;
        }
    }

    // Order cancellation endpoint
    @PostMapping("/order/cancel/{orderId}")
    @ResponseBody
    public Map<String, Object> cancelOrder(@PathVariable String orderId,
                                           @RequestParam(required = false) String reason,
                                           HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        try {
            UUID orderUuid = UUID.fromString(orderId);

            // Check if user can cancel this order
            if (!paymentService.canCancelOrder(orderUuid)) {
                result.put("success", false);
                result.put("message", "This order cannot be cancelled. It may have already been processed or shipped.");
                return result;
            }

            // Cancel the order
            boolean cancelled = paymentService.cancelOrder(orderUuid, reason != null ? reason : "User requested cancellation");

            if (cancelled) {
                // Optionally restore cart items
                paymentService.restoreCartFromCancelledOrder(orderUuid);

                result.put("success", true);
                result.put("message", "Order has been successfully cancelled.");
            } else {
                result.put("success", false);
                result.put("message", "Failed to cancel the order. Please try again or contact support.");
            }

        } catch (IllegalArgumentException e) {
            result.put("success", false);
            result.put("message", "Invalid order ID.");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "An error occurred while cancelling the order: " + e.getMessage());
        }

        return result;
    }

    /**
     * Get user-friendly error message for VNPay response codes
     */
    private String getVNPayErrorMessage(String responseCode) {
        switch (responseCode) {
            case "07":
                return "Transaction was suspected of fraud. Please contact your bank.";
            case "09":
                return "Customer cancelled the transaction.";
            case "10":
                return "Customer entered incorrect information more than 3 times.";
            case "11":
                return "Payment deadline has expired. Please try again.";
            case "12":
                return "Customer's account is locked. Please contact your bank.";
            case "13":
                return "Incorrect OTP. Please try again.";
            case "24":
                return "Transaction was cancelled.";
            case "51":
                return "Insufficient account balance.";
            case "65":
                return "Customer exceeded daily transaction limit.";
            case "75":
                return "Payment bank is under maintenance.";
            case "79":
                return "Customer entered payment password incorrectly more than allowed times.";
            case "99":
                return "Unknown error occurred. Please try again.";
            default:
                return "Payment failed with code: " + responseCode + ". Please try again or contact support.";
        }
    }
}

