---
name: java-clean-arch-codegen
description: Generate complete Java Spring Boot code following Clean Architecture (Hexagonal Architecture). Use this skill whenever the user wants to implement a new feature or entity in Java using Clean Architecture layers (Domain, Usecase, Adapter, Application). Triggers on: entity spec with fields/business rules, "generate Java layers for X", "implement X with clean architecture", "scaffold X entity", "write TDD implementation for X in Java", requirement + test descriptions in a Java project. Always invoke even for partial specs — generate what you can and ask only for missing critical info (entity name or fields).
---

# Java Clean Architecture Code Generator

Generate complete, compilable Java Spring Boot code following Clean Architecture / Hexagonal Architecture. Given an entity specification and optional test descriptions, produce all four architecture layers with zero placeholder stubs.

## Reference Files

Load `references/architecture.md` at the start (package structure + naming conventions). Load each layer's reference only when you reach that layer:

| Layer | Reference |
|---|---|
| Architecture overview, packages, naming | `references/architecture.md` |
| Domain entity, enum, strategy interface | `references/domain_layer.md` |
| Usecase ports, impl, events, exceptions | `references/usecase_layer.md` |
| JPA entity, mapper, repository, SDK services | `references/adapter_layer.md` |
| Controller, DTOs, global exception handler | `references/application_layer.md` |

## Input Format

```
Entity: <PascalCase name>
Fields:
  - <fieldName> (<JavaType>) [description, optional]
Business Rules:
  - <methodName>(): description
Outbound Dependencies:
  - <InterfaceName>: purpose
API Endpoints:
  - <METHOD> <path>: description
Tests: (optional — if present, generate tests BEFORE implementation)
  - <test scenario description>
```

If the user's input is a paragraph rather than a structured spec, extract entity name, fields, and business rules yourself and confirm before generating. Only block on input if the entity name or fields are completely absent.

## Workflow

### Step 1 — Parse & Confirm

Extract from user input:
- Entity name (ensure PascalCase)
- Fields with Java types
- Business rules → method signatures
- Outbound dependencies (repositories, external services)
- API endpoints
- Test descriptions (if any)

If a base package name is not provided, use `com.example.<project>` and note it in your output.

### Step 2 — (TDD) Generate Tests First

Only run this step if test descriptions are provided.

**Domain unit tests** — `<Entity>Test.java`
- Package: `com.example.<project>.domain.<entity>`
- JUnit 5 only, no mocks (Domain is pure Java)
- One test per business rule: normal case + at least one edge case

**Usecase unit tests** — `<Entity>UseCaseImplTest.java`
- Package: `com.example.<project>.usecase.<entity>`
- JUnit 5 + Mockito: `@Mock` all Outbound Ports, `@InjectMocks` the Impl
- One test per use case method

### Step 3 — Generate All Four Layers (Inner → Outer)

Outer layers depend on inner layers, so generate in this order:

1. **Domain** — entity, enum/status, strategy interface (if applicable)
2. **Usecase** — outbound port interfaces, inbound port interface, impl, exceptions, events
3. **Adapter** — JPA entity, mapper, JpaRepository, RepositoryImpl, (ServiceImpl if needed)
4. **Application** — request DTO, response DTO, controller, global exception handler

Consult the matching reference file for each layer before generating it.

### Step 4 — Output Format

Every file uses this header so the user can place it directly:

```
// === <Layer> Layer ===
// File: src/main/java/<com/example/project/layer/package/ClassName.java>

<complete Java source — full package declaration, all imports, zero TODOs>
```

## Quality Checklist

Run through this mentally before each layer:

### Domain
- Zero Spring / JPA / Lombok / Jackson annotations — only `java.*` imports
- Business logic lives in entity methods, not in UseCase
- Enum defined in the same domain package
- Strategy interface + multiple implementations when behaviour varies by type (e.g., `Plan` → `FreePlan` / `StandardPlan`)
- No nulls in constructor — use `Objects.requireNonNull()`

### Usecase
- Impl only imports Domain and other Usecase interfaces — **never** imports Adapter or Application classes
- `@Transactional` on every write method (methods that call `repository.save()` or mutate state)
- Domain exceptions (e.g., `BookingNotFoundException`) defined in the usecase package, extend `RuntimeException`
- Domain events published **after** a successful `save()`, never before
- Constructor injection only — no `@Autowired` on fields

### Adapter
- `<Entity>Mapper` uses **static methods only** — not a Spring bean, not injected
- `<Entity>Entity.java` carries all JPA annotations (`@Entity`, `@Table`, `@Column`); the Domain class has none
- `<Entity>RepositoryImpl` implements the **Usecase Outbound Port** interface — not Spring's `JpaRepository` directly
- External SDK exceptions (e.g., Stripe `StripeException`) are caught here and rethrown as a domain-friendly `<Provider>Exception`
- `@Component` on RepositoryImpl, `@Service` on service impls

### Application
- Controller has **zero** `if/else` business logic — route → UseCase call → response only
- Request DTOs use `jakarta.validation` (`@NotBlank`, `@NotNull`, `@Min`, etc.) + `@Valid` on `@RequestBody`
- `GlobalExceptionHandler` (`@RestControllerAdvice`) handles: `<Entity>NotFoundException` → 404, `IllegalArgumentException` → 400, `MethodArgumentNotValidException` → 400, generic `Exception` → 500
- Response DTOs never expose Domain objects directly
- `@PathVariable` / `@RequestBody` parameters typed correctly

## Anti-Patterns (Never Do These)

| Wrong | Right |
|---|---|
| `@Transactional` on `RepositoryImpl.save()` | `@Transactional` on `UseCaseImpl.write*()` |
| `import ...adapter.repository.AccountEntity` inside Usecase | Usecase never imports Adapter |
| `@Entity` on `Account.java` (domain class) | `@Entity` on `AccountEntity.java` (adapter class) |
| `eventPublisher.publish(...)` before `repository.save(...)` | Save first, publish after |
| `throw new UnsupportedOperationException("TODO")` in output | Complete every method |
| Returning Domain object from Controller | Wrap in Response DTO |

## Generated File Structure (Example: `Entity: Booking`)

```
// Domain
domain/booking/BookingStatus.java
domain/booking/Booking.java

// Usecase
usecase/booking/BookingRepository.java          ← Outbound Port
usecase/booking/BookingUseCase.java             ← Inbound Port
usecase/booking/BookingUseCaseImpl.java
usecase/booking/BookingNotFoundException.java
usecase/booking/event/BookingConfirmedEvent.java

// Adapter
adapter/repository/entity/BookingEntity.java
adapter/repository/mapper/BookingMapper.java
adapter/repository/BookingJpaRepository.java
adapter/repository/BookingRepositoryImpl.java

// Application
application/booking/dto/CreateBookingRequest.java
application/booking/dto/BookingResponse.java
application/booking/BookingController.java
application/exception/GlobalExceptionHandler.java
```
