#!/bin/bash

echo "=== Тестування системи обробки подій ==="
echo ""

echo "1. Перевірка стану системи..."
curl -s http://localhost:8080/actuator/health | jq .
echo ""

echo "2. Створення тестових замовлень різних пріоритетів..."
curl -s -X POST http://localhost:8080/api/orders/test | jq .
echo ""

echo "3. Очікування 3 секунди..."
sleep 3
echo ""

echo "4. Статистика після початкової обробки:"
curl -s http://localhost:8080/api/orders/stats | jq .
echo ""

echo "5. Створення 500 стандартних замовлень..."
curl -s -X POST "http://localhost:8080/api/orders/bulk?count=500&priority=STANDARD" | jq .
echo ""

echo "6. Створення 100 VIP замовлень..."
curl -s -X POST "http://localhost:8080/api/orders/bulk?count=100&priority=VIP" | jq .
echo ""

echo "7. Створення 50 термінових замовлень..."
curl -s -X POST "http://localhost:8080/api/orders/bulk?count=50&priority=URGENT" | jq .
echo ""

echo "8. Очікування 5 секунд для обробки..."
sleep 5
echo ""

echo "9. Фінальна статистика:"
curl -s http://localhost:8080/api/orders/stats | jq .
echo ""

echo "10. Метрики обробки:"
echo "Всього оброблено:"
curl -s http://localhost:8080/actuator/metrics/orders.processed.total | jq '.measurements[0].value'
echo ""
echo "Успішно оброблено:"
curl -s http://localhost:8080/actuator/metrics/orders.processed.success | jq '.measurements[0].value'
echo ""
echo "Помилок:"
curl -s http://localhost:8080/actuator/metrics/orders.processed.error | jq '.measurements[0].value'
echo ""

echo "=== Тестування завершено! ==="
