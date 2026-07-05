package com.pms.application.city;

import com.pms.application.city.dto.CityResponse;
import com.pms.application.city.dto.CreateCityRequest;
import com.pms.domain.city.City;
import com.pms.usecase.city.CityUseCase;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cities")
public class CityController {

    private final CityUseCase cityUseCase;

    public CityController(CityUseCase cityUseCase) {
        this.cityUseCase = cityUseCase;
    }

    @PostMapping
    public ResponseEntity<CityResponse> createCity(@Valid @RequestBody CreateCityRequest request) {
        City city = cityUseCase.createCity(request.getName());
        return ResponseEntity.ok(new CityResponse(city));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCity(@PathVariable("id") Long id) {
        cityUseCase.deleteCity(id);
        return ResponseEntity.noContent().build();
    }
}
