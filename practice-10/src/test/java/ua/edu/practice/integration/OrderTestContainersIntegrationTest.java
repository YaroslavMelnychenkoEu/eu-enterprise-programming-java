package ua.edu.practice.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ua.edu.practice.dto.OrderDTO;
import ua.edu.practice.model.Order;
import ua.edu.practice.repository.OrderRepository;
import ua.edu.practice.service.OrderService;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Інтеграційні тести з використанням Testcontainers
 */
@SpringBootTest
@AutoConfigureWebMvc
@Testcontainers
@ActiveProfiles("test")
@DisplayName("Order TestContainers Integration Tests")
class OrderTestContainersIntegrationTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:14")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");
    
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private OrderRepository orderRepository;
    
    private OrderDTO orderDTO;
    
    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
        
        orderDTO = OrderDTO.builder()
                .orderId("ORD-TESTCONTAINER-001")
                .customerId("testcontainer-customer")
                .productName("TestContainer Product")
                .amount(new BigDecimal("150.00"))
                .build();
    }
    
    @Test
    @DisplayName("Should create and retrieve order using TestContainers")
    @WithMockUser
    void createAndRetrieveOrder_WithTestContainers_Success() throws Exception {
        // Arrange
        String orderJson = """
                {
                    "orderId": "ORD-TESTCONTAINER-001",
                    "customerId": "testcontainer-customer",
                    "productName": "TestContainer Product",
                    "amount": 150.00
                }
                """;
        
        // Act & Assert - Create Order
        mockMvc.perform(post("/api/orders")
                        .with(csrf())
                        .contentType("application/json")
                        .content(orderJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId").value("ORD-TESTCONTAINER-001"))
                .andExpect(jsonPath("$.customerId").value("testcontainer-customer"))
                .andExpect(jsonPath("$.amount").value(150.00));
        
        // Verify in database
        List<Order> orders = orderRepository.findAll();
        assertThat(orders).hasSize(1);
        assertThat(orders.get(0).getOrderId()).isEqualTo("ORD-TESTCONTAINER-001");
    }
    
    @Test
    @DisplayName("Should handle multiple orders with TestContainers")
    @WithMockUser
    void handleMultipleOrders_WithTestContainers_Success() {
        // Arrange
        OrderDTO order1 = OrderDTO.builder()
                .orderId("ORD-MULTI-001")
                .customerId("multi-customer")
                .productName("Product 1")
                .amount(new BigDecimal("100.00"))
                .build();
        
        OrderDTO order2 = OrderDTO.builder()
                .orderId("ORD-MULTI-002")
                .customerId("multi-customer")
                .productName("Product 2")
                .amount(new BigDecimal("200.00"))
                .build();
        
        // Act
        Order createdOrder1 = orderService.createOrder(order1);
        Order createdOrder2 = orderService.createOrder(order2);
        
        // Assert
        List<Order> allOrders = orderRepository.findAll();
        assertThat(allOrders).hasSize(2);
        
        List<Order> customerOrders = orderRepository.findByCustomerId("multi-customer");
        assertThat(customerOrders).hasSize(2);
        assertThat(customerOrders).extracting(Order::getOrderId)
                .containsExactlyInAnyOrder("ORD-MULTI-001", "ORD-MULTI-002");
    }
    
    @Test
    @DisplayName("Should persist order data correctly with TestContainers")
    void persistOrderData_WithTestContainers_DataCorrect() {
        // Act
        Order createdOrder = orderService.createOrder(orderDTO);
        
        // Assert - Check all fields are persisted correctly
        Order retrievedOrder = orderRepository.findById(createdOrder.getId()).orElseThrow();
        
        assertThat(retrievedOrder.getOrderId()).isEqualTo("ORD-TESTCONTAINER-001");
        assertThat(retrievedOrder.getCustomerId()).isEqualTo("testcontainer-customer");
        assertThat(retrievedOrder.getProductName()).isEqualTo("TestContainer Product");
        assertThat(retrievedOrder.getAmount()).isEqualTo(new BigDecimal("150.00"));
        assertThat(retrievedOrder.getCreatedAt()).isNotNull();
        assertThat(retrievedOrder.getUpdatedAt()).isNotNull();
    }
    
    @Test
    @DisplayName("Should handle database transactions correctly")
    @WithMockUser
    void handleDatabaseTransactions_WithTestContainers_Success() throws Exception {
        // Arrange
        String orderJson = """
                {
                    "orderId": "ORD-TRANSACTION-001",
                    "customerId": "transaction-customer",
                    "productName": "Transaction Product",
                    "amount": 300.00
                }
                """;
        
        // Act - Create order
        mockMvc.perform(post("/api/orders")
                        .with(csrf())
                        .contentType("application/json")
                        .content(orderJson))
                .andExpect(status().isCreated());
        
        // Act - Process payment
        mockMvc.perform(post("/api/orders/{orderId}/payment", "ORD-TRANSACTION-001")
                        .with(csrf())
                        .param("amount", "300.00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAID"));
        
        // Assert - Verify transaction was committed
        Order order = orderRepository.findByOrderId("ORD-TRANSACTION-001").orElseThrow();
        assertThat(order.getStatus().name()).isEqualTo("PAID");
    }
}
