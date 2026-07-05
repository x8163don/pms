package com.pms.usecase.fab;

import com.pms.domain.fab.Fab;
import com.pms.usecase.city.CityNotFoundException;
import com.pms.usecase.city.CityRepository;
import com.pms.usecase.parkinglot.ParkingLotRepository;
import org.springframework.transaction.annotation.Transactional;

public class FabUseCaseImpl implements FabUseCase {

    private final FabRepository fabRepository;
    private final CityRepository cityRepository;
    private final ParkingLotRepository parkingLotRepository;

    public FabUseCaseImpl(FabRepository fabRepository, CityRepository cityRepository,
                           ParkingLotRepository parkingLotRepository) {
        this.fabRepository = fabRepository;
        this.cityRepository = cityRepository;
        this.parkingLotRepository = parkingLotRepository;
    }

    @Override
    @Transactional
    public Fab createFab(Long cityId, String name) {
        cityRepository.getById(cityId)
                .orElseThrow(() -> new CityNotFoundException("City not found for ID: " + cityId));

        Fab fab = new Fab(cityId, name.trim());

        if (fabRepository.existsByName(fab.getName())) {
            throw new IllegalArgumentException("Fab name already exists: " + fab.getName());
        }

        return fabRepository.save(fab);
    }

    @Override
    @Transactional
    public void deleteFab(Long id) {
        fabRepository.getById(id)
                .orElseThrow(() -> new FabNotFoundException("Fab not found for ID: " + id));

        if (parkingLotRepository.existsByFabId(id)) {
            throw new FabInUseException("Cannot delete Fab " + id + " because it still has ParkingLots");
        }

        fabRepository.deleteById(id);
    }
}
