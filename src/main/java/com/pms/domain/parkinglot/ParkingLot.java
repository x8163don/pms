package com.pms.domain.parkinglot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Aggregate root. Zone is a child entity of this aggregate — all Zone
 * mutations must go through ParkingLot so the aggregate is always
 * loaded/saved as a single consistency boundary.
 */
public class ParkingLot {
    private final Long id;
    private final Long fabId;
    private String name;
    private final int regularTotal;
    private int regularRemain;
    private final int flexibleTotal;
    private int flexibleRemain;
    private final List<Zone> zones;

    /** Creation constructor — remain counts start out equal to the total counts. */
    public ParkingLot(Long fabId, String name, int regularTotal, int flexibleTotal) {
        this(null, fabId, name, regularTotal, regularTotal, flexibleTotal, flexibleTotal, new ArrayList<>());
    }

    /** Reconstruction constructor — used to rebuild an existing record (with its Zones) from storage. */
    public ParkingLot(Long id, Long fabId, String name, int regularTotal, int regularRemain,
                       int flexibleTotal, int flexibleRemain, List<Zone> zones) {
        this.id = id;
        this.fabId = Objects.requireNonNull(fabId, "Fab id cannot be null");
        this.name = validateName(name);
        this.regularTotal = validateNonNegative(regularTotal, "regularTotal");
        this.regularRemain = validateNonNegative(regularRemain, "regularRemain");
        this.flexibleTotal = validateNonNegative(flexibleTotal, "flexibleTotal");
        this.flexibleRemain = validateNonNegative(flexibleRemain, "flexibleRemain");
        this.zones = new ArrayList<>(Objects.requireNonNull(zones, "Zones cannot be null"));
    }

    private String validateName(String name) {
        Objects.requireNonNull(name, "ParkingLot name cannot be null");
        if (name.isBlank()) {
            throw new IllegalArgumentException("ParkingLot name cannot be blank");
        }
        if (name.length() > 50) {
            throw new IllegalArgumentException("ParkingLot name cannot exceed 50 characters");
        }
        return name;
    }

    private int validateNonNegative(int value, String field) {
        if (value < 0) {
            throw new IllegalArgumentException(field + " cannot be negative");
        }
        return value;
    }

    /** Adds a new Zone to this aggregate; returns the created Zone (not yet persisted). */
    public Zone addZone(String floor, String name, int regularTotal, int flexibleTotal, int color, int note) {
        Zone zone = new Zone(floor, name, regularTotal, flexibleTotal, color, note);
        zones.add(zone);
        return zone;
    }

    /** Removes the Zone with the given id from this aggregate; returns false if no such Zone exists. */
    public boolean removeZone(Long zoneId) {
        return zones.removeIf(zone -> zoneId.equals(zone.getId()));
    }

    public int getAllTotal() {
        return regularTotal + flexibleTotal;
    }

    public int getAllRemain() {
        return regularRemain + flexibleRemain;
    }

    public Long getId() {
        return id;
    }

    public Long getFabId() {
        return fabId;
    }

    public String getName() {
        return name;
    }

    public int getRegularTotal() {
        return regularTotal;
    }

    public int getRegularRemain() {
        return regularRemain;
    }

    public int getFlexibleTotal() {
        return flexibleTotal;
    }

    public int getFlexibleRemain() {
        return flexibleRemain;
    }

    public List<Zone> getZones() {
        return Collections.unmodifiableList(zones);
    }
}
