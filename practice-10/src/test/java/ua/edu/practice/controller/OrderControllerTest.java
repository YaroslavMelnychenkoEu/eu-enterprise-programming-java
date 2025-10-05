package ua.edu.practice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import ua.edu.practice.dto.OrderDTO;
import ua.edu.practice.model.Order;
import ua.edu.practice.model.OrderStatus;
import ua.edu.practice.service.OrderService;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Модульні тести для OrderController
 */
@WebMvcTest(OrderController.class)
@DisplayName("OrderController Unit Tests")
class OrderControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private OrderService orderService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private OrderDTO orderDTO;
    private Order order;
    
    @BeforeEach
    void setUp() {
        orderDTO = OrderDTO.builder()
                .orderId("ORD-123456")
                .customerId("customer1")
                .productName("Test Product")
                .amount(new BigDecimal("100.00"))
                .build();
        
        order = Order.builder()
                .id(1L)
                .orderId("ORD-123456")
                .customerId("customer1")
                .productName("Test Product")
                .amount(new BigDecimal("100.00"))
                .status(OrderStatus.NEW)
                .build();
    }
    
    @Test
    @DisplayName("Should create order successfully")
    @WithMockUser
    void createOrder_ValidData_Success() throws Exception {
        // Arrange
        when(orderService.createOrder(any(OrderDTO.class))).thenReturn(order);
        
        // Act & Assert
        mockMvc.perform(post("/api/orders")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId").value("ORD-123456"))
                .andExpect(jsonPath("$.customerId").value("customer1"))
                .andExpect(jsonPath("$.status").value("NEW"));
    }
    
    @Test
    @DisplayName("Should get order by ID successfully")
    @WithMockUser
    void getOrderById_ValidId_Success() throws Exception {
        // Arrange
        when(orderService.getOrderById(1L)).thenReturn(order);
        
        // Act & Assert
        mockMvc.perform(get("/api/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.orderId").value("ORD-123456"));
    }
    
    @Test
    @DisplayName("Should get order by orderId successfully")
    @WithMockUser
    void getOrderByOrderId_ValidOrderId_Success() throws Exception {
        // Arrange
        when(orderService.getOrderByOrderId("ORD-123456")).thenReturn(order);
        
        // Act & Assert
        mockMvc.perform(get("/api/orders/order/ORD-123456"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value("ORD-123456"));
    }
    
    @Test
    @DisplayName("Should get orders by customer ID successfully")
    @WithMockUser
    void getOrdersByCustomerId_ValidCustomerId_Success() throws Exception {
        // Arrange
        List<Order> orders = Arrays.asList(order);
        when(orderService.getOrdersByCustomerId("customer1")).thenReturn(orders);
        
        // Act & Assert
        mockMvc.perform(get("/api/orders/customer/customer1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].customerId").value("customer1"));
    }
    
    @Test
    @DisplayName("Should get all orders with ADMIN role")
    @WithMockUser(roles = "ADMIN")
    void getAllOrders_AdminRole_Success() throws Exception {
        // Arrange
        List<Order> orders = Arrays.asList(order);
        when(orderService.getAllOrders()).thenReturn(orders);
        
        // Act & Assert
        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].orderId").value("ORD-123456"));
    }
    
    @Test
    @DisplayName("Should return 403 for non-admin users accessing all orders")
    @WithMockUser(roles = "USER")
    void getAllOrders_UserRole_Returns403() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isForbidden());
    }
    
    @Test
    @DisplayName("Should update order status with ADMIN role")
    @WithMockUser(roles = "ADMIN")
    void updateOrderStatus_AdminRole_Success() throws Exception {
        // Arrange
        when(orderService.updateOrderStatus(anyLong(), any(OrderStatus.class))).thenReturn(order);
        
        // Act & Assert
        mockMvc.perform(put("/api/orders/1/status")
                        .with(csrf())
                        .param("status", "PROCESSING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value("ORD-123456"));
    }
    
    @Test
    @DisplayName("Should process payment successfully")
    @WithMockUser
    void processPayment_ValidData_Success() throws Exception {
        // Arrange
        when(orderService.processPayment(any(), any(BigDecimal.class))).thenReturn(order);
        
        // Act & Assert
        mockMvc.perform(post("/api/orders/ORD-123456/payment")
                        .with(csrf())
                        .param("amount", "100.00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value("ORD-123456"));
    }
    
    @Test
    @DisplayName("Should cancel order successfully")
    @WithMockUser
    void cancelOrder_ValidOrderId_Success() throws Exception {
        // Arrange
        when(orderService.cancelOrder("ORD-123456")).thenReturn(order);
        
        // Act & Assert
        mockMvc.perform(delete("/api/orders/ORD-123456")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value("ORD-123456"));
    }
    
    @Test
    @DisplayName("Should get order statistics with ADMIN role")
    @WithMockUser(roles = "ADMIN")
    void getOrderStatistics_AdminRole_Success() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/orders/statistics"))
                .andExpect(status().isOk());
    }
    
    @Test
    @DisplayName("Should generate order ID successfully")
    void generateOrderId_Always_Success() throws Exception {
        // Arrange
        when(orderService.generateOrderId()).thenReturn("ORD-ABCD1234");
        
        // Act & Assert
        mockMvc.perform(get("/api/orders/generate-order-id"))
                .andExpect(status().isOk())
                .andExpect(content().string("ORD-ABCD1234"));
    }
}
