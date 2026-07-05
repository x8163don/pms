package com.pms.application.city.dto;

import com.pms.domain.city.City;

public class CityResponse {

    private final Long id;
    private final String name;

    public CityResponse(City city) {
        this.id = city.getId();
        this.name = city.getName();
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
