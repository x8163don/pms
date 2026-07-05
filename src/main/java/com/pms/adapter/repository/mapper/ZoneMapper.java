package com.pms.adapter.repository.mapper;

import com.pms.adapter.repository.dto.ZoneDto;
import com.pms.domain.parkinglot.Zone;

public class ZoneMapper {

    private ZoneMapper() {
    }

    public static Zone toDomain(ZoneDto dto) {
        if (dto == null) {
            return null;
        }
        return new Zone(
                dto.getId(),
                dto.getFloor(),
                dto.getName(),
                dto.getRegularTotal(),
                dto.getRegularRemain(),
                dto.getFlexibleTotal(),
                dto.getFlexibleRemain(),
                dto.getColor(),
                dto.getNote()
        );
    }

    public static ZoneDto toDto(Zone domain) {
        if (domain == null) {
            return null;
        }
        ZoneDto dto = new ZoneDto();
        dto.setId(domain.getId());
        dto.setFloor(domain.getFloor());
        dto.setName(domain.getName());
        dto.setRegularTotal(domain.getRegularTotal());
        dto.setRegularRemain(domain.getRegularRemain());
        dto.setFlexibleTotal(domain.getFlexibleTotal());
        dto.setFlexibleRemain(domain.getFlexibleRemain());
        dto.setAllTotal(domain.getAllTotal());
        dto.setAllRemain(domain.getAllRemain());
        dto.setColor(domain.getColor());
        dto.setNote(domain.getNote());
        return dto;
    }
}
