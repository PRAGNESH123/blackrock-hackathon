# BlackRock Hackathon — Auto-Saving Retirement System

> **Challenge:** Self-saving for your retirement  
> **Team:** Individual  
> **Stack:** Java 21 · Spring Boot 3 · Docker · Alpine Linux

---

## Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Prerequisites](#prerequisites)
- [Project Structure](#project-structure)
- [Configuration](#configuration)
- [How to Run](#how-to-run)
- [API Endpoints](#api-endpoints)
- [Business Logic](#business-logic)
- [Testing](#testing)
- [Docker Details](#docker-details)
- [Edge Cases Handled](#edge-cases-handled)
- [Design Decisions & Tradeoffs](#design-decisions--tradeoffs)

---

## Overview

This system implements an **automated micro-savings engine** for retirement planning. It rounds up daily expenses to the next multiple of ₹100 and invests the spare change. The API supports:

- Parsing raw expenses into enriched transactions
- Validating transactions against business rules
- Applying temporal period constraints (fixed overrides, extra additions, grouping windows)
- Calculating inflation-adjusted returns via NPS or Index Fund (NIFTY 50)
- Reporting live system performance metrics

---

## Architecture

```
┌─────────────────────────────────────────────────────┐
│                   REST API Layer                     │
│  TransactionController  │  ReturnsController        │
│  PerformanceController                              │
└─────────────────┬───────────────────────────────────┘
                  │
┌─────────────────▼───────────────────────────────────┐
│                  Service Layer                       │
│  ParserService  │  ValidatorService                 │
│  FilterService  │  ReturnsService                   │
│  PerformanceService                                 │
└─────────────────┬───────────────────────────────────┘
                  │
┌─────────────────▼───────────────────────────────────┐
│               Utility Layer                          │
│  TaxCalculator  │  FlexibleDateDeserializer         │
│  GlobalExceptionHandler                             │
└─────────────────────────────────────────────────────┘
```

All services are **stateless** — no database required. Every request is fully self-contained.

---

## Prerequisites

| Tool | Version | Required For |
|------|---------|-------------|
| Java | 17 (LTS) | Local development |
| Maven | 3.9+    | Building the project |
| Docker | 24+     | Running via container |
| Docker Compose | v2+     | Multi-service orchestration |

---

## Project Structure

```
blackrock-challenge/
├── src/
│   ├── main/
│   │   ├── java/com/blackrock/challenge/
│   │   │   ├── controller/
│   │   │   │   ├── TransactionController.java
│   │   │   │   ├── ReturnsController.java
│   │   │   │   └── PerformanceController.java
│   │   │   ├── service/
│   │   │   │   ├── TransactionParserService.java
│   │   │   │   ├── TransactionValidatorService.java
│   │   │   │   ├── TransactionFilterService.java
│   │   │   │   ├── ReturnsCalculationService.java
│   │   │   │   ├── PerformanceService.java
│   │   │   │   └── impl/
│   │   │   │       ├── TransactionParserServiceImpl.java
│   │   │   │       ├── TransactionValidatorServiceImpl.java
│   │   │   │       ├── TransactionFilterServiceImpl.java
│   │   │   │       ├── ReturnsCalculationServiceImpl.java
│   │   │   │       └── PerformanceServiceImpl.java
│   │   │   ├── model/
│   │   │   │   ├── domain/
│   │   │   │   │   ├── Transaction.java
│   │   │   │   │   ├── InvalidTransaction.java
│   │   │   │   │   ├── QPeriod.java
│   │   │   │   │   ├── PPeriod.java
│   │   │   │   │   ├── KPeriod.java
│   │   │   │   │   └── KPeriodResult.java
│   │   │   │   ├── request/
│   │   │   │   │   ├── ExpenseRequest.java
│   │   │   │   │   ├── ValidatorRequest.java
│   │   │   │   │   ├── FilterRequest.java
│   │   │   │   │   └── ReturnsRequest.java
│   │   │   │   └── response/
│   │   │   │       ├── TransactionResponse.java
│   │   │   │       ├── ValidatorResponse.java
│   │   │   │       ├── FilterResponse.java
│   │   │   │       ├── ReturnsResponse.java
│   │   │   │       └── PerformanceResponse.java
│   │   │   ├── config/
│   │   │   │   ├── JacksonConfig.java
│   │   │   │   └── WebConfig.java
│   │   │   ├── interceptor/
│   │   │   │   └── PerformanceInterceptor.java
│   │   │   ├── exception/
│   │   │   │   └── GlobalExceptionHandler.java
│   │   │   └── util/
│   │   │       ├── TaxCalculator.java
│   │   │       └── FlexibleLocalDateTimeDeserializer.java
│   │   └── resources/
│   │       ├── application.properties
│   │       └── application-prod.properties
├── test/
│   └── (see Testing section)
├── Dockerfile
├── compose.yaml
└── README.md
```

---

## Configuration

### `application.properties`

```properties
server.port=5477
spring.jackson.date-format=yyyy-MM-dd HH:mm:ss
spring.jackson.serialization.write-dates-as-timestamps=false
spring.jackson.deserialization.fail-on-unknown-properties=false
```

### `application-prod.properties` (used inside Docker)

```properties
server.port=5477
spring.profiles.active=prod
spring.jackson.date-format=yyyy-MM-dd HH:mm:ss
spring.jackson.serialization.write-dates-as-timestamps=false
logging.level.root=WARN
logging.level.com.blackrock=INFO
```


---

## How to Run


### Option 2 — Docker Only

```bash
# Build image
docker build -t blk-hacking-ind-{name-lastname} .

# Run container
docker run -d \
  -p 5477:5477 \
  --name blk-hacking-ind-{name-lastname} \
  blk-hacking-ind-{name-lastname}

# Check logs
docker logs -f blk-hacking-ind-{name-lastname}
```

### Option 3 — Local Development

```bash
# Build
mvn clean package -DskipTests

# Run
java -jar target/*.jar

# Or with Maven directly
mvn spring-boot:run
```

> Application starts on **port 5477** in all cases.

---

## API Endpoints

### Base URL
```
http://localhost:5477/blackrock/challenge/v1
```

---

### 1. Transaction Parser
**`POST /transactions:parse`**

Converts raw expenses into enriched transactions with `ceiling` and `remanent`.

**Formula:**
```
ceiling  = next multiple of 100 above amount
remanent = ceiling - amount
```

**Request:**
```json
[
  {"date": "2023-10-12 20:15:30", "amount": 250},
  {"date": "2023-02-28 15:49:20", "amount": 375},
  {"date": "2023-07-01 21:59:00", "amount": 620},
  {"date": "2023-12-17 08:09:45", "amount": 480}
]
```

**Response:**
```json
{
  "transactions": [
    {"date": "2023-10-12 20:15:30", "amount": 250.0, "ceiling": 300.0, "remanent": 50.0},
    {"date": "2023-02-28 15:49:20", "amount": 375.0, "ceiling": 400.0, "remanent": 25.0},
    {"date": "2023-07-01 21:59:00", "amount": 620.0, "ceiling": 700.0, "remanent": 80.0},
    {"date": "2023-12-17 08:09:45", "amount": 480.0, "ceiling": 500.0, "remanent": 20.0}
  ],
  "totalAmount": 1725.0,
  "totalCeiling": 1900.0,
  "totalRemanent": 175.0
}
```

**cURL:**
```bash
curl -X POST http://localhost:5477/blackrock/challenge/v1/transactions:parse \
  -H "Content-Type: application/json" \
  -d '[
    {"date": "2023-10-12 20:15:30", "amount": 250},
    {"date": "2023-02-28 15:49:20", "amount": 375},
    {"date": "2023-07-01 21:59:00", "amount": 620},
    {"date": "2023-12-17 08:09:45", "amount": 480}
  ]'
```

---

### 2. Transaction Validator
**`POST /transactions:validator`**

Validates transactions. Returns `valid` and `invalid` lists with rejection reasons.

**Validation Rules (in order):**
1. Negative amount → rejected
2. Amount ≥ 500,000 → rejected
3. Duplicate timestamp → rejected
4. Ceiling/remanent inconsistency → rejected

**Request:**
```json
{
  "wage": 50000,
  "transactions": [
    {"date": "2023-01-15 10:30:00", "amount": 2000.0, "ceiling": 2000.0, "remanent": 0.0},
    {"date": "2023-07-10 09:15:00", "amount": -250.0, "ceiling": 0.0,   "remanent": 0.0},
    {"date": "2023-01-15 10:30:00", "amount": 2000.0, "ceiling": 2000.0, "remanent": 0.0}
  ]
}
```

**Response:**
```json
{
  "valid": [
    {"date": "2023-01-15 10:30:00", "amount": 2000.0, "ceiling": 2000.0, "remanent": 0.0}
  ],
  "invalid": [
    {"date": "2023-07-10 09:15:00", "amount": -250.0, "ceiling": 0.0, "remanent": 0.0,
     "message": "Negative amounts are not allowed"},
    {"date": "2023-01-15 10:30:00", "amount": 2000.0, "ceiling": 2000.0, "remanent": 0.0,
     "message": "Duplicate transaction"}
  ]
}
```

---

### 3. Temporal Constraints Filter
**`POST /transactions:filter`**

Applies q → p → k period rules to adjust remanents.

**Processing Order:**
```
Step 1: Base remanent (from ceiling)
Step 2: Q period — REPLACE remanent with fixed amount
Step 3: P period — ADD extra to remanent (all matching P periods stack)
Step 4: K period — flag which transactions fall in evaluation windows
```

**Request:**
```json
{
  "q": [{"fixed": 0, "start": "2023-07-01 00:00:00", "end": "2023-07-31 23:59:59"}],
  "p": [{"extra": 25, "start": "2023-10-01 08:00:00", "end": "2023-12-31 19:59:59"}],
  "k": [
    {"start": "2023-01-01 00:00:00", "end": "2023-12-31 23:59:59"},
    {"start": "2023-03-01 00:00:00", "end": "2023-11-30 23:59:59"}
  ],
  "transactions": [
    {"date": "2023-02-28 15:49:20", "amount": 375.0, "ceiling": 400.0, "remanent": 25.0},
    {"date": "2023-07-01 21:59:00", "amount": 620.0, "ceiling": 700.0, "remanent": 80.0},
    {"date": "2023-10-12 20:15:30", "amount": 250.0, "ceiling": 300.0, "remanent": 50.0},
    {"date": "2023-12-17 08:09:45", "amount": 480.0, "ceiling": 500.0, "remanent": 20.0}
  ]
}
```

**Response:**
```json
{
  "valid": [
    {"date": "2023-02-28 15:49:20", "amount": 375.0, "ceiling": 400.0, "remanent": 25.0, "inKPeriod": true},
    {"date": "2023-07-01 21:59:00", "amount": 620.0, "ceiling": 700.0, "remanent": 0.0,  "inKPeriod": true},
    {"date": "2023-10-12 20:15:30", "amount": 250.0, "ceiling": 300.0, "remanent": 75.0, "inKPeriod": true},
    {"date": "2023-12-17 08:09:45", "amount": 480.0, "ceiling": 500.0, "remanent": 45.0, "inKPeriod": true}
  ],
  "invalid": []
}
```

---

### 4a. NPS Returns
**`POST /returns:nps`**

Calculates compound interest returns via NPS (7.11% annually) with tax benefit.

### 4b. Index Fund Returns
**`POST /returns:index`**

Calculates compound interest returns via NIFTY 50 Index Fund (14.49% annually). No tax benefit.

**Shared Request format:**
```json
{
  "age": 29,
  "wage": 50000,
  "inflation": 5.5,
  "q": [{"fixed": 0, "start": "2023-07-01 00:00:00", "end": "2023-07-31 23:59:59"}],
  "p": [{"extra": 25, "start": "2023-10-01 08:00:00", "end": "2023-12-31 19:59:59"}],
  "k": [
    {"start": "2023-01-01 00:00:00", "end": "2023-12-31 23:59:59"},
    {"start": "2023-03-01 00:00:00", "end": "2023-11-30 23:59:59"}
  ],
  "transactions": [
    {"date": "2023-02-28 15:49:20", "amount": 375.0, "ceiling": 400.0, "remanent": 25.0},
    {"date": "2023-07-01 21:59:00", "amount": 620.0, "ceiling": 700.0, "remanent": 80.0},
    {"date": "2023-10-12 20:15:30", "amount": 250.0, "ceiling": 300.0, "remanent": 50.0},
    {"date": "2023-12-17 08:09:45", "amount": 480.0, "ceiling": 500.0, "remanent": 20.0}
  ]
}
```

**NPS Response:**
```json
{
  "totalTransactionAmount": 1725.0,
  "totalCeiling": 1900.0,
  "savingsByDates": [
    {
      "start": "2023-01-01 00:00:00",
      "end":   "2023-12-31 23:59:59",
      "amount": 145.0,
      "profit": 86.9,
      "taxBenefit": 0.0
    },
    {
      "start": "2023-03-01 00:00:00",
      "end":   "2023-11-30 23:59:59",
      "amount": 75.0,
      "profit": 44.94,
      "taxBenefit": 0.0
    }
  ]
}
```

**Index Response:**
```json
{
  "totalTransactionAmount": 1725.0,
  "totalCeiling": 1900.0,
  "savingsByDates": [
    {
      "start": "2023-01-01 00:00:00",
      "end":   "2023-12-31 23:59:59",
      "amount": 145.0,
      "profit": 1684.5,
      "taxBenefit": 0.0
    },
    {
      "start": "2023-03-01 00:00:00",
      "end":   "2023-11-30 23:59:59",
      "amount": 75.0,
      "profit": 870.47,
      "taxBenefit": 0.0
    }
  ]
}
```

---

### 5. Performance Report
**`GET /performance`**

Returns live JVM performance metrics. No input required.

```bash
curl http://localhost:5477/blackrock/challenge/v1/performance
```

**Response:**
```json
{
  "time": "00:00:11.135",
  "memory": "85.42 MB",
  "threads": 16
}
```

| Field | Description | Format |
|-------|-------------|--------|
| `time` | Last request duration or uptime | `HH:mm:ss.SSS` |
| `memory` | JVM heap memory in use | `XXX.XX MB` |
| `threads` | Active JVM thread count | Integer |

---

## Business Logic

### Remanent Calculation
```
ceiling  = ⌈amount / 100⌉ × 100
remanent = ceiling - amount

Special cases:
  amount = 0   → ceiling = 0, remanent = 0
  amount % 100 == 0 → ceiling = amount, remanent = 0
  amount < 0   → ceiling = 0, remanent = 0 (validator rejects downstream)
```

### Q Period Rules
- **Purpose:** Override remanent with a fixed amount
- **Conflict resolution:** If multiple Q periods match → use the one with the **latest start date**
- **Tie-breaking:** Same start date → use the **first one in the list**

### P Period Rules
- **Purpose:** Add extra amount to remanent
- **Multiple matches:** ALL matching P periods stack (sum of all extras added)
- **Interaction with Q:** P adds on top of whatever Q left (even if Q set remanent to 0)

### K Period Rules
- **Purpose:** Group transactions into evaluation windows for returns calculation
- **Overlap:** A single transaction can appear in multiple K periods — each K is fully independent
- **No K periods:** All transactions are included

### Investment Formulas

**Compound Interest:**
```
A = P × (1 + r)^t
  r = 0.0711  (NPS)  or  0.1449  (Index Fund)
  t = 60 - age       (if age ≥ 60 → t = 5)
  n = 1 (compounded annually)
```

**Inflation Adjustment:**
```
A_real = A / (1 + inflation)^t
```

**NPS Tax Benefit:**
```
annual_income  = wage × 12
NPS_Deduction  = min(invested, 10% of annual_income, ₹2,00,000)
Tax_Benefit    = Tax(annual_income) - Tax(annual_income - NPS_Deduction)
```

**Tax Slabs (Simplified, Marginal):**
```
₹0          → ₹7,00,000   :  0%
₹7,00,001   → ₹10,00,000  : 10% on amount above ₹7L
₹10,00,001  → ₹12,00,000  : 15% on amount above ₹10L
₹12,00,001  → ₹15,00,000  : 20% on amount above ₹12L
Above ₹15,00,000           : 30% on amount above ₹15L
```

---

## Testing

Tests are located in the `/test` folder.

```bash
# Run all tests
mvn test

# Run with coverage report
mvn test jacoco:report
```

Test types included:
- **Unit Tests** — individual service methods in isolation
- **Integration Tests** — full HTTP request/response via MockMvc
- **Edge Case Tests** — boundary conditions (zero amounts, age ≥ 60, duplicate timestamps, etc.)

---

## Docker Details

### Image Specification

| Property | Value |
|----------|-------|
| Base OS | Alpine Linux (minimal, secure, production-proven) |
| JDK (build) | Eclipse Temurin 21 Alpine |
| JRE (runtime) | Eclipse Temurin 21 JRE Alpine |
| Port | 5477 |
| User | Non-root (`blackrock`) |
| Build strategy | Multi-stage (builder + runtime) |

### Build Command
```bash
docker build -t blk-hacking-ind-{name-lastname} .
```

### Run Command
```bash
docker run -d -p 5477:5477 blk-hacking-ind-{name-lastname}
```

### Why Alpine Linux?
- Minimal footprint (~5MB base image) → fast pull and startup
- Reduced attack surface (fewer pre-installed packages)
- Production-proven in enterprise containerized environments
- Official support from Eclipse Temurin for JVM workloads

### JVM Flags Explained
```
-XX:+UseContainerSupport    → respects Docker memory limits (not host RAM)
-XX:MaxRAMPercentage=75.0   → heap = 75% of container memory
-XX:+UseG1GC                → G1 collector, optimal for throughput
-Djava.security.egd=...     → faster startup (non-blocking random)
```

### Health Check
The container automatically health-checks via:
```
GET /blackrock/challenge/v1/performance
```
Every 30s · 10s timeout · 3 retries · 40s startup grace period

---

## Edge Cases Handled

| Scenario | Behaviour |
|----------|-----------|
| Amount is exact multiple of 100 | remanent = 0, transaction is valid |
| Amount = 0 | ceiling = 0, remanent = 0 |
| Negative amount | ceiling = 0, remanent = 0, flagged invalid by validator |
| Duplicate timestamp | Second occurrence rejected as "Duplicate transaction" |
| Multiple Q periods match | Latest start date wins; tie → first in list |
| Multiple P periods match | All extras summed and added |
| Transaction in both Q and P | Q overrides first, then P adds on top |
| Transaction not in any K period | Excluded from all K sums |
| Transaction in multiple K periods | Counted independently in each K |
| age ≥ 60 | Investment years = 5 (minimum) |
| Annual income ≤ ₹7L | Tax = 0, taxBenefit = 0 |
| Q fixed = 0 | Valid — transaction contributes ₹0 to savings |
| inflation = 0 | A_real = A (formula still works) |
| Empty transaction list | Returns zeroed totals, empty arrays |
| Amount ≥ 5,00,000 | Rejected by validator — exceeds constraint |
| Date format with space (not T) | Handled globally via custom deserializer |

---

## Design Decisions & Tradeoffs

**`double` vs `BigDecimal`**
We used `double` for speed and simplicity at hackathon scale. In a true production financial system, `BigDecimal` would be used to avoid floating-point precision errors. This tradeoff is acceptable here given the scale and time constraints.

**Stateless services**
No database or cache layer — all computation is in-memory per request. This simplifies deployment and keeps the Docker image lean. For a production system with persistent user data, a database would be required.

**Interface + Implementation pattern**
All services are defined as interfaces with a single `impl`. This makes unit testing with mocks trivial and follows Spring best practices.

**Multi-stage Docker build**
Keeps the final image small by excluding Maven, JDK, and build artifacts from the runtime image. Only the JAR and JRE are present in the final container.

**FlexibleLocalDateTimeDeserializer**
Accepts both `yyyy-MM-dd HH:mm:ss` (space) and `yyyy-MM-ddTHH:mm:ss` (ISO) formats. This prevents 400 errors from minor client formatting differences and makes the API more resilient.

---

## Author

**Name:** {Your Name}  
**Email:** {Your Email}  
**Repository:** {GitHub/GitLab URL}
