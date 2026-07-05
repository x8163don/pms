package com.pms.adapter.repository;

import com.pms.adapter.repository.dto.ParkingLotDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ParkingLotJpaRepository extends JpaRepository<ParkingLotDto, Long> {
    boolean existsByFabId(Long fabId);

    boolean existsByName(String name);
}
