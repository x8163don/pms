package com.pms.application.fab.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CreateFabRequest {

    @NotNull(message = "cityId must not be null")
    private Long cityId;

    @NotBlank(message = "name must not be blank")
    private String name;

    public Long getCityId() {
        return cityId;
    }

    public void setCityId(Long cityId) {
        this.cityId = cityId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
