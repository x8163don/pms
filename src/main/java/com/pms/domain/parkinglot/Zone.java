package com.pms.domain.parkinglot;

import java.util.Objects;

/**
 * Zone is a child entity of the ParkingLot aggregate — it has no
 * existence, repository, or use case independent of its parent ParkingLot.
 */
public class Zone {
    private final Long id;
    private String floor;
    private String name;
    private final int regularTotal;
    private int regularRemain;
    private final int flexibleTotal;
    private int flexibleRemain;
    private int color;
    private int note;

    /** Creation constructor — remain counts start out equal to the total counts. */
    public Zone(String floor, String name, int regularTotal, int flexibleTotal, int color, int note) {
        this(null, floor, name, regularTotal, regularTotal, flexibleTotal, flexibleTotal, color, note);
    }

    /** Reconstruction constructor — used to rebuild an existing record from storage. */
    public Zone(Long id, String floor, String name, int regularTotal, int regularRemain,
                int flexibleTotal, int flexibleRemain, int color, int note) {
        this.id = id;
        this.floor = validateFloor(floor);
        this.name = validateName(name);
        this.regularTotal = validateNonNegative(regularTotal, "regularTotal");
        this.regularRemain = validateNonNegative(regularRemain, "regularRemain");
        this.flexibleTotal = validateNonNegative(flexibleTotal, "flexibleTotal");
        this.flexibleRemain = validateNonNegative(flexibleRemain, "flexibleRemain");
        this.color = color;
        this.note = note;
    }

    private String validateFloor(String floor) {
        Objects.requireNonNull(floor, "Zone floor cannot be null");
        if (floor.isBlank()) {
            throw new IllegalArgumentException("Zone floor cannot be blank");
        }
        return floor;
    }

    private String validateName(String name) {
        Objects.requireNonNull(name, "Zone name cannot be null");
        if (name.isBlank()) {
            throw new IllegalArgumentException("Zone name cannot be blank");
        }
        return name;
    }

    private int validateNonNegative(int value, String field) {
        if (value < 0) {
            throw new IllegalArgumentException(field + " cannot be negative");
        }
        return value;
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

    public String getFloor() {
        return floor;
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

    public int getColor() {
        return color;
    }

    public int getNote() {
        return note;
    }
}
