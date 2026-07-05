---
name: pms-clean-arch-codegen
description: Generate new entities/features for this repo (com.pms) following its Clean Architecture layering. Use when adding a new domain entity, or a new feature to an existing one (City, Fab, ParkingLot, Zone, or a future one like LayoutPlan). This is a project-specific override of the generic java-clean-arch-codegen skill — same four-layer workflow, but with this repo's two established conventions applied automatically instead of re-derived or asked about.
---

# PMS Clean Architecture Codegen

This project (base package `com.pms`) follows Clean/Hexagonal Architecture with four layers, inner→outer dependency only:

`domain` (framework-free POJOs) → `usecase` (ports + use case impls) → `adapter` (JPA/persistence) → `application` (REST controllers/DTOs)

If the general-purpose `java-clean-arch-codegen` skill is available, use its workflow and reference docs (input parsing, per-layer generation order, quality checklist) as the base process. Regardless of whether that skill is available, **always apply the two overrides below** — they are already-decided conventions for this repo, not defaults to re-derive.

## Override 1 — Persistence class naming

Adapter-layer JPA classes are named `<Entity>Dto`, in package `adapter.repository.dto` (not `<Entity>Entity` in an `entity/` package).

- Still carries `@Entity`, `@Table`, `@Column` — only the class name and package differ.
- Mapper methods: `toDto(...)` / `toDomain(...)` — not `toEntity(...)` — so the method name matches its return type.
- Example: `com.pms.adapter.repository.dto.CityDto`, `CityMapper.toDto(City)`.

## Override 2 — Check for aggregate boundaries before generating

Before generating a full 4-layer stack for a new entity, check the class diagram (`doc/class-diagram.text`) for its relationship to existing entities. If the new entity is clearly a **child** of an existing one under a `1 -- 0..*` cardinality (e.g. a new `LayoutPlan` under `ParkingLot`), **ask the user** whether it should:

1. Get its own full 4-layer stack (independent Repository/UseCase/Controller), or
2. Be folded into the parent's aggregate — no own Repository/UseCase/Controller; parent's UseCase gets `addX(parentId, ...)`/`removeX(parentId, childId)`; child persisted via the parent's `@OneToMany(cascade=ALL, orphanRemoval=true)`; API nested under the parent's routes.

Don't assume one or the other — `ParkingLot`/`Zone` already went with option 2 (see below), but that was a specific choice, not a rule that applies to every parent/child pair.

### Reference: how ParkingLot/Zone implements option 2

- `Zone` lives in `domain.parkinglot` (not its own `domain.zone` package) and has no FK field of its own — it's contained by `ParkingLot.zones`.
- `ParkingLot.addZone(...)` / `removeZone(zoneId)` are the only mutators; no `ZoneRepository`/`ZoneUseCase` exist.
- `ZoneNotFoundException` lives in `usecase.parkinglot` (the parent's usecase package).
- `ParkingLotDto` owns `@OneToMany(cascade=CascadeType.ALL, orphanRemoval=true) @JoinColumn(name="lot_id") @OrderColumn(name="zone_order")` — the `@OrderColumn` matters: without it, list order after a save/reload isn't guaranteed, which `ParkingLotUseCaseImpl.addZone`'s "last element = newly added" logic depends on.
- `CreateZoneRequest`/`ZoneResponse` DTOs live under `application.parkinglot.dto`, exposed via `ParkingLotController` at `/api/parking-lots/{lotId}/zones`.

## Other repo conventions to carry over

- UseCase impls (e.g. `CityUseCaseImpl`) have no `@Service` annotation — wire any new one as a `@Bean` in `com.pms.config.UseCaseConfig`, or Spring won't find it.
- Dev/test DB is in-memory H2 (`application.properties`); production target is MariaDB (not yet wired up).
