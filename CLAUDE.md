# DriverKonnect Backend

Spring Boot (Java 21, Maven) REST API with PostgreSQL + Flyway, JWT auth via httpOnly cookies, springdoc Swagger.

## Build & run
- JDK 21 is required. Set `JAVA_HOME` to a JDK 21 installation before running `mvnw`.
- Compile: `./mvnw clean compile`. Run: `./mvnw spring-boot:run` (default port 8080, `SERVER_PORT` overrides).
- `DriverKonnectApplicationTests.contextLoads` needs a reachable database (datasource env vars).
- Swagger UI: `http://localhost:8080/swagger-ui.html`. OpenAPI JSON: `/v3/api-docs`.

## Code conventions
- Layering: `controller` → `service` (interface) → `service/implementation` → `repository` → `entity`. DTOs go in `dto/request/<area>` and `dto/response/<area>`.
- Wrap responses with `ResponseUtil.success(...)` / `ResponseUtil.created(...)` → `Response<T>` (`data`, `message`, `status`). Use `PagedResponseDto<T>` for paginated lists.
- Errors: throw `CustomException(message, httpStatus)`. `GlobalExceptionHandler` maps it.
- Every endpoint gets `@Operation(summary, description)`, and every controller gets an `@Tag`.
- Admin controllers live under `/api/admin/**` with `@PreAuthorize("hasRole('ADMIN')")`. Public endpoints must be added to `SecurityConfig`.
- Schema changes only go in a new Flyway migration `src/main/resources/db/migration/V<next>__<desc>.sql`. Never edit an applied migration.
- Current user: `AuthUtil.getAuthenticatedUser().getUsername()` (this is the email).

## Documentation & Postman: REQUIRED for every API change

Any task that adds or changes an endpoint is **not done** until both of the following exist and match the code. Other developers use them to understand and test the work.

### 1. API doc: `docs/api/<feature>.md`
Write one Markdown file per feature or controller area, in kebab-case (e.g. `tour-company-registration.md`). Update the existing file when you change a feature; don't create a duplicate. Use `docs/api/tour-company-registration.md` and `docs/api/driver-registration.md` as the style reference. Include:
- **Overview:** what was implemented and why, in a couple of sentences, plus the base path.
- **Auth model:** public, or which role is required, and any cookie/session side effects.
- **State/lifecycle table** if the feature has statuses: allowed transitions and their results.
- **Per endpoint:** method + path, path/query params, a request body field table (type, required, validation notes), a JSON request example, a JSON success response example with its status code, and an error table (status → when).
- **Suggested frontend flow:** a short numbered list.
- **Known limitations** or follow-ups, if any.

### 2. Postman collection: `docs/postman/<feature>.postman_collection.json`
Use the same `<feature>` name as the doc. The file must import directly into Postman (schema v2.1). Use `docs/postman/tour-company-approval.postman_collection.json` as the template. Requirements:
- Collection variables: `baseUrl` (default `http://localhost:8080`), credentials (`adminEmail`, `adminPassword`, etc. with placeholder values, **never real credentials**), and any ids the flow captures.
- Number the folders and requests so they run top to bottom in Collection Runner. Start each folder with the "Login as …" request it needs. Auth is cookie based, so a successful login switches the active user.
- Pre-request scripts generate unique test data (e.g. timestamped emails) so the collection can be re-run.
- Test scripts capture ids from responses (`pm.collectionVariables.set`). Every request asserts its expected status code and its key fields, including negative cases (400/401/403/404/409).
- Put a collection `description` explaining setup and run order.
- Validate the JSON parses before finishing.

### Checklist before reporting an API task as complete
- [ ] Code compiles (`./mvnw clean compile`)
- [ ] `docs/api/<feature>.md` created/updated
- [ ] `docs/postman/<feature>.postman_collection.json` created/updated and valid JSON
- [ ] Swagger `@Operation` / `@Tag` text matches the doc
