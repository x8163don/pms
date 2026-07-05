package com.pms.usecase.city;

import com.pms.domain.city.City;

public interface CityUseCase {
    City createCity(String name);

    void deleteCity(Long id);
}
