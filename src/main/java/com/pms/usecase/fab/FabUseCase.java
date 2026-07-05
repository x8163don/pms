package com.pms.usecase.fab;

import com.pms.domain.fab.Fab;

public interface FabUseCase {
    Fab createFab(Long cityId, String name);

    void deleteFab(Long id);
}
