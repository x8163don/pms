package com.pms.domain.fab;

import java.util.Objects;

public class Fab {
    private final Long id;
    private final Long cityId;
    private String name;

    public Fab(Long cityId, String name) {
        this(null, cityId, name);
    }

    public Fab(Long id, Long cityId, String name) {
        this.id = id;
        this.cityId = Objects.requireNonNull(cityId, "City id cannot be null");
        this.name = validateName(name);
    }

    private String validateName(String name) {
        Objects.requireNonNull(name, "Fab name cannot be null");
        if (name.isBlank()) {
            throw new IllegalArgumentException("Fab name cannot be blank");
        }
        if (name.length() > 50) {
            throw new IllegalArgumentException("Fab name cannot exceed 50 characters");
        }
        return name;
    }

    public Long getId() {
        return id;
    }

    public Long getCityId() {
        return cityId;
    }

    public String getName() {
        return name;
    }
}
