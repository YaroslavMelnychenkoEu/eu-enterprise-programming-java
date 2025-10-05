package ua.edu.practice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ua.edu.practice.dto.OrderDTO;
import ua.edu.practice.dto.OrderStatistics;
import ua.edu.practice.model.Order;
import ua.edu.practice.model.OrderStatus;
import ua.edu.practice.service.OrderService;

import java.math.BigDecimal;
import java.util.List;

/**
 * REST контролер для роботи з замовленнями
 */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {
    
    private final OrderService orderService;
    
    /**
     * Створити нове замовлення
     */
    @PostMapping
    public ResponseEntity<Order> createOrder(@Valid @RequestBody OrderDTO orderDTO) {
        log.info("Creating order for customer: {}", orderDTO.getCustomerId());
        
        Order order = orderService.createOrder(orderDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }
    
    /**
     * Отримати замовлення за ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderById(@PathVariable Long id) {
        log.info("Getting order by ID: {}", id);
        
        Order order = orderService.getOrderById(id);
        return ResponseEntity.ok(order);
    }
    
    /**
     * Отримати замовлення за orderId
     */
    @GetMapping("/order/{orderId}")
    public ResponseEntity<Order> getOrderByOrderId(@PathVariable String orderId) {
        log.info("Getting order by orderId: {}", orderId);
        
        Order order = orderService.getOrderByOrderId(orderId);
        return ResponseEntity.ok(order);
    }
    
    /**
     * Отримати всі замовлення клієнта
     */
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<Order>> getOrdersByCustomerId(@PathVariable String customerId) {
        log.info("Getting orders for customer: {}", customerId);
        
        List<Order> orders = orderService.getOrdersByCustomerId(customerId);
        return ResponseEntity.ok(orders);
    }
    
    /**
     * Отримати всі замовлення (тільки для адміністраторів)
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Order>> getAllOrders() {
        log.info("Getting all orders");
        
        List<Order> orders = orderService.getAllOrders();
        return ResponseEntity.ok(orders);
    }
    
    /**
     * Оновити статус замовлення
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Order> updateOrderStatus(@PathVariable Long id, 
                                                   @RequestParam OrderStatus status) {
        log.info("Updating order {} status to: {}", id, status);
        
        Order order = orderService.updateOrderStatus(id, status);
        return ResponseEntity.ok(order);
    }
    
    /**
     * Обробити платіж за замовлення
     */
    @PostMapping("/{orderId}/payment")
    public ResponseEntity<Order> processPayment(@PathVariable String orderId, 
                                               @RequestParam BigDecimal amount) {
        log.info("Processing payment for order: {} with amount: {}", orderId, amount);
        
        Order order = orderService.processPayment(orderId, amount);
        return ResponseEntity.ok(order);
    }
    
    /**
     * Скасувати замовлення
     */
    @DeleteMapping("/{orderId}")
    public ResponseEntity<Order> cancelOrder(@PathVariable String orderId) {
        log.info("Cancelling order: {}", orderId);
        
        Order order = orderService.cancelOrder(orderId);
        return ResponseEntity.ok(order);
    }
    
    /**
     * Отримати статистику замовлень (тільки для адміністраторів)
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderStatistics> getOrderStatistics() {
        log.info("Getting order statistics");
        
        OrderStatistics statistics = orderService.getOrderStatistics();
        return ResponseEntity.ok(statistics);
    }
    
    /**
     * Генерувати новий orderId
     */
    @GetMapping("/generate-order-id")
    public ResponseEntity<String> generateOrderId() {
        String orderId = orderService.generateOrderId();
        return ResponseEntity.ok(orderId);
    }
}
