# Payment Service

## Features

- **Payment Creation:**  
  Create payments of different types with validation rules:
    - **TYPE1:** Must be in EUR and include payment details.
    - **TYPE2:** Must be in USD; details are optional.
    - **TYPE3:** Can be EUR or USD, but requires a creditor bank BIC.

- **Payment Cancellation:**  
  Cancel payments on the same day they were created. A cancellation fee is calculated based on the hour of creation.

- **Payment Querying:**  
  Retrieve IDs of non-canceled payments (optionally filtered by amount) and get cancellation info for a specific payment.

## Getting Started

### Prerequisites

- Java 17
- Gradle (the Gradle Wrapper is included)

### Running the Application

You can build and run the application with a single command using the Gradle Wrapper:

```bash
./gradlew bootRun
```

The application will start on port 8080 by default.

## API Documentation

### 1. User Creation
- **URL:** `/api/users/`
- **Method:** `POST`
- **Request Body:**
  ```json
  {
    "username": "john_doe"
  }
  ```

### 2. Create Payment

- **URL:** `/api/payments`
- **Method:** `POST`
- **Prerequisites:** User ID
- **Request Body:**
  ```json
  {
    "type": "TYPE1",
    "amount": 100.0,
    "currency": "EUR",
    "debtorIban": "DE89370400440532013000",
    "creditorIban": "DE75512108001245126199",
    "details": "Payment details",
    "creditorBankBic": "GENODEF1S04",
    "userId:": 1
  }
  ```
  
### 3. Cancel Payment by ID

- **URL:** `/api/payments/{id}/cancel`
- **Method:** `POST`
- **Example:**
  - `/api/payments/21/cancel`

### 4. Get Payments

- **URL:** `/api/payments`
- **Method:** `GET`
- **Query Parameters:**
  - `amount`: Filter payments by amount
- **Example:**
  - `/api/payments?amount=100.0`
  - `/api/payments`

### 5. Get Payments by User ID

- **URL:** `/api/users/{userId}/payments`
- **Method:** `GET`
- **Example:**
  - `/api/users/1/payments`


### 6. Get Payment Cancellation Info

- **URL:** `/api/payments/{id}`
- **Method:** `GET`
- **Example:**
  - `/api/payments/1`

## Testing

You can run the tests with the following command:

```bash
./gradlew test
```
