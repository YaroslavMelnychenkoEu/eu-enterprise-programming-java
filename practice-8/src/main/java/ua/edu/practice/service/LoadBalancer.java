package ua.edu.practice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ua.edu.practice.model.OrderEvent;
import ua.edu.practice.model.OrderPriority;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Балансування навантаження між обробниками
 */
@Service
@Slf4j
public class LoadBalancer {

    private final AtomicInteger counter = new AtomicInteger(0);
    private final int partitionCount;

    public LoadBalancer(@Value("${queue.processing.threads}") int partitionCount) {
        this.partitionCount = partitionCount;
        log.info("LoadBalancer initialized with {} partitions", partitionCount);
    }

    /**
     * Визначає партицію для розподілу навантаження
     * Round-robin стратегія з урахуванням пріоритетів
     */
    public String determinePartition(OrderEvent event) {
        return switch (event.getPriority()) {
            case URGENT -> "partition-0"; // Виділена партиція для термінових замовлень
            case VIP -> "partition-" + (counter.incrementAndGet() % (partitionCount - 1) + 1);
            default -> "partition-" + (counter.incrementAndGet() % partitionCount);
        };
    }

    /**
     * Визначає routing key на основі пріоритету
     */
    public String determineRoutingKey(OrderPriority priority) {
        return switch (priority) {
            case URGENT -> "order.urgent";
            case VIP -> "order.vip";
            case STANDARD -> "order.standard";
            case BACKGROUND -> "order.background";
        };
    }

    /**
     * Визначає пріоритет повідомлення для RabbitMQ
     */
    public int determinePriority(OrderPriority priority) {
        return switch (priority) {
            case URGENT -> 10;
            case VIP -> 8;
            case STANDARD -> 5;
            case BACKGROUND -> 1;
        };
    }

    /**
     * Повертає поточне навантаження
     */
    public int getCurrentLoad() {
        return counter.get();
    }

    /**
     * Скидає лічильник (для тестування)
     */
    public void reset() {
        counter.set(0);
        log.info("LoadBalancer counter reset");
    }
}

