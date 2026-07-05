package com.pms.usecase.parkinglot;

public class ParkingLotInUseException extends RuntimeException {
    public ParkingLotInUseException(String message) {
        super(message);
    }
}
