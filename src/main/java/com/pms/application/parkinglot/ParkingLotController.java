package com.pms.application.parkinglot;

import com.pms.application.parkinglot.dto.CreateParkingLotRequest;
import com.pms.application.parkinglot.dto.CreateZoneRequest;
import com.pms.application.parkinglot.dto.ParkingLotResponse;
import com.pms.application.parkinglot.dto.ZoneResponse;
import com.pms.domain.parkinglot.ParkingLot;
import com.pms.domain.parkinglot.Zone;
import com.pms.usecase.parkinglot.ParkingLotUseCase;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/parking-lots")
public class ParkingLotController {

    private final ParkingLotUseCase parkingLotUseCase;

    public ParkingLotController(ParkingLotUseCase parkingLotUseCase) {
        this.parkingLotUseCase = parkingLotUseCase;
    }

    @PostMapping
    public ResponseEntity<ParkingLotResponse> createParkingLot(@Valid @RequestBody CreateParkingLotRequest request) {
        ParkingLot parkingLot = parkingLotUseCase.createParkingLot(
                request.getFabId(), request.getName(), request.getRegularTotal(), request.getFlexibleTotal());
        return ResponseEntity.ok(new ParkingLotResponse(parkingLot));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteParkingLot(@PathVariable("id") Long id) {
        parkingLotUseCase.deleteParkingLot(id);
        return ResponseEntity.noContent().build();
    }

    // Zone is a child entity of the ParkingLot aggregate — it has no top-level API of its own.
    @PostMapping("/{lotId}/zones")
    public ResponseEntity<ZoneResponse> addZone(@PathVariable("lotId") Long lotId,
                                                 @Valid @RequestBody CreateZoneRequest request) {
        Zone zone = parkingLotUseCase.addZone(
                lotId, request.getFloor(), request.getName(),
                request.getRegularTotal(), request.getFlexibleTotal(),
                request.getColor(), request.getNote());
        return ResponseEntity.ok(new ZoneResponse(lotId, zone));
    }

    @DeleteMapping("/{lotId}/zones/{zoneId}")
    public ResponseEntity<Void> removeZone(@PathVariable("lotId") Long lotId,
                                            @PathVariable("zoneId") Long zoneId) {
        parkingLotUseCase.removeZone(lotId, zoneId);
        return ResponseEntity.noContent().build();
    }
}
