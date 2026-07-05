package com.pms.adapter.repository;

import com.pms.adapter.repository.dto.CityDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CityJpaRepository extends JpaRepository<CityDto, Long> {
    boolean existsByName(String name);
}
