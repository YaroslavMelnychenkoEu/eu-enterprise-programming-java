package ua.edu.practice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ua.edu.practice.model.Order;

/**
 * Сервіс для відправки сповіщень
 */
@Service
@Slf4j
public class NotificationService {
    
    /**
     * Відправити підтвердження замовлення
     */
    public void sendOrderConfirmation(Order order) {
        log.info("Sending order confirmation for order: {} to customer: {}", 
                order.getOrderId(), order.getCustomerId());
        
        // Імітація відправки email/SMS
        try {
            Thread.sleep(50); // Імітація затримки
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        log.info("Order confirmation sent successfully for order: {}", order.getOrderId());
    }
    
    /**
     * Відправити сповіщення про скасування замовлення
     */
    public void sendOrderCancellation(Order order) {
        log.info("Sending order cancellation notification for order: {} to customer: {}", 
                order.getOrderId(), order.getCustomerId());
        
        // Імітація відправки email/SMS
        try {
            Thread.sleep(50); // Імітація затримки
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        log.info("Order cancellation notification sent successfully for order: {}", order.getOrderId());
    }
    
    /**
     * Відправити сповіщення про зміну статусу
     */
    public void sendStatusUpdate(Order order) {
        log.info("Sending status update for order: {} to customer: {}", 
                order.getOrderId(), order.getCustomerId());
        
        // Імітація відправки email/SMS
        try {
            Thread.sleep(50); // Імітація затримки
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        log.info("Status update sent successfully for order: {}", order.getOrderId());
    }
}
