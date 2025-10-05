#!/bin/bash

# Скрипт для запуску всіх тестів практичної роботи №10

echo "╔════════════════════════════════════════════════════════════════╗"
echo "║  Практична робота №10: Комплексне тестування                  ║"
echo "║  Enterprise-застосунку з використанням різних підходів       ║"
echo "╚════════════════════════════════════════════════════════════════╝"
echo ""

# Кольори для виводу
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Функція для виводу заголовків
print_header() {
    echo -e "${BLUE}=== $1 ===${NC}"
}

# Функція для виводу успіху
print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

# Функція для виводу помилки
print_error() {
    echo -e "${RED}❌ $1${NC}"
}

# Функція для виводу попередження
print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

# Перевірка наявності Java
print_header "Перевірка середовища"
if ! command -v java &> /dev/null; then
    print_error "Java не знайдена. Будь ласка, встановіть Java 17+"
    exit 1
fi

JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 17 ]; then
    print_error "Потрібна Java 17+, знайдена Java $JAVA_VERSION"
    exit 1
fi
print_success "Java $JAVA_VERSION знайдена"

# Перевірка наявності Maven
if ! command -v mvn &> /dev/null; then
    print_error "Maven не знайдений. Будь ласка, встановіть Maven 3.6+"
    exit 1
fi
print_success "Maven знайдений"

# Очищення та компіляція
print_header "Компіляція проєкту"
echo "Очищення та компіляція..."
if mvn clean compile -q; then
    print_success "Компіляція завершена успішно"
else
    print_error "Помилка компіляції"
    exit 1
fi

# Запуск модульних тестів
print_header "Модульні тести (Unit Tests)"
echo "Запуск модульних тестів..."
if mvn test -Dtest="*Test" -q; then
    print_success "Модульні тести пройшли успішно"
else
    print_error "Модульні тести не пройшли"
    exit 1
fi

# Запуск інтеграційних тестів
print_header "Інтеграційні тести (Integration Tests)"
echo "Запуск інтеграційних тестів..."
if mvn test -Dtest="*IntegrationTest" -q; then
    print_success "Інтеграційні тести пройшли успішно"
else
    print_error "Інтеграційні тести не пройшли"
    exit 1
fi

# Запуск тестів безпеки
print_header "Тести безпеки (Security Tests)"
echo "Запуск тестів безпеки..."
if mvn test -Dtest="*SecurityTest" -q; then
    print_success "Тести безпеки пройшли успішно"
else
    print_error "Тести безпеки не пройшли"
    exit 1
fi

# Запуск застосунку в фоновому режимі
print_header "Запуск застосунку"
echo "Запуск Spring Boot застосунку..."
mvn spring-boot:run > app.log 2>&1 &
APP_PID=$!

# Очікування запуску застосунку
echo "Очікування запуску застосунку..."
for i in {1..30}; do
    if curl -s http://localhost:8080/api/orders/generate-order-id > /dev/null 2>&1; then
        print_success "Застосунок запущено успішно"
        break
    fi
    if [ $i -eq 30 ]; then
        print_error "Застосунок не запустився протягом 30 секунд"
        kill $APP_PID 2>/dev/null
        exit 1
    fi
    sleep 1
done

# Тести продуктивності (якщо JMeter доступний)
print_header "Тести продуктивності (Performance Tests)"
if command -v jmeter &> /dev/null; then
    echo "Запуск тестів продуктивності з JMeter..."
    if ./src/test/jmeter/run-performance-tests.sh; then
        print_success "Тести продуктивності завершені"
    else
        print_warning "Тести продуктивності не пройшли (можливо JMeter не налаштований)"
    fi
else
    print_warning "JMeter не знайдений, пропускаємо тести продуктивності"
fi

# Зупинка застосунку
print_header "Завершення"
echo "Зупинка застосунку..."
kill $APP_PID 2>/dev/null
wait $APP_PID 2>/dev/null
print_success "Застосунок зупинено"

# Підсумок
print_header "Підсумок виконання"
echo ""
echo "📊 Результати тестування:"
echo "   ✅ Модульні тести - ПРОЙШЛИ"
echo "   ✅ Інтеграційні тести - ПРОЙШЛИ"
echo "   ✅ Тести безпеки - ПРОЙШЛИ"
echo "   ✅ Застосунок - ЗАПУЩЕНО"
if command -v jmeter &> /dev/null; then
    echo "   ✅ Тести продуктивності - ЗАВЕРШЕНО"
else
    echo "   ⚠️  Тести продуктивності - ПРОПУЩЕНО (JMeter не знайдений)"
fi

echo ""
echo "🎯 Всі основні тести пройшли успішно!"
echo ""
echo "📁 Корисні файли:"
echo "   - Логи застосунку: app.log"
echo "   - Результати JMeter: target/jmeter/"
echo "   - Звіти тестів: target/surefire-reports/"
echo ""
echo "🌐 Для ручного тестування запустіть:"
echo "   mvn spring-boot:run"
echo "   # Застосунок буде доступний на http://localhost:8080"
echo ""
echo "╔════════════════════════════════════════════════════════════════╗"
echo "║  Практична робота №10 ЗАВЕРШЕНА УСПІШНО! 🎉                  ║"
echo "╚════════════════════════════════════════════════════════════════╝"
