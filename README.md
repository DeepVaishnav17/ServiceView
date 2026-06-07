# Microservice Master

This project is a backend microservices architecture built using Spring Boot and Maven multi-module structure.

## Technologies Used

- Java 21
- Spring Boot
- Spring Data JPA
- Spring Data MongoDB
- MySQL
- MongoDB
- Maven
- Lombok

## Project Structure

```text
microservice-master
│
├── inventory-service
├── order-service
├── product-service
└── pom.xml
```

## Services

### Product Service

Handles product-related operations using MongoDB.

### Order Service

Handles order management using MySQL and JPA.

### Inventory Service

Handles inventory and stock management using MySQL and JPA.

## Features

- REST API based architecture
- Separate databases for services
- Maven multi-module setup
- MySQL and MongoDB integration
- Layered backend architecture

## Running the Project

### Clone Repository

```bash
git clone <repository-url>
```

### Build All Services

```bash
.\mvnw clean install
```

### Run Individual Services

#### Product Service

```bash
cd product-service
.\mvnw spring-boot:run
```

#### Order Service

```bash
cd order-service
.\mvnw spring-boot:run
```

#### Inventory Service

```bash
cd inventory-service
.\mvnw spring-boot:run
```

## Databases

### MySQL

Create databases:

```sql
CREATE DATABASE order_service;
CREATE DATABASE inventory_service;
```

### MongoDB

Start MongoDB locally on port `27017`.

## Screenshots

![Architecture](images/architecture.png)
