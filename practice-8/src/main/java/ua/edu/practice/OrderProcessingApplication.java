package ua.edu.practice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Головний клас застосунку для обробки замовлень з використанням черг
 * 
 * Практична робота №8: Розробка системи оброблення подій з використанням Queue
 * 
 * Особливості системи:
 * - Асинхронна обробка замовлень через RabbitMQ
 * - Пріоритезація: URGENT, VIP, STANDARD, BACKGROUND
 * - Балансування навантаження
 * - Моніторинг та збір метрик
 * - Гарантована обробка повідомлень
 * 
 * @author КН-261; КБ-263
 */
@SpringBootApplication
public class OrderProcessingApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderProcessingApplication.class, args);
        System.out.println("""
                
                ╔════════════════════════════════════════════════════════════════╗
                ║  Order Processing System - Practice Work #8                   ║
                ║  Система обробки замовлень з використанням черг (RabbitMQ)    ║
                ╠════════════════════════════════════════════════════════════════╣
                ║  REST API:         http://localhost:8080                      ║
                ║  RabbitMQ UI:      http://localhost:15672                     ║
                ║  Credentials:      admin / admin                              ║
                ║  Actuator:         http://localhost:8080/actuator             ║
                ╠════════════════════════════════════════════════════════════════╣
                ║  API Endpoints:                                                ║
                ║    POST /api/orders        - Створити замовлення              ║
                ║    POST /api/orders/bulk   - Масове створення                 ║
                ║    POST /api/orders/test   - Тестування всіх пріоритетів     ║
                ║    GET  /api/orders/stats  - Статистика черг                  ║
                ╚════════════════════════════════════════════════════════════════╝
                """);
    }
}

