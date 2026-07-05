package com.pms.application.city.dto;

import jakarta.validation.constraints.NotBlank;

public class CreateCityRequest {

    @NotBlank(message = "name must not be blank")
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
