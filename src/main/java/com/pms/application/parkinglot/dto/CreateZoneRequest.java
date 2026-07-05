package com.pms.application.parkinglot.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public class CreateZoneRequest {

    @NotBlank(message = "floor must not be blank")
    private String floor;

    @NotBlank(message = "name must not be blank")
    private String name;

    @Min(value = 0, message = "regularTotal must not be negative")
    private int regularTotal;

    @Min(value = 0, message = "flexibleTotal must not be negative")
    private int flexibleTotal;

    private int color;

    private int note;

    public String getFloor() {
        return floor;
    }

    public void setFloor(String floor) {
        this.floor = floor;
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

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public int getNote() {
        return note;
    }

    public void setNote(int note) {
        this.note = note;
    }
}
