# StockPulse API Documentation

## Product Management APIs

### 1. Create a new product
**POST** `/api/products`

Creates a new product in the system.

#### Request Body
```json
{
  "sku": "TECH-001",
  "name": "Wireless Bluetooth Headphones",
  "category": "ELECTRONICS",
  "currentPrice": 99.99,
  "stockLevel": 50,
  "reorderThreshold": 10,
  "demandVelocity": 2.5
}
```

#### Response
```json
{
  "id": 1,
  "sku": "TECH-001",
  "name": "Wireless Bluetooth Headphones",
  "category": "ELECTRONICS",
  "currentPrice": 99.99,
  "stockLevel": 50,
  "reorderThreshold": 10,
  "demandVelocity": 2.5,
  "status": "ACTIVE"
}
```

---

### 2. Get all products
**GET** `/api/products`

Returns a list of all products.

#### Response
```json
[
  {
    "id": 1,
    "sku": "TECH-001",
    "name": "Wireless Bluetooth Headphones",
    "category": "ELECTRONICS",
    "currentPrice": 99.99,
    "stockLevel": 50,
    "reorderThreshold": 10,
    "demandVelocity": 2.5,
    "status": "ACTIVE"
  },
  {
    "id": 2,
    "sku": "CLOTH-002",
    "name": "Premium Cotton T-Shirt",
    "category": "APPAREL",
    "currentPrice": 29.99,
    "stockLevel": 100,
    "reorderThreshold": 20,
    "demandVelocity": 5.0,
    "status": "ACTIVE"
  }
]
```

---

### 3. Get products by status
**GET** `/api/products?status=ACTIVE`

Returns products filtered by status.

#### Response
```json
[
  {
    "id": 1,
    "sku": "TECH-001",
    "name": "Wireless Bluetooth Headphones",
    "category": "ELECTRONICS",
    "currentPrice": 99.99,
    "stockLevel": 50,
    "reorderThreshold": 10,
    "demandVelocity": 2.5,
    "status": "ACTIVE"
  }
]
```

---

### 4. Get products by category
**GET** `/api/products?category=ELECTRONICS`

Returns products filtered by category.

#### Response
```json
[
  {
    "id": 1,
    "sku": "TECH-001",
    "name": "Wireless Bluetooth Headphones",
    "category": "ELECTRONICS",
    "currentPrice": 99.99,
    "stockLevel": 50,
    "reorderThreshold": 10,
    "demandVelocity": 2.5,
    "status": "ACTIVE"
  }
]
```

---

### 5. Get products by status and category
**GET** `/api/products?status=ACTIVE&category=ELECTRONICS`

Returns products filtered by both status and category.

#### Response
```json
[
  {
    "id": 1,
    "sku": "TECH-001",
    "name": "Wireless Bluetooth Headphones",
    "category": "ELECTRONICS",
    "currentPrice": 99.99,
    "stockLevel": 50,
    "reorderThreshold": 10,
    "demandVelocity": 2.5,
    "status": "ACTIVE"
  }
]
```

---

### 6. Update product stock level
**PATCH** `/api/products/{id}/stock`

Updates the stock level for a specific product.

#### Request Body
```json
{
  "stockLevel": 25
}
```

#### Response
```json
{
  "id": 1,
  "sku": "TECH-001",
  "name": "Wireless Bluetooth Headphones",
  "category": "ELECTRONICS",
  "currentPrice": 99.99,
  "stockLevel": 25,
  "reorderThreshold": 10,
  "demandVelocity": 2.5,
  "status": "ACTIVE"
}
```

---

### 7. Process an order
**POST** `/api/products/{id}/orders`

Simulates an order by reducing stock and increasing demand velocity.

#### Request Body
```json
{
  "quantity": 5
}
```

#### Successful Response (200 OK)
```json
{
  "id": 1,
  "sku": "TECH-001",
  "name": "Wireless Bluetooth Headphones",
  "category": "ELECTRONICS",
  "currentPrice": 99.99,
  "stockLevel": 45,
  "reorderThreshold": 10,
  "demandVelocity": 3.0,
  "status": "ACTIVE"
}
```

#### Insufficient Stock Response (400 Bad Request)
Returned when trying to order more items than available in stock.