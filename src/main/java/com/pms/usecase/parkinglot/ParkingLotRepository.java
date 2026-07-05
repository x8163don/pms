package com.pms.usecase.parkinglot;

import com.pms.domain.parkinglot.ParkingLot;

import java.util.Optional;

public interface ParkingLotRepository {
    Optional<ParkingLot> getById(Long id);

    ParkingLot save(ParkingLot parkingLot);

    void deleteById(Long id);

    boolean existsByFabId(Long fabId);

    boolean existsByName(String name);
}
