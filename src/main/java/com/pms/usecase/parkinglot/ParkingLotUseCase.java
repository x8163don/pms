package com.pms.usecase.parkinglot;

import com.pms.domain.parkinglot.ParkingLot;
import com.pms.domain.parkinglot.Zone;

public interface ParkingLotUseCase {
    ParkingLot createParkingLot(Long fabId, String name, int regularTotal, int flexibleTotal);

    void deleteParkingLot(Long id);

    Zone addZone(Long lotId, String floor, String name, int regularTotal, int flexibleTotal, int color, int note);

    void removeZone(Long lotId, Long zoneId);
}
