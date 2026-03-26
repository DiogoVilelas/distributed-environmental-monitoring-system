# Trabalho Prático 2 — Sistemas Distribuídos

**Aluno:** Diogo Vilelas  
**UC:** Sistemas Distribuídos  
**Ano Letivo:** 2025/2026  

# Distributed Environmental Monitoring System

## Overview

This project implements a **distributed system for ingestion, storage, and querying of environmental metrics** (temperature and humidity), using multiple communication protocols:

* REST
* gRPC
* MQTT

The system validates IoT devices, processes incoming data, stores it in a PostgreSQL database, and provides aggregated queries by room, department, floor, and building.

---

## Tech Stack

* Java 17
* Spring Boot
* PostgreSQL
* ActiveMQ (MQTT)
* gRPC (Protocol Buffers)
* Docker & Docker Compose
* Maven

---

## Features

* Multi-protocol data ingestion (MQTT, gRPC, REST)
* Device management via REST API
* Raw and aggregated metrics queries
* PostgreSQL data persistence
* Admin CLI for system management
* Simulated IoT sensors

---

## Project Structure

```
├── server/             # Spring Boot server
├── client-mqtt/        # MQTT simulator
├── client-grpc/        # gRPC simulator
├── client-rest/        # REST simulator
├── admin-cli/          # CLI admin client
├── proto/              # gRPC definitions
├── docker-compose.yml
├── relatorio_t02SD_62347.pdf
└── README.md
```

---

## Requirements

* Docker & Docker Compose
* Java 17+
* Maven

---

## Setup & Execution

### 1. Configure Environment Variables

Create a `.env` file in the root of the project:

```
POSTGRES_DB=your_database
POSTGRES_USER=your_user
POSTGRES_PASSWORD=your_password

PGADMIN_EMAIL=your_email@example.com
PGADMIN_PASSWORD=your_password
```

This file is required for Docker services to start correctly.

---

### 2. Start Infrastructure (Docker)

Run the following command in the root directory:

```
docker compose up -d
```

This will start:

* PostgreSQL
* pgAdmin
* MQTT Broker (ActiveMQ)

To verify:

```
docker ps
```

#### Common Issue

If containers already exist:

```
docker compose down -v
docker compose up -d
```

---

### 3. Run the Server

Navigate to the server module:

```
cd server
mvn spring-boot:run
```

Server endpoints:

* REST → http://localhost:8080
* gRPC → configured port (e.g. 50051)

---

## Device Management

Before sending metrics, devices must be registered and active.

Devices can be created:

* via Admin CLI
* via REST API
* directly in the database

---

## Sensor Simulators

Each simulator accepts:

```
<deviceId> <interval_ms>
```

### Examples

**MQTT**

```
mvn exec:java -Dexec.args="device-mqtt01 2000"
```

**gRPC**

```
mvn exec:java -Dexec.args="device-grpc01 2000"
```

**REST**

```
mvn exec:java -Dexec.args="device-rest01 2000"
```

If needed:

```
mvn clean compile exec:java
```

---

## Admin CLI

The Admin CLI allows:

* Device management
* Metrics queries
* Time range filtering
* Tabular visualization

Run:

```
mvn exec:java
```

---

## API Endpoints

### Metrics

* `POST /api/metrics/ingest`
* `GET /api/metrics/raw`
* `GET /api/metrics/average`

Additional:

* `GET /api/metrics/average/grouped`
* `GET /api/metrics/all`

---

## Performance Analysis

The system was tested under:

* low load
* high load

Performance analysis and protocol comparison (MQTT vs gRPC vs REST) are detailed in the project report.

---

## Report

The full report is available in:

```
relatorio_t02SD_62347.pdf
```

It includes:

* architectural decisions
* database design
* system configuration
* performance evaluation
* conclusions

---

## Notes

This project was developed as part of a Distributed Systems course and focuses on:

* multi-protocol communication
* data consistency and validation
* scalable system design
* IoT integration
