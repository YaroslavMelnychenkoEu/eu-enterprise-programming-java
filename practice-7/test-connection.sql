-- Test connection to PostgreSQL database
-- Run this with: psql -h localhost -p 5432 -U postgres -d postgres

-- Check if we can connect
SELECT 'Connection successful!' as status;

-- Check if tables exist
SELECT table_name 
FROM information_schema.tables 
WHERE table_schema = 'public' 
ORDER BY table_name;

-- Check sample data
SELECT 'Categories:' as info;
SELECT id, name, description FROM categories LIMIT 5;

SELECT 'Products:' as info;
SELECT id, name, price, category_id FROM products LIMIT 5;

SELECT 'Customers:' as info;
SELECT id, first_name, last_name, email FROM customers LIMIT 5;
