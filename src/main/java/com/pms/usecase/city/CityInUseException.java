package com.pms.usecase.city;

public class CityInUseException extends RuntimeException {
    public CityInUseException(String message) {
        super(message);
    }
}
