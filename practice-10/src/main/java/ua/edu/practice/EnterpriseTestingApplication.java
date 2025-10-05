package ua.edu.practice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Головний клас застосунку для комплексного тестування Enterprise-застосунку
 * 
 * Практична робота №10: Комплексне тестування Enterprise-застосунку з використанням різних підходів
 * 
 * Включає:
 * - Модульне тестування (Unit Tests)
 * - Інтеграційне тестування (Integration Tests)
 * - Тестування продуктивності (Performance Tests)
 * - Тестування безпеки (Security Tests)
 * 
 * @author КН-261; КБ-263
 */
@SpringBootApplication
public class EnterpriseTestingApplication {

    public static void main(String[] args) {
        SpringApplication.run(EnterpriseTestingApplication.class, args);
        System.out.println("""
                
                ╔════════════════════════════════════════════════════════════════╗
                ║  Enterprise Testing Application - Practice Work #10           ║
                ║  Комплексне тестування Enterprise-застосунку                  ║
                ╠════════════════════════════════════════════════════════════════╣
                ║  REST API:         http://localhost:8080                      ║
                ║  H2 Console:       http://localhost:8080/h2-console           ║
                ║  JDBC URL:         jdbc:h2:mem:testdb                         ║
                ║  Username:         sa                                          ║
                ║  Password:         password                                     ║
                ╠════════════════════════════════════════════════════════════════╣
                ║  Test Users:                                                   ║
                ║    admin / admin (ADMIN role)                                  ║
                ║    user / user (USER role)                                     ║
                ╠════════════════════════════════════════════════════════════════╣
                ║  API Endpoints:                                                ║
                ║    POST /api/orders        - Створити замовлення              ║
                ║    GET  /api/orders        - Всі замовлення (ADMIN)           ║
                ║    GET  /api/orders/{id}   - Замовлення за ID                 ║
                ║    POST /api/orders/{id}/payment - Обробити платіж            ║
                ║    GET  /api/orders/statistics - Статистика (ADMIN)           ║
                ╚════════════════════════════════════════════════════════════════╝
                """);
    }
}
