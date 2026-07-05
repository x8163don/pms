package com.pms.usecase.fab;

import com.pms.domain.fab.Fab;

import java.util.Optional;

public interface FabRepository {
    Optional<Fab> getById(Long id);

    Fab save(Fab fab);

    void deleteById(Long id);

    boolean existsByCityId(Long cityId);

    boolean existsByName(String name);
}
