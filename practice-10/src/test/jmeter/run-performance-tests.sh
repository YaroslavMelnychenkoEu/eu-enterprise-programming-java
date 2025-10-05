#!/bin/bash

# Скрипт для запуску тестів продуктивності JMeter

echo "=== Запуск тестів продуктивності Order API ==="

# Перевірка чи запущений застосунок
echo "Перевірка доступності застосунку..."
if ! curl -s http://localhost:8080/api/orders/generate-order-id > /dev/null; then
    echo "❌ Застосунок не доступний на http://localhost:8080"
    echo "Будь ласка, запустіть застосунок спочатку:"
    echo "mvn spring-boot:run"
    exit 1
fi

echo "✅ Застосунок доступний"

# Створення директорій для результатів
mkdir -p target/jmeter/results
mkdir -p target/jmeter/reports

# Запуск JMeter тестів
echo "Запуск JMeter тестів..."

jmeter -n -t src/test/jmeter/order-api-test.jmx \
    -l target/jmeter/results/order-api-results.jtl \
    -e -o target/jmeter/reports \
    -JBASE_URL=http://localhost:8080 \
    -JUSERNAME=admin \
    -JPASSWORD=admin

# Перевірка результату
if [ $? -eq 0 ]; then
    echo "✅ Тести продуктивності завершені успішно"
    echo "📊 Результати збережено в:"
    echo "   - target/jmeter/results/order-api-results.jtl"
    echo "   - target/jmeter/reports/ (HTML звіт)"
    echo ""
    echo "🌐 Відкрийте target/jmeter/reports/index.html для перегляду результатів"
else
    echo "❌ Помилка при запуску тестів продуктивності"
    exit 1
fi

# Показ основних метрик
echo ""
echo "=== Основні метрики ==="
if [ -f target/jmeter/results/order-api-results.jtl ]; then
    echo "📈 Аналіз результатів:"
    
    # Підрахунок успішних запитів
    SUCCESS_COUNT=$(grep -c ",true," target/jmeter/results/order-api-results.jtl)
    TOTAL_COUNT=$(wc -l < target/jmeter/results/order-api-results.jtl)
    SUCCESS_RATE=$((SUCCESS_COUNT * 100 / TOTAL_COUNT))
    
    echo "   - Всього запитів: $TOTAL_COUNT"
    echo "   - Успішних: $SUCCESS_COUNT"
    echo "   - Відсоток успіху: $SUCCESS_RATE%"
    
    # Середній час відповіді
    if command -v awk > /dev/null; then
        AVG_RESPONSE_TIME=$(awk -F',' 'NR>1 {sum+=$2; count++} END {if(count>0) print sum/count; else print 0}' target/jmeter/results/order-api-results.jtl)
        echo "   - Середній час відповіді: ${AVG_RESPONSE_TIME}ms"
    fi
fi

echo ""
echo "🎯 Тести продуктивності завершені!"
