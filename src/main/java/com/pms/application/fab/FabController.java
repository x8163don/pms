package com.pms.application.fab;

import com.pms.application.fab.dto.CreateFabRequest;
import com.pms.application.fab.dto.FabResponse;
import com.pms.domain.fab.Fab;
import com.pms.usecase.fab.FabUseCase;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/fabs")
public class FabController {

    private final FabUseCase fabUseCase;

    public FabController(FabUseCase fabUseCase) {
        this.fabUseCase = fabUseCase;
    }

    @PostMapping
    public ResponseEntity<FabResponse> createFab(@Valid @RequestBody CreateFabRequest request) {
        Fab fab = fabUseCase.createFab(request.getCityId(), request.getName());
        return ResponseEntity.ok(new FabResponse(fab));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFab(@PathVariable("id") Long id) {
        fabUseCase.deleteFab(id);
        return ResponseEntity.noContent().build();
    }
}
