package com.pms.application.fab.dto;

import com.pms.domain.fab.Fab;

public class FabResponse {

    private final Long id;
    private final Long cityId;
    private final String name;

    public FabResponse(Fab fab) {
        this.id = fab.getId();
        this.cityId = fab.getCityId();
        this.name = fab.getName();
    }

    public Long getId() {
        return id;
    }

    public Long getCityId() {
        return cityId;
    }

    public String getName() {
        return name;
    }
}
