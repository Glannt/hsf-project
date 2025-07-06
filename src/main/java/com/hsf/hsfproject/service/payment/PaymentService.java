package com.hsf.hsfproject.service.payment;

import com.hsf.hsfproject.configuration.VnPayConfig;
import com.hsf.hsfproject.constants.OrderStatus;
import com.hsf.hsfproject.constants.PaymentMethod;
import com.hsf.hsfproject.constants.TransactionStatus;
import com.hsf.hsfproject.dtos.request.VnPayRequest;
import com.hsf.hsfproject.model.*;
import com.hsf.hsfproject.repository.CartItemRepository;
import com.hsf.hsfproject.repository.CartRepository;
import com.hsf.hsfproject.repository.TransactionRepository;
import com.hsf.hsfproject.service.order.IOrderService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.Calendar;
import java.util.TimeZone;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final VnPayConfig vnPayConfig;
    private final TransactionRepository transactionRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final IOrderService orderService;

    public String createPaymentUrl(HttpServletRequest req, VnPayRequest order) throws UnsupportedEncodingException {
        String vnp_Version = "2.1.0";
        String vnp_Command = "pay";
        String orderType = "other";
        long amount = order.getAmount() * 100; // Convert to VND cents

        String vnp_TxnRef = VnPayConfig.getRandomNumber(8);
        String vnp_IpAddr = VnPayConfig.getIpAddress(req);
        String baseUrl = req.getScheme() + "://" + req.getServerName() + ":" + req.getServerPort();
        String vnp_ReturnUrl = baseUrl + "/payment/vnpay/return";
        
        // Update existing transaction with vnp_TxnRef for VNPay orders
        if (order.getOrderId() != null) {
            try {
                UUID orderId = UUID.fromString(order.getOrderId());
                Optional<Transaction> transactionOpt = transactionRepository.findByOrderId(orderId);
                if (transactionOpt.isPresent()) {
                    Transaction transaction = transactionOpt.get();
                    transaction.setTransactionRef(vnp_TxnRef);
                    transactionRepository.save(transaction);
                }
            } catch (Exception e) {
                // Log error but continue with payment URL generation
            }
        }

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", vnp_Version);
        vnp_Params.put("vnp_Command", vnp_Command);
        vnp_Params.put("vnp_TmnCode", vnPayConfig.getVnpTmnCode());
        vnp_Params.put("vnp_Amount", String.valueOf(amount));
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", order.getOrderInfo() != null ? order.getOrderInfo() : order.getOrderId());
        vnp_Params.put("vnp_OrderType", orderType);

        String locale = order.getLanguage();
        vnp_Params.put("vnp_Locale", (locale != null && !locale.isEmpty()) ? locale : "vn");
        vnp_Params.put("vnp_ReturnUrl", vnp_ReturnUrl);
        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);
        
        // Add optional parameters for better VNPay compatibility
        vnp_Params.put("vnp_BankCode", "NCB"); // Empty for payment gateway selection page

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        cld.add(Calendar.MINUTE, 15);
        String vnp_ExpireDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        // Build query string first (without hash)
        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder query = new StringBuilder();
        StringBuilder hashData = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = (String)itr.next();
            String fieldValue = (String)vnp_Params.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                // Build hash data with original values (not URL encoded)
                hashData.append(fieldName);
                hashData.append("=");
                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));

                
                // Build query string with URL encoded values
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()));
                query.append('=');
                query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                
                if (itr.hasNext()) {
                    hashData.append("&");
                    query.append('&');
                }
            }
        }
        
        // Generate hash using original (non-encoded) values
        String queryUrl = query.toString();
        String vnp_SecureHash = VnPayConfig.hmacSHA512(vnPayConfig.getSecretKey(), hashData.toString());
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;
        String paymentUrl = vnPayConfig.getVnpPayUrl() + "?" + queryUrl;
        return paymentUrl;
    }

    public Transaction createTransaction(Order order, PaymentMethod paymentMethod) {
        Transaction transaction = new Transaction();
        transaction.setOrder(order);
        transaction.setTransactionDate(LocalDateTime.now());
        transaction.setTotalAmount(order.getTotalPrice());
        transaction.setPaymentMethod(paymentMethod.getCode());
        transaction.setStatus(TransactionStatus.PENDING.getCode());
        return transactionRepository.save(transaction);
    }

    public Transaction createTransaction(Order order, PaymentMethod paymentMethod, String transactionRef) {
        Transaction transaction = new Transaction();
        transaction.setOrder(order);
        transaction.setTransactionDate(LocalDateTime.now());
        transaction.setTotalAmount(order.getTotalPrice());
        transaction.setPaymentMethod(paymentMethod.getCode());
        transaction.setStatus(TransactionStatus.PENDING.getCode());
        transaction.setTransactionRef(transactionRef);
        return transactionRepository.save(transaction);
    }

    public void updateTransactionStatus(UUID transactionId, TransactionStatus status) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));
        transaction.setStatus(status.getCode());
        transactionRepository.save(transaction);
    }

    public void updateTransactionStatus(Order order, TransactionStatus status, String transactionRef) {
        Optional<Transaction> transactionOpt = transactionRepository.findByOrderId(order.getId());
        if (transactionOpt.isPresent()) {
            Transaction transaction = transactionOpt.get();
            transaction.setStatus(status.getCode());
            if (transactionRef != null) {
                transaction.setTransactionRef(transactionRef);
            }
            transactionRepository.save(transaction);
        }
    }

    public boolean validateVNPayResponse(Map<String, String> params) {
        String vnp_SecureHash = params.get("vnp_SecureHash");
        if (vnp_SecureHash == null || vnp_SecureHash.isEmpty()) {
            return false;
        }
        
        // Remove secure hash and hash type from params for validation (like VNPay sample)
        Map<String, String> fields = new HashMap<>(params);
        fields.remove("vnp_SecureHashType");
        fields.remove("vnp_SecureHash");

        String signValue = VnPayConfig.hashAllFields(fields, vnPayConfig.getSecretKey());
        return signValue.equals(vnp_SecureHash);
    }

    @Transactional
    public void clearUserCart(User user) {
        Optional<Cart> cartOpt = cartRepository.findByUserId(user.getId());
        if (cartOpt.isPresent()) {
            Cart cart = cartOpt.get();
            
            // First, clear the collection to avoid Hibernate issues
            cart.getCartItems().clear();
            
            // Then delete all cart items from database
            cartItemRepository.deleteByCartId(cart.getId());
            
            // Reset cart totals
            cart.setTotalPrice(0.0);
            cart.setItemCount(0);
            
            // Save the updated cart
            cartRepository.save(cart);
        }
    }

    public Transaction findTransactionByOrderId(UUID orderId) {
        return transactionRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Transaction not found for order: " + orderId));
    }

    public Optional<Transaction> findTransactionByTxnRef(String txnRef) {
        return transactionRepository.findByTransactionRef(txnRef);
    }

    // IPN validation methods
    public boolean checkOrderExists(String txnRef) {
        try {
            // txnRef is the VNPay transaction reference
            return transactionRepository.findByTransactionRef(txnRef).isPresent();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean validateAmount(String txnRef, String vnpAmount) {
        try {
            Optional<Transaction> transactionOpt = transactionRepository.findByTransactionRef(txnRef);
            if (transactionOpt.isPresent()) {
                Transaction transaction = transactionOpt.get();
                // VNPay amount is in cents (multiply by 100)
                long expectedAmount = (long) (transaction.getTotalAmount() * 100);
                long receivedAmount = Long.parseLong(vnpAmount);
                return expectedAmount == receivedAmount;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isOrderPending(String txnRef) {
        try {
            Optional<Transaction> transactionOpt = transactionRepository.findByTransactionRef(txnRef);
            if (transactionOpt.isPresent()) {
                Transaction transaction = transactionOpt.get();
                return TransactionStatus.PENDING.getCode().equals(transaction.getStatus());
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public void updateTransactionStatusByTxnRef(String txnRef, TransactionStatus status, String transactionNo) {
        try {
            Optional<Transaction> transactionOpt = transactionRepository.findByTransactionRef(txnRef);
            if (transactionOpt.isPresent()) {
                Transaction transaction = transactionOpt.get();
                transaction.setStatus(status.getCode());
                if (transactionNo != null) {
                    // Update with VNPay transaction number if provided
                    transaction.setTransactionRef(transactionNo);
                }
                transactionRepository.save(transaction);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to update transaction status: " + e.getMessage());
        }
    }

    public void updateOrderStatusByTxnRef(String txnRef, OrderStatus status) {
        try {
            Optional<Transaction> transactionOpt = transactionRepository.findByTransactionRef(txnRef);
            if (transactionOpt.isPresent()) {
                Transaction transaction = transactionOpt.get();
                Order order = transaction.getOrder();
                if (order != null) {
                    order.setStatus(status.getCode());
                    orderService.saveOrder(order);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to update order status: " + e.getMessage());
        }
    }

    /**
     * Cancel an order and rollback the transaction
     * This method handles both order cancellation and transaction rollback
     */
    public boolean cancelOrder(UUID orderId, String reason) {
        try {
            // Find the order
            Order order = orderService.getOrderById(orderId.toString());
            if (order == null) {
                return false;
            }

            // Check if order can be cancelled (only PENDING or CONFIRMED orders)
            if (!OrderStatus.PENDING.getCode().equals(order.getStatus()) && 
                !OrderStatus.CONFIRMED.getCode().equals(order.getStatus())) {
                return false; // Cannot cancel orders that are already processed/shipped
            }

            // Update order status to CANCELLED
            order.setStatus(OrderStatus.CANCELLED.getCode());
            orderService.saveOrder(order);

            // Find and update transaction status
            Optional<Transaction> transactionOpt = transactionRepository.findByOrderId(orderId);
            if (transactionOpt.isPresent()) {
                Transaction transaction = transactionOpt.get();
                transaction.setStatus(TransactionStatus.CANCELLED.getCode());
                transactionRepository.save(transaction);
            }

            return true;
        } catch (Exception e) {
            throw new RuntimeException("Failed to cancel order: " + e.getMessage());
        }
    }

    /**
     * Rollback a failed VNPay transaction
     * This method handles cleanup when VNPay payment fails
     */
    public void rollbackFailedVNPayTransaction(String txnRef) {
        try {
            Optional<Transaction> transactionOpt = transactionRepository.findByTransactionRef(txnRef);
            if (transactionOpt.isPresent()) {
                Transaction transaction = transactionOpt.get();
                Order order = transaction.getOrder();
                
                // Update transaction status to FAILED
                transaction.setStatus(TransactionStatus.FAILED.getCode());
                transactionRepository.save(transaction);
                
                // Update order status to CANCELLED
                if (order != null) {
                    order.setStatus(OrderStatus.CANCELLED.getCode());
                    orderService.saveOrder(order);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to rollback transaction: " + e.getMessage());
        }
    }

    /**
     * Check if an order can be cancelled
     */
    public boolean canCancelOrder(UUID orderId) {
        try {
            Order order = orderService.getOrderById(orderId.toString());
            if (order == null) {
                return false;
            }
            
            // Only allow cancellation for PENDING or CONFIRMED orders
            return OrderStatus.PENDING.getCode().equals(order.getStatus()) || 
                   OrderStatus.CONFIRMED.getCode().equals(order.getStatus());
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Restore user cart items when order is cancelled
     * This helps users to easily reorder if they cancelled by mistake
     */
    public void restoreCartFromCancelledOrder(UUID orderId) {
        try {
            Order order = orderService.getOrderById(orderId.toString());
            if (order != null && OrderStatus.CANCELLED.getCode().equals(order.getStatus())) {
                User user = order.getUser();
                Optional<Cart> cartOpt = cartRepository.findByUserId(user.getId());
                
                if (cartOpt.isPresent()) {
                    Cart cart = cartOpt.get();
                    // Add order items back to cart
                    // Note: This is a simplified implementation
                    // You might want to check for product availability and stock
                    double totalPrice = 0;
                    int itemCount = 0;
                    
                    for (OrderDetail orderDetail : order.getOrderItems()) {
                        // Create cart item from order detail
                        // This would require implementing the reverse mapping
                        totalPrice += orderDetail.getUnitPrice() * orderDetail.getQuantity();
                        itemCount += orderDetail.getQuantity();
                    }
                    
                    cart.setTotalPrice(totalPrice);
                    cart.setItemCount(itemCount);
                    cartRepository.save(cart);
                }
            }
        } catch (Exception e) {
            // Log error but don't throw exception as this is a convenience feature
            System.err.println("Failed to restore cart from cancelled order: " + e.getMessage());
        }
    }
}