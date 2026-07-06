# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Architecture

Clean Architecture / Hexagonal Architecture, base package `com.pms`. Each entity gets four layers with strict inner→outer dependency (outer depends on inner, never the reverse):

`domain` (framework-free POJOs) → `usecase` (ports + use case impls) → `adapter` (JPA/persistence) → `application` (REST controllers/DTOs)

The domain model (City → Fab → ParkingLot → Zone, plus LayoutPlan not yet implemented) is defined in `doc/class-diagram.text`. Use the `java-clean-arch-codegen` skill to generate new entities in this style — but apply the two deviations below, which are already-decided overrides for this project, not the skill's defaults.

### Deviations from the generic codegen pattern

- **Persistence class naming**: adapter-layer JPA classes are named `<Entity>Dto` and live in `adapter.repository.dto` (not `<Entity>Entity` in `entity/`). They still carry `@Entity`/`@Table`/`@Column` — only the name/package differ, because "Entity" is reserved for the Domain layer's DDD entities here.
- **ParkingLot/Zone is one aggregate**: `ParkingLot` is the Aggregate Root; `Zone` is a child entity with no Repository, UseCase, or top-level API of its own. All Zone reads/writes go through `ParkingLotUseCase.addZone(...)`/`removeZone(...)`, exposed as nested routes under `/api/parking-lots/{lotId}/zones`. Before generating a new entity that's clearly a child of an existing one (per the class diagram's cardinalities), ask whether it should be folded into the parent's aggregate like this instead of getting its own full 4-layer stack.
- **UseCase wiring**: UseCase impls (e.g. `CityUseCaseImpl`) are plain POJOs with no `@Service` annotation, by design, so the layer stays framework-free. They're wired manually as `@Bean`s in `com.pms.config.UseCaseConfig`. Any new UseCase impl needs a bean added there or Spring won't pick it up.

## Build / run / test

No global `mvn` on this machine — always use the wrapper:

- `./mvnw compile` — compile
- `./mvnw test` — run tests
- `./mvnw spring-boot:run` — run the app (port 8080)
- `./mvnw package` — build the jar

## Database

- Dev/unit tests: in-memory H2 (`src/main/resources/application.properties`, `spring.jpa.hibernate.ddl-auto=update`). `CityUseCaseImplTest`/`FabUseCaseImplTest`/`ParkingLotUseCaseImplTest` are pure Mockito unit tests and never touch a database at all.
- `PmsApplicationTests` (`@SpringBootTest`) verifies against a real MariaDB via Testcontainers instead of H2: `TestcontainersConfiguration` registers a `MariaDBContainer` `@Bean` with `@ServiceConnection`, which Spring Boot auto-wires as the datasource — no manual `@DynamicPropertySource` needed.
- Requires Docker running locally. On Colima, the Ryuk reaper container can't bind-mount Colima's `docker.sock`, so `pom.xml`'s `maven-surefire-plugin` sets `TESTCONTAINERS_RYUK_DISABLED=true` for test runs — Testcontainers still cleans up containers via JVM shutdown hooks.
- Production target: MariaDB — the driver (`org.mariadb.jdbc:mariadb-java-client`) is currently `test`-scope only (for the Testcontainers verification above); production datasource config still isn't wired up.
