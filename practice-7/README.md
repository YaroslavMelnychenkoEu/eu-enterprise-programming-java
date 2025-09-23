# Enterprise Java Programming - Practice 7

## Implementation of Database Interaction using Spring Data JPA and Hibernate

This project demonstrates the implementation of database interaction in Enterprise Java development using Spring Data JPA and Hibernate for an online shop management system.

## Project Structure

```
src/main/java/com/example/shop/
├── ShopApplication.java                 # Main Spring Boot application
├── config/
│   ├── DatabaseConfig.java             # Database configuration
│   └── AuditConfig.java                # JPA auditing configuration
├── domain/
│   ├── enums/
│   │   └── OrderStatus.java            # Order status enumeration
│   ├── Category.java                   # Category entity
│   ├── Product.java                    # Product entity
│   ├── Customer.java                   # Customer entity
│   └── Order.java                      # Order entity
├── repository/
│   ├── CategoryRepository.java         # Category repository
│   ├── ProductRepository.java          # Product repository
│   ├── CustomerRepository.java         # Customer repository
│   └── OrderRepository.java            # Order repository
├── service/
│   ├── CategoryService.java            # Category business logic
│   ├── ProductService.java             # Product business logic
│   ├── CustomerService.java            # Customer business logic
│   ├── OrderService.java               # Order business logic
│   ├── TransactionService.java         # Transactional operations
│   └── ComplexQueryService.java        # Complex queries implementation
├── dto/
│   ├── CategoryDTO.java                # Category data transfer object
│   ├── ProductDTO.java                 # Product data transfer object
│   ├── CustomerDTO.java                # Customer data transfer object
│   └── OrderDTO.java                   # Order data transfer object
├── exception/
│   ├── ResourceNotFoundException.java  # Resource not found exception
│   └── BusinessException.java          # Business logic exception
└── controller/
    └── ShopController.java             # REST API controller
```

## Features Implemented

### Part 1: Project Setup and Database Connection
- ✅ Spring Boot project configuration
- ✅ PostgreSQL database connection
- ✅ JPA/Hibernate configuration
- ✅ Database configuration class
- ✅ Basic error handling

### Part 2: Domain Model Development
- ✅ **Product** entity (id, name, description, price, category, createdAt, updatedAt)
- ✅ **Category** entity (id, name, description, parentCategory)
- ✅ **Order** entity (id, customer, products, totalAmount, status, createdAt)
- ✅ **Customer** entity (id, firstName, lastName, email, address, phone)
- ✅ Proper relationships (One-to-Many, Many-to-Many)
- ✅ Cascade operations configuration
- ✅ JPA auditing for timestamps

### Part 3: Repository and Service Layer Implementation
- ✅ Spring Data JPA repositories for all entities
- ✅ CRUD operations for all entities
- ✅ Product search by category and price range
- ✅ Customer order history retrieval
- ✅ Sales statistics by categories
- ✅ Pagination and sorting implementation
- ✅ DTO pattern for data transfer

### Part 4: Transaction Management
- ✅ Order creation with product availability check
- ✅ Order status update with inventory changes
- ✅ Mass price update for product categories
- ✅ Order cancellation with inventory return
- ✅ Proper transaction boundaries and rollback handling

### Part 5: Complex Queries Implementation
- ✅ **JPQL queries** for complex data retrieval
- ✅ **Criteria API** for dynamic filtering
- ✅ **Native SQL queries** for advanced analytics
- ✅ **Specifications** for dynamic filtering (framework ready)
- ✅ Advanced aggregation and reporting queries

## Database Schema

The project includes a comprehensive database schema with:
- Categories table with hierarchical structure
- Products table with category relationships
- Customers table with contact information
- Orders table with status tracking
- Order-Products junction table for many-to-many relationship
- Proper indexes for performance optimization
- Sample data for testing

## API Endpoints

### Categories
- `POST /api/shop/categories` - Create category
- `GET /api/shop/categories` - Get all categories
- `GET /api/shop/categories/{id}` - Get category by ID

### Products
- `POST /api/shop/products` - Create product
- `GET /api/shop/products` - Get all products (paginated)
- `GET /api/shop/products/search` - Search products with filters
- `GET /api/shop/products/category/{categoryId}` - Get products by category

### Customers
- `POST /api/shop/customers` - Create customer
- `GET /api/shop/customers` - Get all customers (paginated)
- `GET /api/shop/customers/{id}` - Get customer by ID

### Orders
- `POST /api/shop/orders` - Create order
- `GET /api/shop/orders` - Get all orders (paginated)
- `GET /api/shop/orders/customer/{customerId}` - Get orders by customer
- `PUT /api/shop/orders/{id}/status` - Update order status

### Transactional Operations
- `POST /api/shop/orders/with-availability-check` - Create order with availability check
- `PUT /api/shop/orders/{id}/status-with-inventory` - Update status with inventory changes
- `PUT /api/shop/orders/{id}/cancel` - Cancel order with inventory return
- `PUT /api/shop/products/category/{categoryId}/mass-update-prices` - Mass price update

### Analytics and Complex Queries
- `GET /api/shop/analytics/top-customers` - Top customers by spending
- `GET /api/shop/analytics/top-products` - Top selling products
- `GET /api/shop/analytics/monthly-sales` - Monthly sales statistics
- `GET /api/shop/analytics/sales-by-category` - Sales by category
- `GET /api/shop/analytics/order-statistics` - Order aggregation statistics
- `GET /api/shop/analytics/customers-with-orders` - Customers with orders in period

## Running the Application

### Prerequisites
- Java 17+
- Maven 3.6+
- Docker and Docker Compose
- PostgreSQL (via Docker)

### Setup Instructions

1. **Start PostgreSQL database:**
   ```bash
   docker-compose up -d
   ```

2. **Build and run the application:**
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

3. **Access the application:**
   - Application: http://localhost:8080
   - Database: localhost:5432 (postgres/postgres)

### Testing the API

You can test the API endpoints using tools like Postman or curl. Here are some example requests:

**Create a category:**
```bash
curl -X POST http://localhost:8080/api/shop/categories \
  -H "Content-Type: application/json" \
  -d '{"name": "Electronics", "description": "Electronic devices"}'
```

**Create a product:**
```bash
curl -X POST http://localhost:8080/api/shop/products \
  -H "Content-Type: application/json" \
  -d '{"name": "Laptop", "description": "Gaming laptop", "price": 999.99, "categoryId": 1}'
```

**Search products:**
```bash
curl "http://localhost:8080/api/shop/products/search?minPrice=100&maxPrice=1000"
```

## Key Technologies Used

- **Spring Boot 3.2.0** - Application framework
- **Spring Data JPA** - Data access layer
- **Hibernate** - ORM implementation
- **PostgreSQL** - Database
- **Maven** - Build tool
- **Docker** - Containerization

## Learning Outcomes

This project demonstrates:
- Enterprise Java development patterns
- Spring Data JPA and Hibernate best practices
- Database design and relationships
- Transaction management
- Complex query implementation
- REST API development
- Error handling and validation
- Pagination and sorting
- Analytics and reporting

## Database Connection Details

- **Host:** localhost
- **Port:** 5432
- **Database:** postgres
- **Username:** postgres
- **Password:** postgres

The application automatically creates the database schema and populates it with sample data on startup.
