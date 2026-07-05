package com.pms.usecase.fab;

public class FabNotFoundException extends RuntimeException {
    public FabNotFoundException(String message) {
        super(message);
    }
}
