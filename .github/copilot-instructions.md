# Copilot Instructions for proxy-gateway

## Project Overview
- This is a Quarkus-based Java microservice acting as a dynamic reverse proxy and service registry.
- Main components: service registration (persisted in PostgreSQL), dynamic proxying, and health monitoring.
- Data flows: Services register via REST, requests are proxied based on registry lookups.

## Key Files & Structure
- `src/main/java/com/dmdr/gateway/model/db/RegistryUrlEntity.java`: JPA entity for service registry table.
- `src/main/resources/application.properties`: Quarkus and DB config.
- `docker-compose.yml`: Defines both the gateway and its required PostgreSQL instance.
- `README.md`: Build, run, and packaging instructions.
- `SPECIFICATION.md`: Functional and data model specification.

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
- Registry table: `registry_url` (see SPECIFICATION.md for schema).

## Examples
- Register a service: `POST /registry` with JSON body `{ "application": "string", "url": "string" }`
- Proxy a request: `GET /{service}/{endpoint}`
- Add another service: run with `--network=proxy-gateway_default` to access gateway by container name.

## References
- See `README.md` and `SPECIFICATION.md` for more details on build, run, and API usage.

---
