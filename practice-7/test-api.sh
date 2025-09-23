#!/bin/bash

echo "=== Testing Shop API ==="
echo "Base URL: http://localhost:8080/api/shop"
echo

echo "1. Getting all categories:"
curl -s http://localhost:8080/api/shop/categories | jq '.[0:3]' 2>/dev/null || curl -s http://localhost:8080/api/shop/categories | head -c 200
echo -e "\n"

echo "2. Getting all products:"
curl -s http://localhost:8080/api/shop/products | jq '.content[0:2]' 2>/dev/null || curl -s http://localhost:8080/api/shop/products | head -c 200
echo -e "\n"

echo "3. Getting all customers:"
curl -s http://localhost:8080/api/shop/customers | jq '.content[0:2]' 2>/dev/null || curl -s http://localhost:8080/api/shop/customers | head -c 200
echo -e "\n"

echo "4. Searching products by price range (100-1000):"
curl -s "http://localhost:8080/api/shop/products/search?minPrice=100&maxPrice=1000" | jq '.' 2>/dev/null || curl -s "http://localhost:8080/api/shop/products/search?minPrice=100&maxPrice=1000"
echo -e "\n"

echo "5. Getting order statistics:"
curl -s http://localhost:8080/api/shop/analytics/order-statistics | jq '.' 2>/dev/null || curl -s http://localhost:8080/api/shop/analytics/order-statistics
echo -e "\n"

echo "6. Creating a new order:"
ORDER_RESPONSE=$(curl -s -X POST http://localhost:8080/api/shop/orders \
  -H "Content-Type: application/json" \
  -d '{"customerId": 2, "productIds": [3, 4]}')
echo $ORDER_RESPONSE | jq '.' 2>/dev/null || echo $ORDER_RESPONSE
echo -e "\n"

echo "7. Getting all orders:"
curl -s http://localhost:8080/api/shop/orders | jq '.content' 2>/dev/null || curl -s http://localhost:8080/api/shop/orders | head -c 300
echo -e "\n"

echo "=== API Testing Complete ==="
