package com.pms.adapter.repository;

import com.pms.adapter.repository.dto.FabDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FabJpaRepository extends JpaRepository<FabDto, Long> {
    boolean existsByCityId(Long cityId);

    boolean existsByName(String name);
}
