## Code Style
- All public methods must be placed at the top of the class, before any private or protected methods.

# Copilot Instructions for proxy-gateway

## Overview
- This file contains Copilot-specific instructions and coding conventions for the proxy-gateway project.
- For full project design, requirements, and API details, see [SPECIFICATION.md](../SPECIFICATION.md).

## Key Files & Structure
- See [SPECIFICATION.md](../SPECIFICATION.md) for data models, API endpoints, and business logic.
- Additional files:
  - `src/main/resources/application.properties`: Quarkus and DB config.
  - `docker-compose.yml`: Defines both the gateway and its required PostgreSQL instance.
  - `README.md`: Build, run, and packaging instructions.

## Developer Workflows
- **Build:** Use `./mvnw clean install` or `./mvnw package`.
- **Run (dev mode):** `./mvnw quarkus:dev` (enables hot reload and Dev UI at `/q/dev`).
- **Run (prod):** `docker-compose up --build` (runs both gateway and DB).
- **Native build:** `./mvnw package -Dnative` (see README for details).

## Patterns & Conventions
- JPA entities use Lombok for constructors and Panache for ORM.
- Service registration is idempotent: re-registering updates existing records.
- Database connection uses container name (`postgres`) for JDBC URL in compose.
- Healthcheck endpoint is `/q/health` (used in Docker healthcheck).
- All environment variables for DB are set in `docker-compose.yml` for portability.

## Integration Points
- PostgreSQL is required; schema is auto-generated/updated by Quarkus ORM.
- External services can connect to the proxy-gateway via Docker network (`proxy-gateway_default`) or mapped port (`localhost:8080`).

## Examples
- Register a service: `POST /registry` with JSON body `{ "application": "string", "url": "string" }`
- Proxy a request: `GET /{service}/{endpoint}` (see [SPECIFICATION.md](../SPECIFICATION.md) for all supported REST methods)
- Add another service: run with `--network=proxy-gateway_default` to access gateway by container name.

## References
- See [README.md](../README.md) and [SPECIFICATION.md](../SPECIFICATION.md) for more details on build, run, and API usage.

---
