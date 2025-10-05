#!/bin/bash

echo "=== Practice 10 - Running with Docker ==="
echo

# Перевірка наявності Docker
if ! command -v docker &> /dev/null; then
    echo "❌ Docker не знайдений. Будь ласка, встановіть Docker."
    exit 1
fi

if ! command -v docker-compose &> /dev/null; then
    echo "❌ Docker Compose не знайдений. Будь ласка, встановіть Docker Compose."
    exit 1
fi

echo "✅ Docker та Docker Compose знайдені"
echo

# Запуск PostgreSQL
echo "🐘 Запуск PostgreSQL..."
docker-compose up -d
if [ $? -ne 0 ]; then
    echo "❌ Помилка запуску PostgreSQL"
    exit 1
fi

echo "⏳ Очікування готовності PostgreSQL..."
sleep 10

# Перевірка статусу контейнера
if ! docker-compose ps | grep -q "Up"; then
    echo "❌ PostgreSQL не запустився"
    docker-compose logs
    exit 1
fi

echo "✅ PostgreSQL запущений"
echo

# Запуск додатку з профілем test
echo "🚀 Запуск додатку з PostgreSQL..."
echo "Додаток буде доступний на http://localhost:8080"
echo "H2 Console: http://localhost:8080/h2-console"
echo "Тестові користувачі:"
echo "  - user/password (USER)"
echo "  - admin/admin (ADMIN)"
echo
echo "Натисніть Ctrl+C для зупинки додатку"
echo

mvn spring-boot:run -Dspring.profiles.active=test

# Очищення при завершенні
echo
echo "🧹 Зупинка PostgreSQL..."
docker-compose down
