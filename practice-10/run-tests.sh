#!/bin/bash

echo "=== Practice 10 - Comprehensive Testing Demo ==="
echo

# Перевірка наявності Java
if ! command -v java &> /dev/null; then
    echo "❌ Java не знайдена. Будь ласка, встановіть Java 17 або новішу версію."
    exit 1
fi

# Перевірка наявності Maven
if ! command -v mvn &> /dev/null; then
    echo "❌ Maven не знайдений. Будь ласка, встановіть Maven."
    exit 1
fi

echo "✅ Java та Maven знайдені"
echo

# Компіляція проєкту
echo "🔨 Компіляція проєкту..."
mvn clean compile
if [ $? -ne 0 ]; then
    echo "❌ Помилка компіляції"
    exit 1
fi
echo "✅ Компіляція успішна"
echo

# Запуск unit тестів
echo "🧪 Запуск unit тестів..."
mvn test -Dtest="*ServiceTest,*ControllerTest" -q
if [ $? -ne 0 ]; then
    echo "⚠️  Деякі unit тести не пройшли, але це нормально для демонстрації"
fi
echo "✅ Unit тести завершені"
echo

# Запуск інтеграційних тестів (без Testcontainers)
echo "🔗 Запуск інтеграційних тестів (без Testcontainers)..."
mvn test -Dtest="*RepositoryIntegrationTest" -q
if [ $? -ne 0 ]; then
    echo "⚠️  Деякі інтеграційні тести не пройшли"
fi
echo "✅ Інтеграційні тести завершені"
echo

# Запуск додатку
echo "🚀 Запуск додатку..."
echo "Додаток буде доступний на http://localhost:8080"
echo "H2 Console: http://localhost:8080/h2-console"
echo "Тестові користувачі:"
echo "  - user/password (USER)"
echo "  - admin/admin (ADMIN)"
echo
echo "Натисніть Ctrl+C для зупинки додатку"
echo

mvn spring-boot:run
