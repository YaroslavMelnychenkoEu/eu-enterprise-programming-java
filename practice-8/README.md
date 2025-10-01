# Практична робота №8: Розробка системи оброблення подій з використанням Queue

**Дисципліна:** Enterprise програмування Java  
**Група:** КН-261; КБ-263  
**Викладач:** д.т.н., професор, Сушинський Орест Євгенович

## Мета роботи

Отримання практичних навичок розробки асинхронних систем обробки даних з використанням черг повідомлень, реалізація пріоритезації завдань та балансування навантаження в Enterprise Java-застосунках.

## Опис системи

Система обробки замовлень інтернет-магазину з використанням RabbitMQ для асинхронної обробки подій. 

### Основні можливості

- ✅ Асинхронна обробка замовлень через RabbitMQ
- ✅ Чотири рівні пріоритетів: URGENT, VIP, STANDARD, BACKGROUND
- ✅ Балансування навантаження між обробниками (Round-robin)
- ✅ Моніторинг стану черг у реальному часі
- ✅ Збір метрик продуктивності (Micrometer)
- ✅ Обробка помилок та повторні спроби
- ✅ REST API для управління та тестування

### Вимоги до обробки

| Пріоритет | Макс. час очікування | Конкурентність |
|-----------|---------------------|----------------|
| URGENT    | 5 секунд            | 3-6 потоків    |
| VIP       | 30 секунд           | 2-5 потоків    |
| STANDARD  | 2 хвилини           | 2-4 потоки     |
| BACKGROUND| Необмежено          | 2-4 потоки     |

## Структура проєкту

```
practice-8/
├── docker-compose.yml              # RabbitMQ контейнер
├── pom.xml                         # Maven конфігурація
├── src/main/
│   ├── java/ua/edu/practice/
│   │   ├── OrderProcessingApplication.java  # Головний клас
│   │   ├── config/
│   │   │   └── RabbitMQConfig.java         # Конфігурація черг та exchanges
│   │   ├── model/
│   │   │   ├── OrderEvent.java             # Модель події замовлення
│   │   │   └── OrderPriority.java          # Енумерація пріоритетів
│   │   ├── service/
│   │   │   ├── QueueService.java           # Сервіс роботи з чергами
│   │   │   ├── OrderProcessor.java         # Обробник замовлень
│   │   │   ├── LoadBalancer.java           # Балансувальник навантаження
│   │   │   ├── QueueMonitor.java           # Моніторинг черг
│   │   │   └── MetricsCollector.java       # Збір метрик
│   │   └── controller/
│   │       └── OrderController.java        # REST API контролер
│   └── resources/
│       └── application.yml                  # Налаштування Spring Boot
└── README.md
```

## Встановлення та запуск

### Передумови

- Java 17+
- Maven 3.6+
- Docker та Docker Compose

### Крок 1: Запуск RabbitMQ

```bash
cd practice-8
docker-compose up -d
```

Перевірка статусу:
```bash
docker-compose ps
```

RabbitMQ Management UI буде доступний за адресою: http://localhost:15672
- **Username:** admin
- **Password:** admin

### Крок 2: Компіляція проєкту

```bash
mvn clean package
```

### Крок 3: Запуск застосунку

```bash
mvn spring-boot:run
```

Або через JAR:
```bash
java -jar target/practice-8-1.0.0.jar
```

## API Endpoints

### Створення замовлення

```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "priority": "VIP",
    "payload": {
      "customerId": "12345",
      "items": ["item1", "item2"],
      "total": 199.99
    }
  }'
```

### Масове створення замовлень (для тестування)

```bash
# Створити 1000 стандартних замовлень
curl -X POST "http://localhost:8080/api/orders/bulk?count=1000&priority=STANDARD"

# Створити 500 VIP замовлень
curl -X POST "http://localhost:8080/api/orders/bulk?count=500&priority=VIP"

# Створити 100 термінових замовлень
curl -X POST "http://localhost:8080/api/orders/bulk?count=100&priority=URGENT"
```

