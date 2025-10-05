package ua.edu.practice.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import ua.edu.practice.model.Order;
import ua.edu.practice.model.OrderStatus;
import ua.edu.practice.repository.OrderRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Інтеграційні тести для OrderRepository
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("OrderRepository Integration Tests")
class OrderRepositoryIntegrationTest {
    
    @Autowired
    private TestEntityManager entityManager;
    
    @Autowired
    private OrderRepository orderRepository;
    
    private Order order1;
    private Order order2;
    private Order order3;
    
    @BeforeEach
    void setUp() {
        order1 = Order.builder()
                .orderId("ORD-REPO-001")
                .customerId("customer1")
                .productName("Product 1")
                .amount(new BigDecimal("100.00"))
                .status(OrderStatus.NEW)
                .build();
        
        order2 = Order.builder()
                .orderId("ORD-REPO-002")
                .customerId("customer1")
                .productName("Product 2")
                .amount(new BigDecimal("200.00"))
                .status(OrderStatus.PAID)
                .build();
        
        order3 = Order.builder()
                .orderId("ORD-REPO-003")
                .customerId("customer2")
                .productName("Product 3")
                .amount(new BigDecimal("300.00"))
                .status(OrderStatus.CANCELLED)
                .build();
        
        entityManager.persistAndFlush(order1);
        entityManager.persistAndFlush(order2);
        entityManager.persistAndFlush(order3);
    }
    
    @Test
    @DisplayName("Should find order by orderId")
    void findByOrderId_ValidOrderId_ReturnsOrder() {
        // Act
        Optional<Order> result = orderRepository.findByOrderId("ORD-REPO-001");
        
        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getOrderId()).isEqualTo("ORD-REPO-001");
        assertThat(result.get().getCustomerId()).isEqualTo("customer1");
    }
    
    @Test
    @DisplayName("Should return empty when orderId not found")
    void findByOrderId_InvalidOrderId_ReturnsEmpty() {
        // Act
        Optional<Order> result = orderRepository.findByOrderId("NON-EXISTENT");
        
        // Assert
        assertThat(result).isEmpty();
    }
    
    @Test
    @DisplayName("Should find orders by customer ID")
    void findByCustomerId_ValidCustomerId_ReturnsOrders() {
        // Act
        List<Order> result = orderRepository.findByCustomerId("customer1");
        
        // Assert
        assertThat(result).hasSize(2);
        assertThat(result).extracting(Order::getCustomerId)
                .containsOnly("customer1");
        assertThat(result).extracting(Order::getOrderId)
                .containsExactlyInAnyOrder("ORD-REPO-001", "ORD-REPO-002");
    }
    
    @Test
    @DisplayName("Should find orders by status")
    void findByStatus_ValidStatus_ReturnsOrders() {
        // Act
        List<Order> result = orderRepository.findByStatus(OrderStatus.PAID);
        
        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(OrderStatus.PAID);
        assertThat(result.get(0).getOrderId()).isEqualTo("ORD-REPO-002");
    }
    
    @Test
    @DisplayName("Should find orders with amount greater than specified")
    void findByAmountGreaterThan_ValidAmount_ReturnsOrders() {
        // Act
        List<Order> result = orderRepository.findByAmountGreaterThan(new BigDecimal("150.00"));
        
        // Assert
        assertThat(result).hasSize(2);
        assertThat(result).extracting(Order::getAmount)
                .allMatch(amount -> amount.compareTo(new BigDecimal("150.00")) > 0);
    }
    
    @Test
    @DisplayName("Should count orders by status")
    void countByStatus_ValidStatus_ReturnsCount() {
        // Act
        long newCount = orderRepository.countByStatus(OrderStatus.NEW);
        long paidCount = orderRepository.countByStatus(OrderStatus.PAID);
        long cancelledCount = orderRepository.countByStatus(OrderStatus.CANCELLED);
        
        // Assert
        assertThat(newCount).isEqualTo(1);
        assertThat(paidCount).isEqualTo(1);
        assertThat(cancelledCount).isEqualTo(1);
    }
    
    @Test
    @DisplayName("Should find orders by amount range")
    void findByAmountBetween_ValidRange_ReturnsOrders() {
        // Act
        List<Order> result = orderRepository.findByAmountBetween(
                new BigDecimal("150.00"), 
                new BigDecimal("250.00")
        );
        
        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getOrderId()).isEqualTo("ORD-REPO-002");
        assertThat(result.get(0).getAmount()).isEqualTo(new BigDecimal("200.00"));
    }
    
    @Test
    @DisplayName("Should save and retrieve order with all fields")
    void saveOrder_AllFields_RetrievesCorrectly() {
        // Arrange
        Order newOrder = Order.builder()
                .orderId("ORD-NEW-001")
                .customerId("new-customer")
                .productName("New Product")
                .amount(new BigDecimal("500.00"))
                .status(OrderStatus.NEW)
                .build();
        
        // Act
        Order savedOrder = orderRepository.save(newOrder);
        entityManager.flush();
        entityManager.clear();
        
        Order retrievedOrder = orderRepository.findById(savedOrder.getId()).orElseThrow();
        
        // Assert
        assertThat(retrievedOrder.getOrderId()).isEqualTo("ORD-NEW-001");
        assertThat(retrievedOrder.getCustomerId()).isEqualTo("new-customer");
        assertThat(retrievedOrder.getProductName()).isEqualTo("New Product");
        assertThat(retrievedOrder.getAmount()).isEqualTo(new BigDecimal("500.00"));
        assertThat(retrievedOrder.getStatus()).isEqualTo(OrderStatus.NEW);
        assertThat(retrievedOrder.getCreatedAt()).isNotNull();
        assertThat(retrievedOrder.getUpdatedAt()).isNotNull();
    }
    
    @Test
    @DisplayName("Should update order and maintain audit fields")
    void updateOrder_StatusChange_UpdatesTimestamp() throws InterruptedException {
        // Arrange
        Order order = orderRepository.findById(order1.getId()).orElseThrow();
        OrderStatus originalStatus = order.getStatus();
        
        // Act
        order.setStatus(OrderStatus.PROCESSING);
        Order updatedOrder = orderRepository.save(order);
        entityManager.flush();
        entityManager.clear();
        
        // Small delay to ensure timestamp difference
        Thread.sleep(10);
        
        Order retrievedOrder = orderRepository.findById(updatedOrder.getId()).orElseThrow();
        
        // Assert
        assertThat(retrievedOrder.getStatus()).isEqualTo(OrderStatus.PROCESSING);
        assertThat(retrievedOrder.getUpdatedAt()).isAfter(retrievedOrder.getCreatedAt());
    }
}
