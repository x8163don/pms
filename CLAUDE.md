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

- Dev/test: in-memory H2 (`src/main/resources/application.properties`, `spring.jpa.hibernate.ddl-auto=update`).
- Production target: MariaDB — not yet wired up (no driver dependency or datasource config exists yet).
