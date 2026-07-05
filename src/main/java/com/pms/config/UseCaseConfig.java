package com.pms.config;

import com.pms.usecase.city.CityRepository;
import com.pms.usecase.city.CityUseCase;
import com.pms.usecase.city.CityUseCaseImpl;
import com.pms.usecase.fab.FabRepository;
import com.pms.usecase.fab.FabUseCase;
import com.pms.usecase.fab.FabUseCaseImpl;
import com.pms.usecase.parkinglot.ParkingLotRepository;
import com.pms.usecase.parkinglot.ParkingLotUseCase;
import com.pms.usecase.parkinglot.ParkingLotUseCaseImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * UseCase impls are plain POJOs (no @Service) so the Usecase layer stays
 * framework-free — Spring wiring for them happens here instead.
 */
@Configuration
public class UseCaseConfig {

    @Bean
    public CityUseCase cityUseCase(CityRepository cityRepository, FabRepository fabRepository) {
        return new CityUseCaseImpl(cityRepository, fabRepository);
    }

    @Bean
    public FabUseCase fabUseCase(FabRepository fabRepository, CityRepository cityRepository,
                                  ParkingLotRepository parkingLotRepository) {
        return new FabUseCaseImpl(fabRepository, cityRepository, parkingLotRepository);
    }

    @Bean
    public ParkingLotUseCase parkingLotUseCase(ParkingLotRepository parkingLotRepository,
                                                FabRepository fabRepository) {
        return new ParkingLotUseCaseImpl(parkingLotRepository, fabRepository);
    }
}
