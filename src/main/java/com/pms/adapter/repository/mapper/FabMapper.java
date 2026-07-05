package com.pms.adapter.repository.mapper;

import com.pms.adapter.repository.dto.FabDto;
import com.pms.domain.fab.Fab;

public class FabMapper {

    private FabMapper() {
    }

    public static Fab toDomain(FabDto dto) {
        if (dto == null) {
            return null;
        }
        return new Fab(dto.getId(), dto.getCityId(), dto.getName());
    }

    public static FabDto toDto(Fab domain) {
        if (domain == null) {
            return null;
        }
        FabDto dto = new FabDto();
        dto.setId(domain.getId());
        dto.setCityId(domain.getCityId());
        dto.setName(domain.getName());
        return dto;
    }
}
