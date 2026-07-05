package com.pms.adapter.repository.mapper;

import com.pms.adapter.repository.dto.CityDto;
import com.pms.domain.city.City;

public class CityMapper {

    private CityMapper() {
    }

    public static City toDomain(CityDto dto) {
        if (dto == null) {
            return null;
        }
        return new City(dto.getId(), dto.getName());
    }

    public static CityDto toDto(City domain) {
        if (domain == null) {
            return null;
        }
        CityDto dto = new CityDto();
        dto.setId(domain.getId());
        dto.setName(domain.getName());
        return dto;
    }
}
