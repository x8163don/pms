package com.pms.adapter.repository;

import com.pms.adapter.repository.dto.CityDto;
import com.pms.adapter.repository.mapper.CityMapper;
import com.pms.domain.city.City;
import com.pms.usecase.city.CityRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class CityRepositoryImpl implements CityRepository {

    private final CityJpaRepository jpaRepository;

    public CityRepositoryImpl(CityJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<City> getById(Long id) {
        return jpaRepository.findById(id).map(CityMapper::toDomain);
    }

    @Override
    public City save(City city) {
        CityDto dto = CityMapper.toDto(city);
        CityDto saved = jpaRepository.save(dto);
        return CityMapper.toDomain(saved);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existsByName(String name) {
        return jpaRepository.existsByName(name);
    }
}
