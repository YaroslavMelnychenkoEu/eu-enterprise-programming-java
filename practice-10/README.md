# Practice 10 - Comprehensive Testing of an Enterprise Application

## Опис проєкту

Цей проєкт демонструє комплексне тестування enterprise-додатку з використанням різних підходів до тестування. Додаток реалізує систему управління замовленнями з REST API, базою даних та системою безпеки.

## Архітектура проєкту

### Основні компоненти:
- **Order Entity** - модель замовлення з JPA
- **OrderService** - бізнес-логіка для роботи з замовленнями
- **OrderController** - REST API контролер
- **PaymentService** - сервіс для обробки платежів
- **NotificationService** - сервіс для відправки сповіщень
- **SecurityConfig** - конфігурація безпеки Spring Security

### База даних:
- **H2** - для розробки та тестування
- **PostgreSQL** - для інтеграційного тестування

## Типи тестування

### 1. Unit Tests (Модульні тести)
- **OrderServiceTest** - тестування бізнес-логіки сервісу
- **PaymentServiceTest** - тестування логіки платежів
- **OrderControllerTest** - тестування REST контролера

### 2. Integration Tests (Інтеграційні тести)
- **OrderIntegrationTest** - повна інтеграція через MockMvc
- **OrderRepositoryIntegrationTest** - тестування JPA репозиторію
- **OrderTestContainersIntegrationTest** - тестування з реальною PostgreSQL

### 3. Security Tests (Тести безпеки)
- **SecurityTest** - тестування автентифікації та авторизації
- **AuthorizationTest** - тестування ролей та дозволів

### 4. Performance Tests (Тести продуктивності)
- **JMeter Test Plan** - навантажувальне тестування API
- **order-api-test.jmx** - конфігурація JMeter

## Технології

### Основні:
- **Java 17**
- **Spring Boot 3.1.5**
- **Spring Data JPA**
- **Spring Security**
- **Maven**

### Тестування:
- **JUnit 5**
- **Mockito**
- **Spring Test**
- **Testcontainers**
- **WireMock**
- **JMeter**

### База даних:
- **H2 Database**
- **PostgreSQL**
- **Docker Compose**

## Запуск проєкту

### 1. Компіляція
```bash
mvn clean compile
```

### 2. Запуск тестів
```bash
# Всі тести
mvn test

# Тільки unit тести
mvn test -Dtest="*Test"

# Тільки інтеграційні тести
mvn test -Dtest="*IntegrationTest"
```

### 3. Запуск додатку
```bash
mvn spring-boot:run
```

### 4. Запуск з Docker
```bash
# Запуск PostgreSQL
docker-compose up -d

# Запуск додатку з профілем test
mvn spring-boot:run -Dspring.profiles.active=test
```

## API Endpoints

### Без автентифікації:
- `GET /api/orders/generate-order-id` - генерація ID замовлення

### З автентифікацією (USER):
- `POST /api/orders` - створення замовлення
- `GET /api/orders/{id}` - отримання замовлення за ID
- `GET /api/orders/customer/{customerId}` - замовлення клієнта

### Тільки для ADMIN:
- `GET /api/orders` - всі замовлення
- `GET /api/orders/statistics` - статистика замовлень
- `PUT /api/orders/{id}/status` - оновлення статусу
- `DELETE /api/orders/{id}` - видалення замовлення

## Тестові користувачі

- **user/password** - роль USER
- **admin/admin** - роль ADMIN

## JMeter Performance Tests

### Запуск тестів продуктивності:
```bash
# Запуск JMeter тестів через Maven
mvn verify -Pjmeter

# Або вручну
cd src/test/jmeter
./run-performance-tests.sh
```

### Результати:
- Звіти зберігаються в `target/jmeter-reports/`
- Графіки та статистика продуктивності

## Структура проєкту

```
practice-10/
├── src/
│   ├── main/
│   │   ├── java/ua/edu/practice/
│   │   │   ├── config/          # Конфігурація
│   │   │   ├── controller/      # REST контролери
│   │   │   ├── dto/            # Data Transfer Objects
│   │   │   ├── exception/      # Винятки
│   │   │   ├── model/          # JPA сутності
│   │   │   ├── repository/     # JPA репозиторії
│   │   │   └── service/        # Бізнес-логіка
│   │   └── resources/
│   │       └── application.yml # Конфігурація додатку
│   └── test/
│       ├── java/ua/edu/practice/
│       │   ├── controller/     # Unit тести контролерів
│       │   ├── integration/    # Інтеграційні тести
│       │   ├── security/       # Тести безпеки
│       │   └── service/        # Unit тести сервісів
│       ├── jmeter/            # JMeter тести
│       └── resources/
│           └── application-test.yml # Тестова конфігурація
├── docker-compose.yml         # PostgreSQL для тестування
├── pom.xml                    # Maven конфігурація
└── README.md                  # Цей файл
```

## Особливості тестування

### 1. Mockito для Unit Tests
- Мокування залежностей
- Перевірка взаємодії між компонентами
- Тестування винятків

### 2. Spring Test для Integration Tests
- `@SpringBootTest` для повного контексту
- `@DataJpaTest` для тестування JPA
- `@WebMvcTest` для тестування контролерів

### 3. Testcontainers
- Реальна PostgreSQL в Docker
- Автоматичне створення/видалення контейнерів
- Тестування з реальною БД

### 4. Security Testing
- `@WithMockUser` для імітації користувачів
- Тестування ролей та дозволів
- Перевірка CSRF захисту

### 5. Performance Testing
- JMeter для навантажувального тестування
- Моніторинг продуктивності API
- Автоматизовані звіти

## Висновки

Проєкт демонструє комплексний підхід до тестування enterprise-додатку:

1. **Покриття всіх рівнів** - від unit до performance тестів
2. **Різні технології** - JUnit, Mockito, Testcontainers, JMeter
3. **Реальні сценарії** - автентифікація, база даних, API
4. **Автоматизація** - Maven, Docker, CI/CD готовність

Цей проєкт може служити основою для розробки та тестування реальних enterprise-додатків.