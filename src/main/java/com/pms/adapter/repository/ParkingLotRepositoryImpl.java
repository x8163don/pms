package com.pms.adapter.repository;

import com.pms.adapter.repository.dto.ParkingLotDto;
import com.pms.adapter.repository.mapper.ParkingLotMapper;
import com.pms.domain.parkinglot.ParkingLot;
import com.pms.usecase.parkinglot.ParkingLotRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ParkingLotRepositoryImpl implements ParkingLotRepository {

    private final ParkingLotJpaRepository jpaRepository;

    public ParkingLotRepositoryImpl(ParkingLotJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<ParkingLot> getById(Long id) {
        return jpaRepository.findById(id).map(ParkingLotMapper::toDomain);
    }

    @Override
    public ParkingLot save(ParkingLot parkingLot) {
        ParkingLotDto dto = ParkingLotMapper.toDto(parkingLot);
        ParkingLotDto saved = jpaRepository.save(dto);
        return ParkingLotMapper.toDomain(saved);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existsByFabId(Long fabId) {
        return jpaRepository.existsByFabId(fabId);
    }

    @Override
    public boolean existsByName(String name) {
        return jpaRepository.existsByName(name);
    }
}
