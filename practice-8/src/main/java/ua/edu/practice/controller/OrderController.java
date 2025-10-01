package ua.edu.practice.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ua.edu.practice.model.OrderEvent;
import ua.edu.practice.model.OrderPriority;
import ua.edu.practice.service.QueueMonitor;
import ua.edu.practice.service.QueueService;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * REST контролер для управління замовленнями та моніторингу
 */
@RestController
@RequestMapping("/api/orders")
@Slf4j
public class OrderController {

    private final QueueService queueService;
    private final QueueMonitor queueMonitor;

    public OrderController(QueueService queueService, QueueMonitor queueMonitor) {
        this.queueService = queueService;
        this.queueMonitor = queueMonitor;
    }

    /**
     * Створення нового замовлення
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createOrder(@RequestBody CreateOrderRequest request) {
        String orderId = UUID.randomUUID().toString();
        
        OrderEvent event = OrderEvent.builder()
                .orderId(orderId)
                .priority(request.priority())
                .createdAt(LocalDateTime.now())
                .payload(request.payload())
                .retryCount(0)
                .status("PENDING")
                .build();
        
        queueService.enqueueOrder(event);
        
        log.info("Created order: {} with priority: {}", orderId, request.priority());
        
        Map<String, Object> response = new HashMap<>();
        response.put("orderId", orderId);
        response.put("priority", request.priority());
        response.put("status", "ENQUEUED");
        response.put("createdAt", event.getCreatedAt());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Масове створення замовлень для тестування навантаження
     */
    @PostMapping("/bulk")
    public ResponseEntity<Map<String, Object>> createBulkOrders(
            @RequestParam(defaultValue = "100") int count,
            @RequestParam(defaultValue = "STANDARD") OrderPriority priority) {
        
        log.info("Creating {} orders with priority: {}", count, priority);
        
        for (int i = 0; i < count; i++) {
            String orderId = UUID.randomUUID().toString();
            
            Map<String, Object> payload = new HashMap<>();
            payload.put("item", "Product-" + i);
            payload.put("quantity", i + 1);
            payload.put("price", (i + 1) * 10.0);
            
            OrderEvent event = OrderEvent.builder()
                    .orderId(orderId)
                    .priority(priority)
                    .createdAt(LocalDateTime.now())
                    .payload(payload)
                    .retryCount(0)
                    .status("PENDING")
                    .build();
            
            queueService.enqueueOrder(event);
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("created", count);
        response.put("priority", priority);
        response.put("status", "ENQUEUED");
        
        return ResponseEntity.ok(response);
    }

    /**
     * Отримання статистики черг
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, QueueMonitor.QueueStats>> getStats() {
        Map<String, QueueMonitor.QueueStats> stats = queueMonitor.getAllStats();
        return ResponseEntity.ok(stats);
    }

    /**
     * Отримання розміру конкретної черги
     */
    @GetMapping("/queue/{queueName}/size")
    public ResponseEntity<Map<String, Object>> getQueueSize(@PathVariable String queueName) {
        int size = queueMonitor.getQueueSize(queueName);
        int processed = queueMonitor.getProcessedCount(queueName);
        
        Map<String, Object> response = new HashMap<>();
        response.put("queueName", queueName);
        response.put("currentSize", size);
        response.put("totalProcessed", processed);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Тестування всіх пріоритетів
     */
    @PostMapping("/test")
    public ResponseEntity<Map<String, Object>> testAllPriorities() {
        log.info("Testing all priority levels");
        
        Map<String, Integer> created = new HashMap<>();
        
        for (OrderPriority priority : OrderPriority.values()) {
            for (int i = 0; i < 10; i++) {
                String orderId = UUID.randomUUID().toString();
                
                Map<String, Object> payload = new HashMap<>();
                payload.put("test", true);
                payload.put("priority", priority.name());
                payload.put("index", i);
                
                OrderEvent event = OrderEvent.builder()
                        .orderId(orderId)
                        .priority(priority)
                        .createdAt(LocalDateTime.now())
                        .payload(payload)
                        .retryCount(0)
                        .status("PENDING")
                        .build();
                
                queueService.enqueueOrder(event);
            }
            created.put(priority.name(), 10);
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Test orders created for all priorities");
        response.put("created", created);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Скидання статистики (для тестування)
     */
    @PostMapping("/reset")
    public ResponseEntity<Map<String, String>> resetStats() {
        queueMonitor.reset();
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Statistics reset successfully");
        
        return ResponseEntity.ok(response);
    }

    /**
     * DTO для створення замовлення
     */
    public record CreateOrderRequest(
            OrderPriority priority,
            Map<String, Object> payload
    ) {}
}

