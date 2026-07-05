package com.pms.usecase.city;

import com.pms.domain.city.City;
import com.pms.usecase.fab.FabRepository;
import org.springframework.transaction.annotation.Transactional;

public class CityUseCaseImpl implements CityUseCase {

    private final CityRepository cityRepository;
    private final FabRepository fabRepository;

    public CityUseCaseImpl(CityRepository cityRepository, FabRepository fabRepository) {
        this.cityRepository = cityRepository;
        this.fabRepository = fabRepository;
    }

    @Override
    @Transactional
    public City createCity(String name) {
        City city = new City(name.trim());

        if (cityRepository.existsByName(city.getName())) {
            throw new IllegalArgumentException("City name already exists: " + city.getName());
        }

        return cityRepository.save(city);
    }

    @Override
    @Transactional
    public void deleteCity(Long id) {
        cityRepository.getById(id)
                .orElseThrow(() -> new CityNotFoundException("City not found for ID: " + id));

        if (fabRepository.existsByCityId(id)) {
            throw new CityInUseException("Cannot delete City " + id + " because it still has Fabs");
        }

        cityRepository.deleteById(id);
    }
}