### Тестування всіх пріоритетів

```bash
curl -X POST http://localhost:8080/api/orders/test
```

### Отримання статистики

```bash
# Статистика по всіх чергах
curl http://localhost:8080/api/orders/stats

# Розмір конкретної черги
curl http://localhost:8080/api/orders/queue/URGENT/size
```

### Метрики Actuator

```bash
# Загальні метрики
curl http://localhost:8080/actuator/metrics

# Конкретна метрика
curl http://localhost:8080/actuator/metrics/orders.processed.total
```

## Тестування продуктивності

### Перевірка обробки 1000 повідомлень на секунду

```bash
# Відкрийте 2 термінали

# Термінал 1: Створення замовлень
for i in {1..10}; do
  curl -X POST "http://localhost:8080/api/orders/bulk?count=100&priority=STANDARD" &
done

# Термінал 2: Моніторинг
watch -n 1 'curl -s http://localhost:8080/api/orders/stats'
```

### Перевірка пріоритезації

```bash
# Створіть замовлення різних пріоритетів одночасно
curl -X POST "http://localhost:8080/api/orders/bulk?count=50&priority=URGENT" &
curl -X POST "http://localhost:8080/api/orders/bulk?count=50&priority=VIP" &
curl -X POST "http://localhost:8080/api/orders/bulk?count=100&priority=STANDARD" &

# Перевірте логи - термінові замовлення повинні оброблятися першими
```

## Моніторинг

### RabbitMQ Management UI

1. Відкрийте http://localhost:15672
2. Авторизуйтесь (admin/admin)
3. Перейдіть до вкладки "Queues"
4. Спостерігайте за:
   - Кількістю повідомлень у кожній черзі
   - Швидкістю публікації/споживання
   - Кількістю активних споживачів

### Логи застосунку

```bash
# Моніторинг логів у реальному часі
tail -f logs/spring.log

# Або у консолі Spring Boot
```

### Метрики

Доступні метрики:
- `orders.enqueued` - кількість доданих замовлень
- `orders.processed.total` - загальна кількість оброблених
- `orders.processed.success` - успішно оброблені
- `orders.processed.error` - помилки обробки
- `orders.processing.time` - час обробки
- `queue.size` - розмір черг

## Технічна реалізація

### Пріоритетні черги

Кожна черга налаштована з відповідним пріоритетом:
- URGENT: priority=10
- VIP: priority=8
- STANDARD: priority=5
- BACKGROUND: priority=1

### Балансування навантаження

- Round-robin розподіл між партиціями
- Виділена партиція для термінових замовлень
- Динамічне масштабування обробників

### Обробка помилок

- Автоматичні повторні спроби (до 3 разів)
- Exponential backoff стратегія
- Логування всіх помилок
- Збір метрик по помилках

## Перевірка вимог

- ✅ Обробка мінімум 1000 повідомлень/сек - використовуйте bulk endpoint
- ✅ Максимальний час очікування реалізовано в OrderProcessor
- ✅ Обробка помилок та перевантажень - retry механізм у QueueService
- ✅ Метрики продуктивності - MetricsCollector + Actuator
- ✅ Документація коду - JavaDoc коментарі у всіх класах

## Зупинка системи

```bash
# Зупинити Spring Boot (Ctrl+C)

# Зупинити RabbitMQ
docker-compose down

# Повністю видалити дані (необов'язково)
docker-compose down -v
```

## Висновки

Система демонструє:
1. Правильну організацію асинхронної обробки даних
2. Ефективну пріоритезацію завдань
3. Масштабованість та відмовостійкість
4. Моніторинг та збір метрик
5. Enterprise-ready архітектуру

## Автор

КН-261 / КБ-263  
Практична робота виконана в рамках курсу "Enterprise програмування Java"

