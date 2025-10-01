package ua.edu.practice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ua.edu.practice.model.OrderEvent;
import ua.edu.practice.model.OrderPriority;

/**
 * Сервіс для роботи з чергами повідомлень
 */
@Service
@Slf4j
public class QueueService {

    private final RabbitTemplate rabbitTemplate;
    private final LoadBalancer loadBalancer;
    private final QueueMonitor queueMonitor;
    private final MetricsCollector metricsCollector;

    @Value("${queue.order.exchanges.main}")
    private String mainExchange;

    @Value("${queue.processing.retry-attempts}")
    private int maxRetryAttempts;

    public QueueService(RabbitTemplate rabbitTemplate,
                        LoadBalancer loadBalancer,
                        QueueMonitor queueMonitor,
                        MetricsCollector metricsCollector) {
        this.rabbitTemplate = rabbitTemplate;
        this.loadBalancer = loadBalancer;
        this.queueMonitor = queueMonitor;
        this.metricsCollector = metricsCollector;
    }

    /**
     * Додає замовлення в чергу з урахуванням пріоритету
     */
    public void enqueueOrder(OrderEvent event) {
        try {
            String routingKey = loadBalancer.determineRoutingKey(event.getPriority());
            int priority = loadBalancer.determinePriority(event.getPriority());
            
            // Відправка повідомлення з пріоритетом
            rabbitTemplate.convertAndSend(
                    mainExchange,
                    routingKey,
                    event,
                    message -> {
                        message.getMessageProperties().setPriority(priority);
                        return message;
                    }
            );
            
            queueMonitor.recordEnqueue(event);
            metricsCollector.recordEnqueue(event);
            
            log.info("Successfully enqueued order: {} with priority: {} to routing key: {}", 
                    event.getOrderId(), event.getPriority(), routingKey);
            
        } catch (Exception e) {
            log.error("Failed to enqueue order: {}", event.getOrderId(), e);
            handleEnqueueError(event, e);
        }
    }

    /**
     * Обробка помилки додавання в чергу
     */
    private void handleEnqueueError(OrderEvent event, Exception e) {
        if (event.getRetryCount() < maxRetryAttempts) {
            event.setRetryCount(event.getRetryCount() + 1);
            metricsCollector.recordRetry(event);
            
            log.warn("Retrying enqueue for order: {} (attempt {})", 
                    event.getOrderId(), event.getRetryCount());
            
            // Exponential backoff
            try {
                Thread.sleep(1000L * event.getRetryCount());
                enqueueOrder(event);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                log.error("Retry interrupted for order: {}", event.getOrderId(), ie);
            }
        } else {
            log.error("Max retry attempts reached for order: {}", event.getOrderId());
            metricsCollector.recordProcessingError(event, e);
        }
    }

    /**
     * Визначає тему на основі пріоритету
     */
    private String determineTopicForPriority(OrderPriority priority) {
        return switch (priority) {
            case URGENT -> "urgent-orders";
            case VIP -> "vip-orders";
            case STANDARD -> "standard-orders";
            case BACKGROUND -> "background-tasks";
        };
    }
}

