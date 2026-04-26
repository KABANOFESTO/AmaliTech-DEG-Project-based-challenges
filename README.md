# AmaliTech DEG Backend Challenges

This repository contains my Spring Boot solutions for the AmaliTech DEG project-based backend challenges.

## Repository Structure

```text
backend/
  Idempotency-gateway/
  Pulse-Check/
  docs/
```

## Projects

### 1. Idempotency Gateway

A pay-once payment API that prevents duplicate charges by using an `Idempotency-Key` header to safely replay previous responses.

Location:

```text
backend/Idempotency-gateway
```

Project documentation:

- [Idempotency Gateway README](./backend/Idempotency-gateway/README.md)

### 2. Pulse-Check API

A watchdog monitoring API that tracks device heartbeats, pauses monitoring during maintenance, and marks devices as down when their timers expire.

Location:

```text
backend/Pulse-Check
```

Project documentation:

- [Pulse-Check README](./backend/Pulse-Check/README.md)

## Tech Stack

- Java 17
- Spring Boot
- REST APIs
- Maven
- Docker

## Local Start Guide

Each project runs independently.

### Idempotency Gateway

```bash
cd backend/Idempotency-gateway
mvn spring-boot:run
```

API base:

```text
http://localhost:8080
```

### Pulse-Check

```bash
cd backend/Pulse-Check
mvn spring-boot:run
```

API base:

```text
http://localhost:8080/api/v1
```

If Maven wrapper works in your environment, you can also use `./mvnw spring-boot:run` inside either project.

## Docker Start Guide

Prerequisite: make sure Docker Desktop or Docker Engine is running.

### Idempotency Gateway with Docker Compose

```bash
cd backend/Idempotency-gateway
docker compose up --build
```

### Pulse-Check with Docker Compose

```bash
cd backend/Pulse-Check
docker compose up --build
```

### Manual Docker Build

Idempotency Gateway:

```bash
cd backend/Idempotency-gateway
docker build -t idempotency-gateway .
docker run -p 8080:8080 idempotency-gateway
```

Pulse-Check:

```bash
cd backend/Pulse-Check
docker build -t pulse-check .
docker run -p 8080:8080 pulse-check
```

### Stop Containers

Run this from the active project directory:

```bash
docker compose down
```

## Notes

- Run one project at a time if both are mapped to port `8080`
- Each project has its own architecture diagram, API documentation, and setup guide inside its dedicated README
