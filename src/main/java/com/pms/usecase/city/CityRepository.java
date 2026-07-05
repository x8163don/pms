package com.pms.usecase.city;

import com.pms.domain.city.City;

import java.util.Optional;

public interface CityRepository {
    Optional<City> getById(Long id);

    City save(City city);

    void deleteById(Long id);

    boolean existsByName(String name);
}
