package com.hsf.hsfproject.controller.checkout;

import com.hsf.hsfproject.dtos.request.OrderDto;
import com.hsf.hsfproject.model.Order;
import com.hsf.hsfproject.model.OrderDetail;
import com.hsf.hsfproject.model.User;
import com.hsf.hsfproject.service.order.OrderService;
import com.hsf.hsfproject.service.payment.PaymentService;
import com.hsf.hsfproject.service.user.IUserService;
import com.stripe.model.checkout.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.security.Principal;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CheckoutControllerTest {

    @Mock
    private PaymentService paymentService;

    @Mock
    private OrderService orderService;

    @Mock
    private IUserService userService;

    @Mock
    private Principal principal;

    @InjectMocks
    private CheckoutController checkoutController;

    private MockMvc mockMvc;
    private MockHttpSession httpSession;
    private RedirectAttributes redirectAttributes;
    private OrderDto orderDto;
    private User user;
    private Order existingOrder;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(checkoutController).build();
        httpSession = new MockHttpSession();
        redirectAttributes = new RedirectAttributesModelMap();

        // Setup test data
        user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");

        orderDto = new OrderDto();
        orderDto.setOrderNumber("ORDER123456");
        orderDto.setTotalPrice(100.0);
        orderDto.setShippingAddress("123 Test Street, Test City");

        existingOrder = new Order();
        existingOrder.setOrderNumber("ORDER123456");
        existingOrder.setTotalPrice(100.0);
        existingOrder.setUser(user);
        existingOrder.setStatus("PENDING");

        // Create mock order items
        Set<OrderDetail> orderItems = new HashSet<>();
        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setProductName("Test Product");
        orderDetail.setQuantity(1);
        orderDetail.setUnitPrice(100.0);
        orderDetail.setSubtotal(100.0);
        orderItems.add(orderDetail);
        existingOrder.setOrderItems(orderItems);
    }

    @Test
    void testHandleCheckout_StripePayment_Success() throws Exception {
        // Arrange
        when(principal.getName()).thenReturn("testuser");
        when(userService.findByUsername("testuser")).thenReturn(user);
        when(orderService.findOrderByOrderNumber("ORDER123456")).thenReturn(existingOrder);
        
        Session mockSession = mock(Session.class);
        when(mockSession.getUrl()).thenReturn("https://checkout.stripe.com/test-session");
        when(paymentService.createCheckoutSession(any(OrderDto.class))).thenReturn(mockSession);
        when(orderService.saveOrder(any(Order.class))).thenReturn(existingOrder);

        // Act
        String result = checkoutController.handleCheckout(
                orderDto, 
                "stripe", 
                principal, 
                httpSession, 
                redirectAttributes
        );

        // Assert
        assertEquals("redirect:https://checkout.stripe.com/test-session", result);
        verify(orderService).findOrderByOrderNumber("ORDER123456");
        verify(orderService).saveOrder(any(Order.class));
        verify(paymentService).createCheckoutSession(any(OrderDto.class));
        assertEquals(orderDto, httpSession.getAttribute("orderDto"));
    }

    @Test
    void testHandleCheckout_CODPayment_Success() throws Exception {
        // Arrange
        when(principal.getName()).thenReturn("testuser");
        when(userService.findByUsername("testuser")).thenReturn(user);
        when(orderService.findOrderByOrderNumber("ORDER123456")).thenReturn(existingOrder);
        when(orderService.acceptOrder(any(Order.class), anyString(), anyString())).thenReturn(existingOrder);

        // Act
        String result = checkoutController.handleCheckout(
                orderDto, 
                "cod", 
                principal, 
                httpSession, 
                redirectAttributes
        );

        // Assert
        assertEquals("redirect:/checkout/success", result);
        verify(orderService).findOrderByOrderNumber("ORDER123456");
        verify(orderService).acceptOrder(any(Order.class), eq("123 Test Street, Test City"), anyString());
        assertEquals(orderDto, httpSession.getAttribute("orderDto"));
    }

    @Test
    void testHandleCheckout_MissingShippingAddress_Error() throws Exception {
        // Arrange
        orderDto.setShippingAddress(null);
        when(principal.getName()).thenReturn("testuser");
        when(userService.findByUsername("testuser")).thenReturn(user);

        // Act
        String result = checkoutController.handleCheckout(
                orderDto, 
                "stripe", 
                principal, 
                httpSession, 
                redirectAttributes
        );

        // Assert
        assertEquals("redirect:/order", result);
        verify(orderService, never()).findOrderByOrderNumber(anyString());
        verify(paymentService, never()).createCheckoutSession(any(OrderDto.class));
    }

    @Test
    void testHandleCheckout_OrderNotFound_Error() throws Exception {
        // Arrange
        when(principal.getName()).thenReturn("testuser");
        when(userService.findByUsername("testuser")).thenReturn(user);
        when(orderService.findOrderByOrderNumber("ORDER123456")).thenReturn(null);

        // Act
        String result = checkoutController.handleCheckout(
                orderDto, 
                "stripe", 
                principal, 
                httpSession, 
                redirectAttributes
        );

        // Assert
        assertEquals("redirect:/checkout/error", result);
        verify(orderService).findOrderByOrderNumber("ORDER123456");
        verify(paymentService, never()).createCheckoutSession(any(OrderDto.class));
    }

    @Test
    void testHandleCheckout_InvalidPaymentMethod_Error() throws Exception {
        // Arrange
        when(principal.getName()).thenReturn("testuser");
        when(userService.findByUsername("testuser")).thenReturn(user);

        // Act
        String result = checkoutController.handleCheckout(
                orderDto, 
                "invalid_method", 
                principal, 
                httpSession, 
                redirectAttributes
        );

        // Assert
        assertEquals("redirect:/checkout/error", result);
        verify(orderService, never()).findOrderByOrderNumber(anyString());
        verify(paymentService, never()).createCheckoutSession(any(OrderDto.class));
    }
}
