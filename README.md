# Employee REST API — ReliaQuest Entry-Level Java Challenge

A protected REST API that exposes employee information for consumption by **Employees-R-US** webhooks.
This is the completed implementation of ReliaQuest's Entry-Level Java Challenge: three endpoints, a service layer and
an in-memory employee store on top of a plain Spring Boot application.

## Challenge requirements and where they are implemented

| Requirement | Implementation |
| --- | --- |
| `GET /api/v1/employee` — all employees, unfiltered | `EmployeeController#getAllEmployees` |
| `GET /api/v1/employee/{uuid}` — single employee | `EmployeeController#getEmployeeByUuid` |
| `POST /api/v1/employee` — create employee | `EmployeeController#createEmployee` |
| "Don't forget to add a Service layer" | `EmployeeService` / `EmployeeServiceImpl` |
| No real persistence layer needed | `ConcurrentHashMap` inside `EmployeeServiceImpl` |
| Protected, secure REST API | API key (`X-API-Key`) enforced by Spring Security |
| Spotless formatting enforced by the build | `spotlessCheck` runs as part of `./gradlew build` |

## Technology

* Java 17
* Spring Boot 3.2.10 — Spring Web, Spring Security, Jakarta Bean Validation
* Gradle 8.12 (wrapper included) with a `buildSrc` convention plugin
* springdoc-openapi 2.3.0 (Swagger UI)
* JUnit 5, MockMvc and AssertJ for the tests

There is **no database and no external service**: the application starts on its own and holds employees in memory,
which is all the assessment asks for.

## Getting started

Prerequisites: a JDK 17 installation (`java -version`). Nothing else.

### Build

```bash
./gradlew clean build
```

The `build` task compiles the code, runs `spotlessCheck` and runs the tests.

### Format

```bash
./gradlew spotlessApply   # format the code
./gradlew spotlessCheck   # verify the formatting (also part of build)
```

### Test

```bash
./gradlew clean test
```

### Run

```bash
./gradlew :api:bootRun
```

The API is then available on <http://localhost:8080> and Swagger UI on
<http://localhost:8080/swagger-ui/index.html>.

To run with your own API key:

```bash
EMPLOYEE_API_KEY=my-secret-key ./gradlew :api:bootRun
```

## Security

Every `/api/v1/employee` endpoint requires the API key in the `X-API-Key` request header.

The key is configured through the `employee.security.api-key` property, which resolves to the `EMPLOYEE_API_KEY`
environment variable and falls back to the development default `local-dev-api-key`:

```yaml
employee:
  security:
    api-key: ${EMPLOYEE_API_KEY:local-dev-api-key}
```

* **Fresh clone / evaluation:** no setup required. Start the application and use `local-dev-api-key`
  (or set `EMPLOYEE_API_KEY` and use that value instead).
* **Real deployments:** set `EMPLOYEE_API_KEY` to a strong secret. No secret is stored in source control and the
  fallback value is intentionally a documented development default, not a credential.
* **Behaviour:** a request with a missing or invalid key receives `401 Unauthorized`; a request with the correct key
  is authenticated as the `employees-r-us` integration. Keys are compared in constant time.
* **Unprotected paths:** `/swagger-ui/**`, `/swagger-ui.html` and `/v3/api-docs/**` are public so the API
  documentation can be read. Calling an employee endpoint from Swagger UI still requires the key.
* **Statelessness:** the API is consumed by webhooks and uses no sessions or cookies; CSRF protection is therefore
  disabled, as is standard for a stateless, header-authenticated API.

## API

Base URL: `http://localhost:8080`, base path: `/api/v1/employee`.
All examples below use the default development key; replace it if you set `EMPLOYEE_API_KEY`.

### GET /api/v1/employee — get all employees

Returns every employee, unfiltered.

```bash
curl -H "X-API-Key: local-dev-api-key" http://localhost:8080/api/v1/employee
```

```json
[
  {
    "uuid": "11111111-1111-1111-1111-111111111111",
    "firstName": "John",
    "lastName": "Doe",
    "fullName": "John Doe",
    "salary": 95000,
    "age": 30,
    "jobTitle": "Software Engineer",
    "email": "john.doe@reliaquest.com",
    "contractHireDate": "2021-03-15T09:00:00Z",
    "contractTerminationDate": null
  },
  {
    "uuid": "22222222-2222-2222-2222-222222222222",
    "firstName": "Jane",
    "lastName": "Smith",
    "fullName": "Jane Smith",
    "salary": 120000,
    "age": 34,
    "jobTitle": "Senior Security Architect",
    "email": "jane.smith@reliaquest.com",
    "contractHireDate": "2019-07-01T09:00:00Z",
    "contractTerminationDate": null
  }
]
```

### GET /api/v1/employee/{uuid} — get employee by UUID

```bash
curl -H "X-API-Key: local-dev-api-key" \
  http://localhost:8080/api/v1/employee/11111111-1111-1111-1111-111111111111
```

| Status | When |
| --- | --- |
| `200 OK` | The employee is returned |
| `400 Bad Request` | The path variable is not a valid UUID |
| `404 Not Found` | No employee exists for the UUID |
| `401 Unauthorized` | Missing or invalid API key |

### POST /api/v1/employee — create employee

