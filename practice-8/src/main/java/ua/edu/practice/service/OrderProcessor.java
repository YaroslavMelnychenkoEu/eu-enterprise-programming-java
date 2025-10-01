package ua.edu.practice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import ua.edu.practice.model.OrderEvent;
import ua.edu.practice.model.OrderPriority;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Обробник замовлень з різних черг
 */
@Service
@Slf4j
public class OrderProcessor {

    private final QueueMonitor queueMonitor;
    private final MetricsCollector metricsCollector;

    public OrderProcessor(QueueMonitor queueMonitor, MetricsCollector metricsCollector) {
        this.queueMonitor = queueMonitor;
        this.metricsCollector = metricsCollector;
    }

    /**
     * Обробка термінових замовлень (найвищий пріоритет)
     * Максимальний час очікування: 5 секунд
     */
    @RabbitListener(
            queues = "${queue.order.queues.urgent}",
            containerFactory = "urgentRabbitListenerContainerFactory"
    )
    public void processUrgentOrder(OrderEvent event) {
        log.info("Processing URGENT order: {}", event.getOrderId());
        processOrderWithPriority(event, OrderPriority.URGENT);
    }

    /**
     * Обробка VIP замовлень (високий пріоритет)
     * Максимальний час очікування: 30 секунд
     */
    @RabbitListener(
            queues = "${queue.order.queues.vip}",
            containerFactory = "vipRabbitListenerContainerFactory"
    )
    public void processVipOrder(OrderEvent event) {
        log.info("Processing VIP order: {}", event.getOrderId());
        processOrderWithPriority(event, OrderPriority.VIP);
    }

    /**
     * Обробка стандартних замовлень (нормальний пріоритет)
     * Максимальний час очікування: 2 хвилини
     */
    @RabbitListener(
            queues = "${queue.order.queues.standard}",
            containerFactory = "standardRabbitListenerContainerFactory"
    )
    public void processStandardOrder(OrderEvent event) {
        log.info("Processing STANDARD order: {}", event.getOrderId());
        processOrderWithPriority(event, OrderPriority.STANDARD);
    }

    /**
     * Обробка фонових задач (низький пріоритет)
     */
    @RabbitListener(
            queues = "${queue.order.queues.background}",
            containerFactory = "standardRabbitListenerContainerFactory"
    )
    public void processBackgroundTask(OrderEvent event) {
        log.info("Processing BACKGROUND task: {}", event.getOrderId());
        processOrderWithPriority(event, OrderPriority.BACKGROUND);
    }

    /**
     * Основна логіка обробки замовлення
     */
    private void processOrderWithPriority(OrderEvent event, OrderPriority priority) {
        LocalDateTime startTime = LocalDateTime.now();
        
        try {
            metricsCollector.recordProcessingStart(event);
            
            // Перевірка часу очікування в черзі
            checkWaitingTime(event, priority);
            
            // Імітація бізнес-логіки обробки замовлення
            processOrder(event);
            
            // Успішна обробка
            event.setStatus("COMPLETED");
            metricsCollector.recordProcessingSuccess(event);
            queueMonitor.recordDequeue(event);
            
            Duration processingTime = Duration.between(startTime, LocalDateTime.now());
            log.info("Successfully processed order: {} with priority: {} in {} ms",
                    event.getOrderId(), priority, processingTime.toMillis());
            
        } catch (Exception e) {
            log.error("Error processing order: {} with priority: {}", 
                    event.getOrderId(), priority, e);
            handleProcessingError(event, e);
        }
    }

    /**
     * Перевірка часу очікування в черзі
     */
    private void checkWaitingTime(OrderEvent event, OrderPriority priority) {
        Duration waitingTime = Duration.between(event.getCreatedAt(), LocalDateTime.now());
        long waitingSeconds = waitingTime.getSeconds();
        
        long maxWaitingSeconds = switch (priority) {
            case URGENT -> 5;
            case VIP -> 30;
            case STANDARD -> 120;
            case BACKGROUND -> Long.MAX_VALUE;
        };
        
        if (waitingSeconds > maxWaitingSeconds) {
            log.warn("Order {} exceeded maximum waiting time: {} seconds (max: {} seconds)",
                    event.getOrderId(), waitingSeconds, maxWaitingSeconds);
        }
        
        log.debug("Order {} waited {} seconds in queue (max: {} seconds)",
                event.getOrderId(), waitingSeconds, maxWaitingSeconds);
    }

    /**
     * Бізнес-логіка обробки замовлення
     * В реальній системі тут буде складна логіка обробки
     */
    private void processOrder(OrderEvent event) throws InterruptedException {
        // Імітація обробки замовлення
        int processingTime = switch (event.getPriority()) {
            case URGENT -> ThreadLocalRandom.current().nextInt(100, 500);
            case VIP -> ThreadLocalRandom.current().nextInt(200, 1000);
            case STANDARD -> ThreadLocalRandom.current().nextInt(500, 2000);
            case BACKGROUND -> ThreadLocalRandom.current().nextInt(1000, 3000);
        };
        
        Thread.sleep(processingTime);
        
        // Імітація можливої помилки (5% ймовірність)
        if (ThreadLocalRandom.current().nextInt(100) < 5) {
            throw new RuntimeException("Simulated processing error");
        }
        
        log.debug("Order {} processing completed after {} ms", 
                event.getOrderId(), processingTime);
    }

    /**
     * Обробка помилки
     */
    private void handleProcessingError(OrderEvent event, Exception e) {
        event.setStatus("FAILED");
        metricsCollector.recordProcessingError(event, e);
        queueMonitor.recordDequeue(event);
        
        // В реальній системі тут може бути логіка повторної спроби
        // або відправка в dead-letter queue
        log.error("Order {} processing failed: {}", event.getOrderId(), e.getMessage());
    }
}

