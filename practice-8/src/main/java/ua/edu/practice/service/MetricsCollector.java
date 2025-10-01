package ua.edu.practice.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ua.edu.practice.model.OrderEvent;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Збір метрик продуктивності системи
 */
@Service
@Slf4j
public class MetricsCollector {

    private final MeterRegistry meterRegistry;
    private final Map<String, LocalDateTime> processingStartTimes = new ConcurrentHashMap<>();
    
    private final Counter totalProcessedCounter;
    private final Counter successCounter;
    private final Counter errorCounter;

    public MetricsCollector(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.totalProcessedCounter = Counter.builder("orders.processed.total")
                .description("Загальна кількість оброблених замовлень")
                .register(meterRegistry);
        this.successCounter = Counter.builder("orders.processed.success")
                .description("Успішно оброблені замовлення")
                .register(meterRegistry);
        this.errorCounter = Counter.builder("orders.processed.error")
                .description("Помилки обробки замовлень")
                .register(meterRegistry);
    }

    public void recordEnqueue(OrderEvent event) {
        Counter.builder("orders.enqueued")
                .tag("priority", event.getPriority().name())
                .description("Кількість доданих замовлень у чергу")
                .register(meterRegistry)
                .increment();
        
        log.debug("Recorded enqueue for order: {} with priority: {}", 
                event.getOrderId(), event.getPriority());
    }

    public void recordProcessingStart(OrderEvent event) {
        processingStartTimes.put(event.getOrderId(), LocalDateTime.now());
        totalProcessedCounter.increment();
        
        log.debug("Started processing order: {}", event.getOrderId());
    }

    public void recordProcessingSuccess(OrderEvent event) {
        LocalDateTime startTime = processingStartTimes.remove(event.getOrderId());
        if (startTime != null) {
            Duration duration = Duration.between(startTime, LocalDateTime.now());
            
            Timer.builder("orders.processing.time")
                    .tag("priority", event.getPriority().name())
                    .description("Час обробки замовлення")
                    .register(meterRegistry)
                    .record(duration);
        }
        
        successCounter.increment();
        
        Counter.builder("orders.processed.by.priority")
                .tag("priority", event.getPriority().name())
                .tag("status", "success")
                .register(meterRegistry)
                .increment();
        
        log.debug("Successfully processed order: {} in {} ms", 
                event.getOrderId(), 
                startTime != null ? Duration.between(startTime, LocalDateTime.now()).toMillis() : "unknown");
    }

    public void recordProcessingError(OrderEvent event, Exception e) {
        processingStartTimes.remove(event.getOrderId());
        errorCounter.increment();
        
        Counter.builder("orders.processed.by.priority")
                .tag("priority", event.getPriority().name())
                .tag("status", "error")
                .tag("error", e.getClass().getSimpleName())
                .register(meterRegistry)
                .increment();
        
        log.error("Error processing order: {}", event.getOrderId(), e);
    }

    public void recordQueueSize(String queueName, int size) {
        meterRegistry.gauge("queue.size", 
                java.util.Collections.singletonList(io.micrometer.core.instrument.Tag.of("queue", queueName)), 
                size);
    }

    public void recordRetry(OrderEvent event) {
        Counter.builder("orders.retried")
                .tag("priority", event.getPriority().name())
                .tag("retry_count", String.valueOf(event.getRetryCount()))
                .description("Кількість повторних спроб")
                .register(meterRegistry)
                .increment();
        
        log.debug("Retry recorded for order: {} (attempt: {})", 
                event.getOrderId(), event.getRetryCount());
    }
}

