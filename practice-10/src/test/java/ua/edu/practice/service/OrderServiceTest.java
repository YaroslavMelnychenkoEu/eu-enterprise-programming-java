package ua.edu.practice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ua.edu.practice.dto.OrderDTO;
import ua.edu.practice.exception.InsufficientFundsException;
import ua.edu.practice.exception.OrderNotFoundException;
import ua.edu.practice.model.Order;
import ua.edu.practice.model.OrderStatus;
import ua.edu.practice.repository.OrderRepository;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Модульні тести для OrderService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService Unit Tests")
class OrderServiceTest {
    
    @Mock
    private OrderRepository orderRepository;
    
    @Mock
    private PaymentService paymentService;
    
    @Mock
    private NotificationService notificationService;
    
    @InjectMocks
    private OrderService orderService;
    
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
    void createOrder_ValidData_Success() {
        // Arrange
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        
        // Act
        Order result = orderService.createOrder(orderDTO);
        
        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getOrderId()).isEqualTo("ORD-123456");
        assertThat(result.getStatus()).isEqualTo(OrderStatus.NEW);
        verify(orderRepository).save(any(Order.class));
    }
    
    @Test
    @DisplayName("Should get order by ID successfully")
    void getOrderById_ValidId_ReturnsOrder() {
        // Arrange
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        
        // Act
        Order result = orderService.getOrderById(1L);
        
        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getOrderId()).isEqualTo("ORD-123456");
    }
    
    @Test
    @DisplayName("Should throw exception when order not found by ID")
    void getOrderById_InvalidId_ThrowsException() {
        // Arrange
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThatThrownBy(() -> orderService.getOrderById(999L))
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessage("Order not found with ID: 999");
    }
    
    @Test
    @DisplayName("Should get order by orderId successfully")
    void getOrderByOrderId_ValidOrderId_ReturnsOrder() {
        // Arrange
        when(orderRepository.findByOrderId("ORD-123456")).thenReturn(Optional.of(order));
        
        // Act
        Order result = orderService.getOrderByOrderId("ORD-123456");
        
        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getOrderId()).isEqualTo("ORD-123456");
    }
    
    @Test
    @DisplayName("Should get orders by customer ID")
    void getOrdersByCustomerId_ValidCustomerId_ReturnsOrders() {
        // Arrange
        List<Order> orders = Arrays.asList(order);
        when(orderRepository.findByCustomerId("customer1")).thenReturn(orders);
        
        // Act
        List<Order> result = orderService.getOrdersByCustomerId("customer1");
        
        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCustomerId()).isEqualTo("customer1");
    }
    
    @Test
    @DisplayName("Should update order status successfully")
    void updateOrderStatus_ValidData_Success() {
        // Arrange
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        
        // Act
        Order result = orderService.updateOrderStatus(1L, OrderStatus.PROCESSING);
        
        // Assert
        assertThat(result).isNotNull();
        verify(orderRepository).save(any(Order.class));
    }
    
    @Test
    @DisplayName("Should process payment successfully")
    void processPayment_ValidData_Success() {
        // Arrange
        when(orderRepository.findByOrderId("ORD-123456")).thenReturn(Optional.of(order));
        when(paymentService.hasSufficientFunds(anyString(), any(BigDecimal.class))).thenReturn(true);
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        
        // Act
        Order result = orderService.processPayment("ORD-123456", new BigDecimal("100.00"));
        
        // Assert
        assertThat(result).isNotNull();
        verify(paymentService).processPayment("customer1", new BigDecimal("100.00"));
        verify(notificationService).sendOrderConfirmation(order);
    }
    
    @Test
    @DisplayName("Should throw exception when insufficient funds")
    void processPayment_InsufficientFunds_ThrowsException() {
        // Arrange
        when(orderRepository.findByOrderId("ORD-123456")).thenReturn(Optional.of(order));
        when(paymentService.hasSufficientFunds(anyString(), any(BigDecimal.class))).thenReturn(false);
        
        // Act & Assert
        assertThatThrownBy(() -> orderService.processPayment("ORD-123456", new BigDecimal("100.00")))
                .isInstanceOf(InsufficientFundsException.class)
                .hasMessage("Insufficient funds for order: ORD-123456");
    }
    
    @Test
    @DisplayName("Should cancel order successfully")
    void cancelOrder_ValidOrderId_Success() {
        // Arrange
        when(orderRepository.findByOrderId("ORD-123456")).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        
        // Act
        Order result = orderService.cancelOrder("ORD-123456");
        
        // Assert
        assertThat(result).isNotNull();
        verify(notificationService).sendOrderCancellation(order);
    }
    
    @Test
    @DisplayName("Should throw exception when cancelling delivered order")
    void cancelOrder_DeliveredOrder_ThrowsException() {
        // Arrange
        order.setStatus(OrderStatus.DELIVERED);
        when(orderRepository.findByOrderId("ORD-123456")).thenReturn(Optional.of(order));
        
        // Act & Assert
        assertThatThrownBy(() -> orderService.cancelOrder("ORD-123456"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Cannot cancel delivered order: ORD-123456");
    }
    
    @Test
    @DisplayName("Should generate unique order ID")
    void generateOrderId_Always_ReturnsUniqueId() {
        // Act
        String orderId1 = orderService.generateOrderId();
        String orderId2 = orderService.generateOrderId();
        
        // Assert
        assertThat(orderId1).isNotEqualTo(orderId2);
        assertThat(orderId1).startsWith("ORD-");
        assertThat(orderId1).hasSize(12); // "ORD-" + 8 characters
    }
}
