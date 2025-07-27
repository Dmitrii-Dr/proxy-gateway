# Functional Specification: Proxy Gateway Service

## 1. Overview

This document specifies the requirements for a **Proxy Gateway** microservice. The service acts as a central entry point and dynamic reverse proxy. It allows other microservices to register themselves and then transparently forwards incoming requests to the correct registered service based on the request path.

## 2. Technology Stack

- **Framework**: Quarkus
- **Language**: Java
- **Database**: PostgreSQL
- **Build Tool**: Maven

## 3. Data Models


### 3.1. Service Registration Data Model

The system must persist service registration information. The data model must include the following attributes:

- **`id`**: A unique numerical identifier for the record (Primary Key).
- **`externalId`**: A unique string identifier for the registered service (e.g., the service name). This will be used for lookups.
- **`application`**: The name of the application/service.
- **`host`**: The hostname of the registered service.
- **`url`**: The full base URL where the service can be reached.
- **`createdAt`**: A timestamp indicating when the registration was created or last updated.

### 3.2. PostgreSQL Table: `registry_url`

The following table is used to store service registration data in PostgreSQL:

| Column Name   | Type         | Constraints                | Description                                 |
|--------------|--------------|----------------------------|---------------------------------------------|
| id           | BIGSERIAL    | PRIMARY KEY                | Unique record identifier                    |
| external_id  | VARCHAR      | NOT NULL, UNIQUE           | Unique string identifier for the service    |
| application  | VARCHAR      | NOT NULL                   | Name of the application/service             |
| host         | VARCHAR      | NOT NULL                   | Hostname of the registered service          |
| url          | VARCHAR      | NOT NULL                   | Full base URL of the service                |
| created_at   | TIMESTAMP    | NOT NULL                   | Timestamp of creation or last update        |

Table name: `registry_url`

## 4. Business Logic Layer

### 4.1. Service Registration

- The system must provide a mechanism to register or update a service's location.
- This operation must be idempotent. If a service with the same unique identifier (`externalId`) is registered again, its details (URL, host, timestamp) should be updated.
- The logic should extract the necessary information from the registration request and persist it using the data model defined in section 3.1.

### 4.2. URL Resolution for Proxying

- The system must be able to look up a service's registered URL using its unique name (`service`).
- If a registration is found, the system must construct a full, reachable target URL by combining the registered base URL with the requested endpoint path.
- If no registration is found for the given service name, the system should indicate that the service is unknown.

## 5. API Endpoints

The service must expose the following RESTful API endpoints.

### 5.1. Register Service Endpoint

- **Endpoint**: `POST /registry`
- **Purpose**: To register or update a microservice with the gateway.
- **Request Body**: A JSON object containing the service's details.
  ```json
  {
    "application": "string",
    "url": "string"
  }
  ```
- **Success Response**:
  - **Status Code**: `200 OK`
  - **Body**: A plain text confirmation message (e.g., "Registered: {application}:{url}").
- **Error Response**: The endpoint should handle invalid input gracefully.

### 5.2. Proxy Endpoint

- **Endpoint**: `GET /{service}/{endpoint}`
- **Purpose**: To receive a request and proxy it to the appropriate registered service.
- **Path Parameters**:
  - `service`: The unique name of the target service (e.g., "customer-service").
  - `endpoint`: The downstream path and query parameters to be called on the target service (e.g., "users/123").
- **Behavior**:
  1.  Resolve the base URL for the specified `{service}` using the logic from section 4.2.
  2.  Construct the full target URL.
  3.  **Default Port Handling**: If the registered URL for the service does not explicitly contain a port, the request must be forwarded to port `8080` by default.
  4.  Perform a request to the full target URL using the same HTTP method as the original request (GET, POST, PUT, DELETE, PATCH, etc.).
  5.  The response body from the target service must be returned directly to the original client.
- **Success Response**:
  - **Status Code**: The status code received from the downstream service.
  - **Body**: The JSON response body received from the downstream service.
- **Error Responses**:
  - **Status Code**: `404 Not Found` if the `{service}` is not registered.
  - **Status Code**: `500 Internal Server Error` if an error occurs during the proxying process (e.g., the downstream service is unreachable).

## 6. Configuration

The application must be configurable via `application.properties`. The following keys must be supported:

- **`quarkus.datasource.db-kind`**: The type of database (e.g., `postgresql`).
- **`quarkus.datasource.username`**: The database user.
- **`quarkus.datasource.password`**: The database password.
- **`quarkus.datasource.jdbc.url`**: The JDBC connection string for the database.
- **`quarkus.hibernate-orm.database.generation`**: The schema generation strategy (e.g., `update`).
