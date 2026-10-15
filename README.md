# Dispatch Load Balancer

A Spring Boot REST API that allocates delivery orders to vehicles based on:

- Order priority
- Vehicle capacity
- Distance between a vehicle and an order
- Vehicle route movement after each assignment

The application uses the Haversine formula to calculate geographical distance between latitude/longitude coordinates.

## Tech Stack

- Java 17
- Spring Boot
- Spring Web MVC
- Spring Data JPA
- H2 in-memory database
- Maven Wrapper
- Lombok
- JUnit 5, Mockito, MockMvc
- Swagger / OpenAPI
- JaCoCo

## Running the Application

### Prerequisites

- Java 17 or later
- Internet access for Maven dependency downloads on the first run

### Start the Application

On Windows PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

The application starts at:

```text
http://localhost:8080
```

Swagger UI is available at:

```text
http://localhost:8080/swagger-ui/index.html
```

## Running Tests

Run the complete test suite:

```powershell
.\mvnw.cmd clean test
```

The JaCoCo coverage report is generated at:

```text
target/site/jacoco/index.html
```

## API Endpoints

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/dispatch/vehicles` | Creates or updates vehicles |
| `POST` | `/api/dispatch/orders` | Creates or updates delivery orders |
| `GET` | `/api/dispatch/plan` | Generates the dispatch plan |

## Add Vehicles

`POST /api/dispatch/vehicles`

```json
{
  "vehicles": [
    {
      "vehicleId": "VEH001",
      "capacity": 100,
      "currentLatitude": 12.9716,
      "currentLongitude": 77.5946,
      "currentAddress": "Bengaluru"
    },
    {
      "vehicleId": "VEH002",
      "capacity": 50,
      "currentLatitude": 12.9352,
      "currentLongitude": 77.6245,
      "currentAddress": "Koramangala"
    }
  ]
}
```

## Add Delivery Orders

`POST /api/dispatch/orders`

```json
{
  "orders": [
    {
      "orderId": "ORD001",
      "latitude": 12.975,
      "longitude": 77.6,
      "address": "MG Road",
      "packageWeight": 30,
      "priority": "HIGH"
    },
    {
      "orderId": "ORD002",
      "latitude": 12.936,
      "longitude": 77.625,
      "address": "Koramangala 5th Block",
      "packageWeight": 20,
      "priority": "LOW"
    }
  ]
}
```

## Generate a Dispatch Plan

`GET /api/dispatch/plan`

Example response:

```json
{
  "dispatchPlan": [
    {
      "vehicleId": "VEH001",
      "totalLoad": 30,
      "totalDistance": "0.7 km",
      "assignedOrders": [
        {
          "orderId": "ORD001",
          "latitude": 12.975,
          "longitude": 77.6,
          "address": "MG Road",
          "packageWeight": 30,
          "priority": "HIGH"
        }
      ]
    }
  ],
  "unassignedOrders": []
}
```

## Dispatch Algorithm

The application uses a deterministic greedy allocation algorithm.

1. Orders are processed in priority order: `HIGH`, then `MEDIUM`, then `LOW`.
2. For each priority level, all feasible vehicle-order pairs are evaluated.
3. A vehicle is eligible only when its remaining capacity is at least the order weight.
4. The candidate with the shortest Haversine distance is selected.
5. After an order is assigned:
   - The vehicle's remaining capacity is reduced.
   - The vehicle's total load and total distance are updated.
   - The vehicle's temporary route endpoint moves to the assigned order's location.
6. The process repeats until every order of that priority is allocated or no eligible vehicle remains.

When distances are equal within `0.000001 km`, the following tie-breakers are applied:

1. Vehicle with the lowest sufficient remaining capacity
2. Heavier package weight
3. Lexicographically smaller vehicle ID
4. Lexicographically smaller order ID

This ensures repeatable output for the same input.

## Unassigned Orders

If no vehicle has sufficient remaining capacity for an order, the request still succeeds with `200 OK`. The order is included in `unassignedOrders` with the reason:

```text
No vehicle has sufficient remaining capacity.
```

## Incomplete Dispatch Data

A dispatch plan cannot be generated unless at least one vehicle and one order are available.

`GET /api/dispatch/plan` returns `409 Conflict` when:

- No vehicles and no orders exist
- Orders exist but no vehicles exist
- Vehicles exist but no orders exist

## Project Structure

```text
src/main/java/com/rishitbal/dispatch_load_balancer
├── controller/   # REST API endpoints
├── domain/       # JPA entities and Priority enum
├── dto/          # Request and response objects
├── exception/    # Global exception handling
├── repository/   # Spring Data JPA repositories
└── service/      # Data ingestion and dispatch-planning logic
```

The planning logic is separated into:

- `DispatchPlanningService` — loads and validates data, then coordinates planning
- `DispatchAllocator` — applies allocation rules and maintains temporary vehicle route state
- `DispatchPlanMapper` — converts allocation results into API response DTOs
- `DistanceCalculator` — calculates Haversine distance

## Testing

The test suite includes:

- Haversine distance calculations
- Order and vehicle upsert behavior
- Priority-based allocation
- Capacity constraints and unassigned orders
- Best-fit vehicle selection during distance ties
- Validation error responses
- Malformed request handling
- Empty or incomplete dispatch-data errors
- End-to-end controller integration tests
