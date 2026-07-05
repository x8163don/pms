package com.pms.domain.city;

import java.util.Objects;

public class City {
    private final Long id;
    private String name;

    public City(String name) {
        this(null, name);
    }

    public City(Long id, String name) {
        this.id = id;
        this.name = validateName(name);
    }

    private String validateName(String name) {
        Objects.requireNonNull(name, "City name cannot be null");
        if (name.isBlank()) {
            throw new IllegalArgumentException("City name cannot be blank");
        }
        if (name.length() > 50) {
            throw new IllegalArgumentException("City name cannot exceed 50 characters");
        }
        return name;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
