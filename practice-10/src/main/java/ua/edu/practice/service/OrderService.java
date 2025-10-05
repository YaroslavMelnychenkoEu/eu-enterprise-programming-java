package ua.edu.practice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.edu.practice.dto.OrderDTO;
import ua.edu.practice.dto.OrderStatistics;
import ua.edu.practice.exception.OrderNotFoundException;
import ua.edu.practice.exception.InsufficientFundsException;
import ua.edu.practice.model.Order;
import ua.edu.practice.model.OrderStatus;
import ua.edu.practice.repository.OrderRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Сервіс для роботи з замовленнями
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OrderService {
    
    private final OrderRepository orderRepository;
    private final PaymentService paymentService;
    private final NotificationService notificationService;
    
    /**
     * Створити нове замовлення
     */
    public Order createOrder(OrderDTO orderDTO) {
        log.info("Creating order for customer: {}", orderDTO.getCustomerId());
        
        Order order = Order.builder()
                .orderId(orderDTO.getOrderId())
                .customerId(orderDTO.getCustomerId())
                .productName(orderDTO.getProductName())
                .amount(orderDTO.getAmount())
                .status(OrderStatus.NEW)
                .build();
        
        Order savedOrder = orderRepository.save(order);
        log.info("Order created with ID: {}", savedOrder.getId());
        
        return savedOrder;
    }
    
    /**
     * Отримати замовлення за ID
     */
    @Transactional(readOnly = true)
    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with ID: " + id));
    }
    
    /**
     * Отримати замовлення за orderId
     */
    @Transactional(readOnly = true)
    public Order getOrderByOrderId(String orderId) {
        return orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with orderId: " + orderId));
    }
    
    /**
     * Отримати всі замовлення клієнта
     */
    @Transactional(readOnly = true)
    public List<Order> getOrdersByCustomerId(String customerId) {
        return orderRepository.findByCustomerId(customerId);
    }
    
    /**
     * Отримати всі замовлення
     */
    @Transactional(readOnly = true)
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }
    
    /**
     * Оновити статус замовлення
     */
    public Order updateOrderStatus(Long id, OrderStatus status) {
        Order order = getOrderById(id);
        order.setStatus(status);
        
        Order updatedOrder = orderRepository.save(order);
        log.info("Order {} status updated to: {}", updatedOrder.getOrderId(), status);
        
        return updatedOrder;
    }
    
    /**
     * Обробити платіж за замовлення
     */
    public Order processPayment(String orderId, BigDecimal amount) {
        log.info("Processing payment for order: {} with amount: {}", orderId, amount);
        
        Order order = getOrderByOrderId(orderId);
        
        // Перевірка достатності коштів
        if (!paymentService.hasSufficientFunds(order.getCustomerId(), amount)) {
            throw new InsufficientFundsException("Insufficient funds for order: " + orderId);
        }
        
        // Обробка платежу
        paymentService.processPayment(order.getCustomerId(), amount);
        
        // Оновлення статусу замовлення
        order.setStatus(OrderStatus.PAID);
        Order updatedOrder = orderRepository.save(order);
        
        // Відправка сповіщення
        notificationService.sendOrderConfirmation(order);
        
        log.info("Payment processed successfully for order: {}", orderId);
        return updatedOrder;
    }
    
    /**
     * Скасувати замовлення
     */
    public Order cancelOrder(String orderId) {
        log.info("Cancelling order: {}", orderId);
        
        Order order = getOrderByOrderId(orderId);
        
        if (order.getStatus() == OrderStatus.DELIVERED) {
            throw new IllegalStateException("Cannot cancel delivered order: " + orderId);
        }
        
        order.setStatus(OrderStatus.CANCELLED);
        Order updatedOrder = orderRepository.save(order);
        
        notificationService.sendOrderCancellation(order);
        
        log.info("Order cancelled: {}", orderId);
        return updatedOrder;
    }
    
    /**
     * Отримати статистику замовлень
     */
    @Transactional(readOnly = true)
    public OrderStatistics getOrderStatistics() {
        long totalOrders = orderRepository.count();
        long newOrders = orderRepository.countByStatus(OrderStatus.NEW);
        long paidOrders = orderRepository.countByStatus(OrderStatus.PAID);
        long cancelledOrders = orderRepository.countByStatus(OrderStatus.CANCELLED);
        
        return OrderStatistics.builder()
                .totalOrders(totalOrders)
                .newOrders(newOrders)
                .paidOrders(paidOrders)
                .cancelledOrders(cancelledOrders)
                .build();
    }
    
    /**
     * Генерувати унікальний orderId
     */
    public String generateOrderId() {
        return "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
