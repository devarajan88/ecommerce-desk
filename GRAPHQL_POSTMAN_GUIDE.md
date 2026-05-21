# GraphQL Endpoints — Postman Usage Guide

> Applies to: **orders-service**, **products-service**, **user-service**
> Each service exposes `POST /graphql` alongside its existing REST API.

---

## Service URLs

| Service | Base URL |
|---|---|
| orders-service | `http://localhost:8081` |
| products-service | `http://localhost:8082` |
| user-service | `http://localhost:8083` |

> Ports may differ based on your config-repo properties. Check `orders-service.properties`, `products-service.properties`, `user-service.properties` in your config-repo.

---

## GraphQL Endpoints

| Endpoint | Method | Description |
|---|---|---|
| `/graphql` | POST | Accepts all queries and mutations (requires auth) |
| `/graphiql` | GET | Browser-based playground with schema explorer (no auth to load) |

---

## Step 1 — Create a New Request in Postman

1. Click **New → HTTP Request**
2. Set method to **POST**
3. Enter URL: `http://localhost:<port>/graphql`

---

## Step 2 — Add Authentication Headers

All `/graphql` endpoints require the same header-based auth as the REST endpoints.
The API Gateway injects these headers after validating the JWT.
When hitting services **directly** (bypassing the gateway), add them manually.

Go to the **Headers** tab and add:

| Key | Value |
|---|---|
| `Content-Type` | `application/json` |
| `X-User-Name` | `testuser@example.com` |
| `X-User-Roles` | `ROLE_USER` |

---

## Step 3 — Set the Request Body

### Option A — Postman GraphQL Mode (Recommended)

1. Go to **Body** tab
2. Select **GraphQL** (not raw/JSON)
3. Paste your query in the **Query** box
4. Postman will auto-fetch the schema via introspection and provide autocomplete

### Option B — Raw JSON

1. Go to **Body** tab
2. Select **raw → JSON**
3. Use this format:

```json
{
  "query": "query { orders { orderId status } }"
}
```

With variables (cleaner for mutations):

```json
{
  "query": "mutation PlaceOrder($input: CreateOrderInput!) { placeOrder(input: $input) { orderId status } }",
  "variables": {
    "input": {
      "customerId": "550e8400-e29b-41d4-a716-446655440000",
      "productId":  "660e8400-e29b-41d4-a716-446655440001",
      "productQuantity": 2
    }
  }
}
```

---

## orders-service Queries & Mutations

**Base URL:** `http://localhost:8081/graphql`

### Query — Get All Orders

```graphql
query {
  orders {
    orderId
    customerId
    productId
    productQuantity
    status
  }
}
```

### Query — Get Order History

```graphql
query {
  orderHistory(orderId: "550e8400-e29b-41d4-a716-446655440000") {
    id
    orderId
    status
    createdAt
  }
}
```

### Mutation — Place an Order

```graphql
mutation {
  placeOrder(input: {
    customerId: "550e8400-e29b-41d4-a716-446655440000"
    productId: "660e8400-e29b-41d4-a716-446655440001"
    productQuantity: 2
  }) {
    orderId
    customerId
    productId
    productQuantity
    status
  }
}
```

### Mutation — Update Order Status

```graphql
mutation {
  updateOrderStatus(
    orderId: "550e8400-e29b-41d4-a716-446655440000"
    status: APPROVED
  ) {
    orderId
    status
  }
}
```

Valid `status` values: `CREATED` | `APPROVED` | `REJECTED`

### Mutation — Delete an Order

```graphql
mutation {
  deleteOrder(orderId: "550e8400-e29b-41d4-a716-446655440000")
}
```

---

## products-service Queries & Mutations

**Base URL:** `http://localhost:8082/graphql`

### Query — Get All Products

```graphql
query {
  products {
    id
    name
    price
    quantity
  }
}
```

### Query — Get Product by ID

```graphql
query {
  product(id: "770e8400-e29b-41d4-a716-446655440002") {
    id
    name
    price
    quantity
  }
}
```

### Mutation — Create a Product

```graphql
mutation {
  createProduct(input: {
    name: "Laptop"
    price: 999.99
    quantity: 50
  }) {
    id
    name
    price
    quantity
  }
}
```

### Mutation — Update a Product

```graphql
mutation {
  updateProduct(
    id: "770e8400-e29b-41d4-a716-446655440002"
    input: {
      name: "Gaming Laptop"
      price: 1299.99
      quantity: 30
    }
  ) {
    id
    name
    price
    quantity
  }
}
```

### Mutation — Delete a Product

```graphql
mutation {
  deleteProduct(id: "770e8400-e29b-41d4-a716-446655440002")
}
```

---

## user-service Queries & Mutations

