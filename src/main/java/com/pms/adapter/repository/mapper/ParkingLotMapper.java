package com.pms.adapter.repository.mapper;

import com.pms.adapter.repository.dto.ParkingLotDto;
import com.pms.adapter.repository.dto.ZoneDto;
import com.pms.domain.parkinglot.ParkingLot;
import com.pms.domain.parkinglot.Zone;

import java.util.List;
import java.util.stream.Collectors;

public class ParkingLotMapper {

    private ParkingLotMapper() {
    }

    public static ParkingLot toDomain(ParkingLotDto dto) {
        if (dto == null) {
            return null;
        }
        List<Zone> zones = dto.getZones().stream()
                .map(ZoneMapper::toDomain)
                .collect(Collectors.toList());

        return new ParkingLot(
                dto.getId(),
                dto.getFabId(),
                dto.getName(),
                dto.getRegularTotal(),
                dto.getRegularRemain(),
                dto.getFlexibleTotal(),
                dto.getFlexibleRemain(),
                zones
        );
    }

    public static ParkingLotDto toDto(ParkingLot domain) {
        if (domain == null) {
            return null;
        }
        ParkingLotDto dto = new ParkingLotDto();
        dto.setId(domain.getId());
        dto.setFabId(domain.getFabId());
        dto.setName(domain.getName());
        dto.setRegularTotal(domain.getRegularTotal());
        dto.setRegularRemain(domain.getRegularRemain());
        dto.setFlexibleTotal(domain.getFlexibleTotal());
        dto.setFlexibleRemain(domain.getFlexibleRemain());
        dto.setAllTotal(domain.getAllTotal());
        dto.setAllRemain(domain.getAllRemain());

        List<ZoneDto> zones = domain.getZones().stream()
                .map(ZoneMapper::toDto)
                .collect(Collectors.toList());
        dto.setZones(zones);

        return dto;
    }
}
