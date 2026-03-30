# FX-Rate – Forex Exchange Rate API

API for importing and querying historical forex exchange-rate data (per currency pair).

## Features
- **Import FX data from CSV**: upload a CSV for a `base/quote` pair and process it asynchronously
- **Query currency pairs and history**: fetch pairs and daily OHLC historical records
- **Track import status**: see whether an upload is processing/finished/failed (and row counts)
- **Paginated history**: cursor-based pagination for historical results

## Tech stack
- Java 21
- Spring Boot 4.x (Web, GraphQL, Data JPA, Validation, AMQP)
- PostgreSQL, Flyway
- RabbitMQ
- Testcontainers (integration tests)

## Configuration
Defaults live in `src/main/resources/application.yaml`.

Key settings:
- **Database**: `spring.datasource.*`
- **Rabbit**: `spring.rabbitmq.*` and `app.rabbit.fx-import-queue`
- **Uploads directory**: `app.uploads.dir` (default: `uploads`)

## Run locally (Docker Compose)
This repo includes a `docker-compose.yml` that starts:
- Postgres
- RabbitMQ (with management UI)
- the app (running `./mvnw spring-boot:run` inside a Java 21 container)

```bash
docker compose up
```

App:
- `http://localhost:8080`

RabbitMQ management UI:
- `http://localhost:15672` (guest/guest)

GraphQL endpoint:
- `POST /graphql`

REST endpoints:
- `POST /api/file-uploads` (multipart: `file`, `base`, `quote`)
- `GET /api/file-uploads/{uuid}`

## Example GraphQL queries
List pairs:

```graphql
query {
  pairs { id base quote }
}
```

Paginate historicals for a pair:

```graphql
query($pairId: ID!, $first: Int, $after: String) {
  historicalByPair(pairId: $pairId, first: $first, after: $after) {
    totalCount
    edges { cursor node { id date high low close } }
    pageInfo { endCursor hasNextPage }
  }
}
```

## Upload CSV (REST)

```bash
curl -F "file=@your-data.csv" -F "base=EUR" -F "quote=USD" \
  http://localhost:8080/api/file-uploads
```

Check status:

```bash
curl http://localhost:8080/api/file-uploads/<uuid>
```

## Run tests
Run everything:

```bash
./mvnw test
```

Run a single test class:

```bash
./mvnw -Dtest=PairControllerGraphQlTest test
```

### Integration tests
Integration tests use **Testcontainers** and require a working **Docker** environment.

## Build container image

```bash
docker build -t fx_rate .
docker run --rm -p 8080:8080 fx_rate
```