**Base URL:** `http://localhost:8083/graphql`

### Query — Get All Users (Paginated)

```graphql
query {
  users(page: 0, size: 10) {
    content {
      id
      name
      age
      emailId
      department
      role
      status
      baseLocation
      address {
        street
        city
        zip
        country
      }
    }
    totalElements
    totalPages
  }
}
```

`page` and `size` are optional — defaults to `page: 0, size: 10`.

### Query — Get User by ID

```graphql
query {
  user(id: "1") {
    id
    name
    emailId
    role
    department
    address {
      street
      city
      country
    }
  }
}
```

### Mutation — Create a User

```graphql
mutation {
  createUser(input: {
    name: "John Doe"
    age: 30
    emailId: "john.doe@example.com"
    department: "Engineering"
    role: "SENIOR_SOFTWARE_ENGINEER"
    baseLocation: "New York"
    password: "Welcome@123"
  }) {
    id
    name
    emailId
    role
  }
}
```

Valid `role` values: `SENIOR_SOFTWARE_ENGINEER` | `ARCHITECT` | `JUNIOR_DEVELOPER` | `BUSINESS_ANALYST` | `SCRUM_MASTER` | `DB_ADMINISTRATOR` | `MANAGER`

### Mutation — Update a User

```graphql
mutation {
  updateUser(
    id: "1"
    input: {
      name: "John Smith"
      age: 31
      emailId: "john.smith@example.com"
      department: "Platform"
      role: "ARCHITECT"
      baseLocation: "San Francisco"
    }
  ) {
    id
    name
    emailId
    role
    department
  }
}
```

### Mutation — Delete a User

```graphql
mutation {
  deleteUser(id: "1")
}
```

---

## GraphQL Schema Reference

### orders-service Schema

```graphql
type Query {
    orders: [Order!]!
    orderHistory(orderId: ID!): [OrderHistory!]!
}

type Mutation {
    placeOrder(input: CreateOrderInput!): CreateOrderResponse!
    updateOrderStatus(orderId: ID!, status: OrderStatus!): Order!
    deleteOrder(orderId: ID!): Boolean!
}

type Order {
    orderId: ID
    customerId: ID
    productId: ID
    productQuantity: Int
    status: OrderStatus
}

type OrderHistory {
    id: ID
    orderId: ID
    status: OrderStatus
    createdAt: String
}

input CreateOrderInput {
    customerId: ID!
    productId: ID!
    productQuantity: Int!
}

enum OrderStatus {
    CREATED
    APPROVED
    REJECTED
}
```

### products-service Schema

```graphql
type Query {
    products: [Product!]!
    product(id: ID!): Product
}

type Mutation {
    createProduct(input: ProductInput!): Product!
    updateProduct(id: ID!, input: ProductInput!): Product!
    deleteProduct(id: ID!): Boolean!
}

type Product {
    id: ID
    name: String
    price: Float
    quantity: Int
}

input ProductInput {
    name: String!
    price: Float!
    quantity: Int!
}
```

### user-service Schema

```graphql
type Query {
    users(page: Int, size: Int): UserPage!
    user(id: ID!): User
}

type Mutation {
    createUser(input: UserInput!): User!
    updateUser(id: ID!, input: UserInput!): User!
    deleteUser(id: ID!): Boolean!
}

type User {
    id: ID
    name: String
    age: Int
    emailId: String
    department: String
    role: String
    status: String
    baseLocation: String
    address: Address
}

type Address {
    id: ID
    no: String
    street: String
    area: String
    city: String
    zip: String
    country: String
}

type UserPage {
    content: [User!]!
    totalElements: Int!
    totalPages: Int!
}

input UserInput {
    name: String!
    age: Int!
    emailId: String!
    department: String
    role: String
    baseLocation: String
    password: String
}
```

---

## Tips

### Use GraphiQL for Schema Exploration
Open in browser (no auth required to load the UI):
- Orders: `http://localhost:8081/graphiql`
- Products: `http://localhost:8082/graphiql`
- Users: `http://localhost:8083/graphiql`

Add auth headers in the **Request Headers** panel inside GraphiQL to execute queries.

### Partial Field Selection (GraphQL advantage over REST)
You can request only the fields you need — no over-fetching:

```graphql
query {
  orders {
    orderId
    status
  }
}
```

### Error Response Format
GraphQL always returns HTTP 200. Errors appear in the response body:

```json
{
  "errors": [
    {
      "message": "INTERNAL_ERROR for xxxxxxxx",
      "locations": [{ "line": 2, "column": 3 }],
      "path": ["placeOrder"],
      "extensions": { "classification": "INTERNAL_ERROR" }
    }
  ],
  "data": null
}
```
