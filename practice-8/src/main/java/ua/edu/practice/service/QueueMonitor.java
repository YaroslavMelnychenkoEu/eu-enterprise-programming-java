package ua.edu.practice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ua.edu.practice.model.OrderEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Моніторинг стану черг
 */
@Service
@Slf4j
public class QueueMonitor {

    private final MetricsCollector metricsCollector;
    private final Map<String, AtomicInteger> queueSizes = new ConcurrentHashMap<>();
    private final Map<String, AtomicInteger> processedCounts = new ConcurrentHashMap<>();

    public QueueMonitor(MetricsCollector metricsCollector) {
        this.metricsCollector = metricsCollector;
    }

    /**
     * Реєструє додавання події в чергу
     */
    public void recordEnqueue(OrderEvent event) {
        String topic = event.getPriority().name();
        int newSize = queueSizes.computeIfAbsent(topic, k -> new AtomicInteger(0))
                .incrementAndGet();
        
        metricsCollector.recordQueueSize(topic, newSize);
        
        log.debug("Enqueued order {} to queue {}, new size: {}", 
                event.getOrderId(), topic, newSize);
    }

    /**
     * Реєструє видалення події з черги
     */
    public void recordDequeue(OrderEvent event) {
        String topic = event.getPriority().name();
        int newSize = queueSizes.computeIfAbsent(topic, k -> new AtomicInteger(0))
                .decrementAndGet();
        
        // Запобігаємо негативним значенням
        if (newSize < 0) {
            queueSizes.get(topic).set(0);
            newSize = 0;
        }
        
        metricsCollector.recordQueueSize(topic, newSize);
        
        // Збільшуємо лічильник оброблених
        int processedCount = processedCounts.computeIfAbsent(topic, k -> new AtomicInteger(0))
                .incrementAndGet();
        
        log.debug("Dequeued order {} from queue {}, new size: {}, total processed: {}", 
                event.getOrderId(), topic, newSize, processedCount);
    }

    /**
     * Отримує поточний розмір черги
     */
    public int getQueueSize(String queueName) {
        return queueSizes.getOrDefault(queueName, new AtomicInteger(0)).get();
    }

    /**
     * Отримує кількість оброблених повідомлень
     */
    public int getProcessedCount(String queueName) {
        return processedCounts.getOrDefault(queueName, new AtomicInteger(0)).get();
    }

    /**
     * Отримує статистику по всіх чергах
     */
    public Map<String, QueueStats> getAllStats() {
        Map<String, QueueStats> stats = new ConcurrentHashMap<>();
        
        queueSizes.forEach((queue, size) -> {
            int processed = processedCounts.getOrDefault(queue, new AtomicInteger(0)).get();
            stats.put(queue, new QueueStats(queue, size.get(), processed));
        });
        
        return stats;
    }

    /**
     * Скидає статистику (для тестування)
     */
    public void reset() {
        queueSizes.clear();
        processedCounts.clear();
        log.info("QueueMonitor statistics reset");
    }

    /**
     * Статистика черги
     */
    public record QueueStats(String queueName, int currentSize, int totalProcessed) {
    }
}

