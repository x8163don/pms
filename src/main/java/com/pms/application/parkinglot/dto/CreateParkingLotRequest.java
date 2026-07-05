package com.pms.application.parkinglot.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CreateParkingLotRequest {

    @NotNull(message = "fabId must not be null")
    private Long fabId;

    @NotBlank(message = "name must not be blank")
    private String name;

    @Min(value = 0, message = "regularTotal must not be negative")
    private int regularTotal;

    @Min(value = 0, message = "flexibleTotal must not be negative")
    private int flexibleTotal;

    public Long getFabId() {
        return fabId;
    }

    public void setFabId(Long fabId) {
        this.fabId = fabId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getRegularTotal() {
        return regularTotal;
    }

    public void setRegularTotal(int regularTotal) {
        this.regularTotal = regularTotal;
    }

    public int getFlexibleTotal() {
        return flexibleTotal;
    }

    public void setFlexibleTotal(int flexibleTotal) {
        this.flexibleTotal = flexibleTotal;
    }
}
