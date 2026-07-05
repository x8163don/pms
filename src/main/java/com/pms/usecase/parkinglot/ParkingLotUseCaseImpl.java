package com.pms.usecase.parkinglot;

import com.pms.domain.parkinglot.ParkingLot;
import com.pms.domain.parkinglot.Zone;
import com.pms.usecase.fab.FabNotFoundException;
import com.pms.usecase.fab.FabRepository;
import org.springframework.transaction.annotation.Transactional;

public class ParkingLotUseCaseImpl implements ParkingLotUseCase {

    private final ParkingLotRepository parkingLotRepository;
    private final FabRepository fabRepository;

    public ParkingLotUseCaseImpl(ParkingLotRepository parkingLotRepository, FabRepository fabRepository) {
        this.parkingLotRepository = parkingLotRepository;
        this.fabRepository = fabRepository;
    }

    @Override
    @Transactional
    public ParkingLot createParkingLot(Long fabId, String name, int regularTotal, int flexibleTotal) {
        fabRepository.getById(fabId)
                .orElseThrow(() -> new FabNotFoundException("Fab not found for ID: " + fabId));

        ParkingLot parkingLot = new ParkingLot(fabId, name.trim(), regularTotal, flexibleTotal);

        if (parkingLotRepository.existsByName(parkingLot.getName())) {
            throw new IllegalArgumentException("ParkingLot name already exists: " + parkingLot.getName());
        }

        return parkingLotRepository.save(parkingLot);
    }

    @Override
    @Transactional
    public void deleteParkingLot(Long id) {
        ParkingLot parkingLot = parkingLotRepository.getById(id)
                .orElseThrow(() -> new ParkingLotNotFoundException("ParkingLot not found for ID: " + id));

        if (!parkingLot.getZones().isEmpty()) {
            throw new ParkingLotInUseException("Cannot delete ParkingLot " + id + " because it still has Zones");
        }

        parkingLotRepository.deleteById(id);
    }

    @Override
    @Transactional
    public Zone addZone(Long lotId, String floor, int name, int regularTotal, int flexibleTotal,
                         int color, int note) {
        ParkingLot parkingLot = parkingLotRepository.getById(lotId)
                .orElseThrow(() -> new ParkingLotNotFoundException("ParkingLot not found for ID: " + lotId));

        parkingLot.addZone(floor, name, regularTotal, flexibleTotal, color, note);
        ParkingLot saved = parkingLotRepository.save(parkingLot);

        return saved.getZones().get(saved.getZones().size() - 1);
    }

    @Override
    @Transactional
    public void removeZone(Long lotId, Long zoneId) {
        ParkingLot parkingLot = parkingLotRepository.getById(lotId)
                .orElseThrow(() -> new ParkingLotNotFoundException("ParkingLot not found for ID: " + lotId));

        if (!parkingLot.removeZone(zoneId)) {
            throw new ZoneNotFoundException("Zone not found for ID: " + zoneId + " in ParkingLot " + lotId);
        }

        parkingLotRepository.save(parkingLot);
    }
}
