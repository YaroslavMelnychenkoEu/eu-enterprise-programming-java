-- Create categories table
CREATE TABLE IF NOT EXISTS categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    parent_category_id BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (parent_category_id) REFERENCES categories(id)
);

-- Create customers table
CREATE TABLE IF NOT EXISTS customers (
    id BIGSERIAL PRIMARY KEY,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    address TEXT,
    phone VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create products table
CREATE TABLE IF NOT EXISTS products (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(10,2) NOT NULL CHECK (price > 0),
    category_id BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES categories(id)
);

-- Create orders table
CREATE TABLE IF NOT EXISTS orders (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL CHECK (total_amount > 0),
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customers(id)
);

-- Create order_products junction table
CREATE TABLE IF NOT EXISTS order_products (
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    PRIMARY KEY (order_id, product_id),
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_categories_parent_id ON categories(parent_category_id);
CREATE INDEX IF NOT EXISTS idx_products_category_id ON products(category_id);
CREATE INDEX IF NOT EXISTS idx_orders_customer_id ON orders(customer_id);
CREATE INDEX IF NOT EXISTS idx_orders_status ON orders(status);
CREATE INDEX IF NOT EXISTS idx_orders_created_at ON orders(created_at);
CREATE INDEX IF NOT EXISTS idx_customers_email ON customers(email);

-- Insert sample data
INSERT INTO categories (name, description) VALUES 
('Electronics', 'Electronic devices and gadgets'),
('Clothing', 'Fashion and apparel'),
('Books', 'Books and literature'),
('Home & Garden', 'Home improvement and garden supplies')
ON CONFLICT (name) DO NOTHING;

INSERT INTO categories (name, description, parent_category_id) VALUES 
('Smartphones', 'Mobile phones and accessories', 1),
('Laptops', 'Portable computers', 1),
('Men''s Clothing', 'Clothing for men', 2),
('Women''s Clothing', 'Clothing for women', 2)
ON CONFLICT (name) DO NOTHING;

INSERT INTO customers (first_name, last_name, email, address, phone) VALUES 
('John', 'Doe', 'john.doe@example.com', '123 Main St, City, Country', '+1234567890'),
('Jane', 'Smith', 'jane.smith@example.com', '456 Oak Ave, City, Country', '+0987654321'),
('Bob', 'Johnson', 'bob.johnson@example.com', '789 Pine Rd, City, Country', '+1122334455')
ON CONFLICT (email) DO NOTHING;

INSERT INTO products (name, description, price, category_id) VALUES 
('iPhone 15', 'Latest Apple smartphone', 999.99, 5),
('MacBook Pro', 'Professional laptop', 1999.99, 6),
('T-Shirt', 'Cotton t-shirt', 29.99, 7),
('Dress', 'Summer dress', 79.99, 8),
('Programming Book', 'Learn Java programming', 49.99, 3)
ON CONFLICT DO NOTHING;
