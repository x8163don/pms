package com.pms.application.parkinglot.dto;

import com.pms.domain.parkinglot.ParkingLot;

public class ParkingLotResponse {

    private final Long id;
    private final Long fabId;
    private final String name;
    private final int regularTotal;
    private final int regularRemain;
    private final int flexibleTotal;
    private final int flexibleRemain;
    private final int allTotal;
    private final int allRemain;

    public ParkingLotResponse(ParkingLot parkingLot) {
        this.id = parkingLot.getId();
        this.fabId = parkingLot.getFabId();
        this.name = parkingLot.getName();
        this.regularTotal = parkingLot.getRegularTotal();
        this.regularRemain = parkingLot.getRegularRemain();
        this.flexibleTotal = parkingLot.getFlexibleTotal();
        this.flexibleRemain = parkingLot.getFlexibleRemain();
        this.allTotal = parkingLot.getAllTotal();
        this.allRemain = parkingLot.getAllRemain();
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

    public int getAllTotal() {
        return allTotal;
    }

    public int getAllRemain() {
        return allRemain;
    }
}
