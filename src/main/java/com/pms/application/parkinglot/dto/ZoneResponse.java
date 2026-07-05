package com.pms.application.parkinglot.dto;

import com.pms.domain.parkinglot.Zone;

public class ZoneResponse {

    private final Long id;
    private final Long lotId;
    private final String floor;
    private final String name;
    private final int regularTotal;
    private final int regularRemain;
    private final int flexibleTotal;
    private final int flexibleRemain;
    private final int allTotal;
    private final int allRemain;
    private final int color;
    private final int note;

    public ZoneResponse(Long lotId, Zone zone) {
        this.id = zone.getId();
        this.lotId = lotId;
        this.floor = zone.getFloor();
        this.name = zone.getName();
        this.regularTotal = zone.getRegularTotal();
        this.regularRemain = zone.getRegularRemain();
        this.flexibleTotal = zone.getFlexibleTotal();
        this.flexibleRemain = zone.getFlexibleRemain();
        this.allTotal = zone.getAllTotal();
        this.allRemain = zone.getAllRemain();
        this.color = zone.getColor();
        this.note = zone.getNote();
    }

    public Long getId() {
        return id;
    }

    public Long getLotId() {
        return lotId;
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

    public int getAllTotal() {
        return allTotal;
    }

    public int getAllRemain() {
        return allRemain;
    }

    public int getColor() {
        return color;
    }

    public int getNote() {
        return note;
    }
}