The employee UUID is generated server-side and the contract termination date is always `null` for a new employee.
`contractHireDate` defaults to the current time when it is omitted.

```bash
curl -X POST http://localhost:8080/api/v1/employee \
  -H "X-API-Key: local-dev-api-key" \
  -H "Content-Type: application/json" \
  -d '{
        "firstName": "Alice",
        "lastName": "Wonderland",
        "salary": 110000,
        "age": 28,
        "jobTitle": "Product Manager",
        "email": "alice.wonderland@reliaquest.com",
        "contractHireDate": "2024-01-02T09:00:00Z"
      }'
```

```json
{
  "uuid": "3f0f6f0e-6a1c-4f1e-9d3e-6f1b2c3d4e5f",
  "firstName": "Alice",
  "lastName": "Wonderland",
  "fullName": "Alice Wonderland",
  "salary": 110000,
  "age": 28,
  "jobTitle": "Product Manager",
  "email": "alice.wonderland@reliaquest.com",
  "contractHireDate": "2024-01-02T09:00:00Z",
  "contractTerminationDate": null
}
```

`200 OK` is returned on success, which keeps the response contract that already existed in this repository.
Invalid requests are rejected with `400 Bad Request` (see below).

### Request validation

| Field | Rule |
| --- | --- |
| `firstName` | required, not blank, at most 100 characters |
| `lastName` | required, not blank, at most 100 characters |
| `salary` | optional, must be zero or positive |
| `age` | optional, between 18 and 100 |
| `jobTitle` | optional, at most 100 characters |
| `email` | optional, must be a valid e-mail address of at most 254 characters |
| `contractHireDate` | optional ISO-8601 instant, defaults to now |

### Error responses

Errors are returned as [RFC 9457](https://www.rfc-editor.org/rfc/rfc9457) problem details, for example:

```json
{
  "type": "about:blank",
  "title": "Bad Request",
  "status": 400,
  "detail": "Request validation failed",
  "instance": "/api/v1/employee",
  "errors": {
    "firstName": "must not be blank",
    "lastName": "must not be blank"
  }
}
```

| Status | Meaning |
| --- | --- |
| `400 Bad Request` | Invalid body, malformed JSON, missing body or malformed UUID path variable |
| `401 Unauthorized` | Missing or invalid API key |
| `404 Not Found` | No employee exists for the given UUID |
| `500 Internal Server Error` | Unexpected failure; the response never contains a stack trace or internal detail |

## Swagger UI

Swagger UI is served at <http://localhost:8080/swagger-ui/index.html>.

1. Open Swagger UI.
2. Press **Authorize** and paste the API key (`local-dev-api-key` by default, or your `EMPLOYEE_API_KEY`).
3. Run any of the three employee endpoints.

The OpenAPI document declares the same `X-API-Key` header security scheme that the application enforces, so the
Authorize button authenticates exactly like any other client. The OpenAPI document itself is available at
`/v3/api-docs`.

## Project structure

```text
api/src/main/java/com/challenge/api
├── EntryLevelJavaChallengeApplication.java   # Spring Boot entry point
├── config
│   ├── ApiKeyAuthFilter.java                 # authenticates the X-API-Key header
│   ├── OpenApiConfig.java                    # OpenAPI/Swagger metadata and security scheme
│   └── SecurityConfig.java                   # stateless security rules for the API
├── controller
│   └── EmployeeController.java               # the three REST endpoints
├── exception
│   ├── ApiExceptionHandler.java              # maps exceptions to RFC 9457 problem responses
│   └── EmployeeNotFoundException.java
├── model
│   ├── CreateEmployeeInput.java              # validated POST payload
│   ├── Employee.java                         # domain contract
│   └── EmployeeModel.java                    # in-memory implementation
└── service
    ├── EmployeeService.java                  # business operations
    └── EmployeeServiceImpl.java              # in-memory store (ConcurrentHashMap)
```

## Design notes

* **Thin controller, real service layer.** The controller only maps HTTP to service calls; lookups, UUID generation
  and the store live in `EmployeeServiceImpl`, and it is injected as the `EmployeeService` interface.
* **Derived full name.** `EmployeeModel` stores only the first and last name and computes `fullName` on every call,
  so the full name can never contradict the names it is made of.
* **In-memory store.** A `ConcurrentHashMap<UUID, Employee>` gives constant-time lookup, safe concurrent access and
  no risk of one request overwriting an unrelated employee. Sample employees are seeded so the API returns data
  immediately after start-up. Swapping in a real database later means providing another `EmployeeService`
  implementation; nothing else changes.
* **Validation at the edge.** Jakarta Bean Validation on the request record keeps HTTP payload rules in one place and
  produces a `400` before any business code runs.
* **Centralised error handling.** `ApiExceptionHandler` maps domain and framework exceptions to status codes, so the
  controller contains no `try/catch` or `ResponseStatusException` noise and clients never see stack traces.

## Continuous integration

* `.gitlab-ci.yml` runs `./gradlew clean build` (compilation, `spotlessCheck` and tests) on every pipeline.
* `.github/workflows/build.yml` runs the same build on GitHub.

## Repository layout

```text
buildSrc/            # Gradle convention plugin shared by the modules
api/                 # the Spring Boot application module
gradle/wrapper/      # Gradle wrapper (8.12)
```
