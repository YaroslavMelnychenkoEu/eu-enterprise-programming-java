package ua.edu.practice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import ua.edu.practice.dto.OrderDTO;
import ua.edu.practice.model.Order;
import ua.edu.practice.model.OrderStatus;
import ua.edu.practice.repository.OrderRepository;
import ua.edu.practice.service.OrderService;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Інтеграційні тести для Order функціональності
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Order Integration Tests")
class OrderIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private OrderDTO orderDTO;
    
    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
        
        orderDTO = OrderDTO.builder()
                .orderId("ORD-INTEGRATION-001")
                .customerId("integration-customer")
                .productName("Integration Test Product")
                .amount(new BigDecimal("250.00"))
                .build();
    }
    
    @Test
    @DisplayName("Should complete order flow from creation to payment")
    @WithMockUser
    void orderFlow_CompleteScenario_Success() throws Exception {
        // Arrange
        String orderJson = objectMapper.writeValueAsString(orderDTO);
        
        // Act & Assert - Create Order
        String response = mockMvc.perform(post("/api/orders")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId").value("ORD-INTEGRATION-001"))
                .andExpect(jsonPath("$.status").value("NEW"))
                .andReturn()
                .getResponse()
                .getContentAsString();
        
        // Extract order ID from response
        Order createdOrder = objectMapper.readValue(response, Order.class);
        Long orderId = createdOrder.getId();
        
        // Act & Assert - Get Order by ID
        mockMvc.perform(get("/api/orders/{id}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId))
                .andExpect(jsonPath("$.orderId").value("ORD-INTEGRATION-001"));
        
        // Act & Assert - Process Payment
        mockMvc.perform(post("/api/orders/{orderId}/payment", "ORD-INTEGRATION-001")
                        .with(csrf())
                        .param("amount", "250.00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAID"));
        
        // Verify in database
        Order orderFromDb = orderRepository.findById(orderId).orElseThrow();
        assertThat(orderFromDb.getStatus()).isEqualTo(OrderStatus.PAID);
    }
    
    @Test
    @DisplayName("Should handle order cancellation flow")
    @WithMockUser
    void orderFlow_CancellationScenario_Success() throws Exception {
        // Arrange - Create order
        String orderJson = objectMapper.writeValueAsString(orderDTO);
        
        String response = mockMvc.perform(post("/api/orders")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderJson))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        
        Order createdOrder = objectMapper.readValue(response, Order.class);
        
        // Act & Assert - Cancel Order
        mockMvc.perform(delete("/api/orders/{orderId}", "ORD-INTEGRATION-001")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
        
        // Verify in database
        Order orderFromDb = orderRepository.findById(createdOrder.getId()).orElseThrow();
        assertThat(orderFromDb.getStatus()).isEqualTo(OrderStatus.CANCELLED);
    }
    
    @Test
    @DisplayName("Should retrieve orders by customer ID")
    @WithMockUser
    void getOrdersByCustomerId_MultipleOrders_ReturnsAllOrders() throws Exception {
        // Arrange - Create multiple orders for same customer
        OrderDTO order1 = OrderDTO.builder()
                .orderId("ORD-CUST-001")
                .customerId("test-customer")
                .productName("Product 1")
                .amount(new BigDecimal("100.00"))
                .build();
        
        OrderDTO order2 = OrderDTO.builder()
                .orderId("ORD-CUST-002")
                .customerId("test-customer")
                .productName("Product 2")
                .amount(new BigDecimal("200.00"))
                .build();
        
        // Create orders
        orderService.createOrder(order1);
        orderService.createOrder(order2);
        
        // Act & Assert
        mockMvc.perform(get("/api/orders/customer/{customerId}", "test-customer"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].customerId").value("test-customer"))
                .andExpect(jsonPath("$[1].customerId").value("test-customer"));
    }
    
    @Test
    @DisplayName("Should handle insufficient funds scenario")
    @WithMockUser
    void processPayment_InsufficientFunds_ReturnsError() throws Exception {
        // Arrange - Create order with high amount
        OrderDTO highAmountOrder = OrderDTO.builder()
                .orderId("ORD-HIGH-001")
                .customerId("customer1") // Has 1000.00 balance
                .productName("Expensive Product")
                .amount(new BigDecimal("1500.00")) // More than balance
                .build();
        
        String orderJson = objectMapper.writeValueAsString(highAmountOrder);
        
        // Create order
        mockMvc.perform(post("/api/orders")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderJson))
                .andExpect(status().isCreated());
        
        // Act & Assert - Try to process payment
        mockMvc.perform(post("/api/orders/{orderId}/payment", "ORD-HIGH-001")
                        .with(csrf())
                        .param("amount", "1500.00"))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("Should update order status with admin privileges")
    @WithMockUser(roles = "ADMIN")
    void updateOrderStatus_AdminRole_Success() throws Exception {
        // Arrange - Create order
        Order order = orderService.createOrder(orderDTO);
        
        // Act & Assert
        mockMvc.perform(put("/api/orders/{id}/status", order.getId())
                        .with(csrf())
                        .param("status", "PROCESSING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PROCESSING"));
        
        // Verify in database
        Order updatedOrder = orderRepository.findById(order.getId()).orElseThrow();
        assertThat(updatedOrder.getStatus()).isEqualTo(OrderStatus.PROCESSING);
    }
    
    @Test
    @DisplayName("Should get order statistics with admin privileges")
    @WithMockUser(roles = "ADMIN")
    void getOrderStatistics_AdminRole_ReturnsStatistics() throws Exception {
        // Arrange - Create some orders
        orderService.createOrder(orderDTO);
        
        OrderDTO order2 = OrderDTO.builder()
                .orderId("ORD-STATS-002")
                .customerId("stats-customer")
                .productName("Stats Product")
                .amount(new BigDecimal("150.00"))
                .build();
        orderService.createOrder(order2);
        
        // Act & Assert
        mockMvc.perform(get("/api/orders/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalOrders").value(2))
                .andExpect(jsonPath("$.newOrders").value(2));
    }
    
    @Test
    @DisplayName("Should generate unique order IDs")
    void generateOrderId_MultipleCalls_ReturnsUniqueIds() throws Exception {
        // Act
        String orderId1 = mockMvc.perform(get("/api/orders/generate-order-id"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        
        String orderId2 = mockMvc.perform(get("/api/orders/generate-order-id"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        
        // Assert
        assertThat(orderId1).isNotEqualTo(orderId2);
        assertThat(orderId1).startsWith("ORD-");
        assertThat(orderId2).startsWith("ORD-");
    }
}
