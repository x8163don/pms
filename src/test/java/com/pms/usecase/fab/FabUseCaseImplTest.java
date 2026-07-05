package com.pms.usecase.fab;

import com.pms.domain.fab.Fab;
import com.pms.usecase.city.CityNotFoundException;
import com.pms.usecase.city.CityRepository;
import com.pms.usecase.parkinglot.ParkingLotRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Covers the create_fab decision table in src/test/resources/create_fab/decision-table.md,
 * and the delete_fab decision table in src/test/resources/delete_fab/decision-table.md.
 *
 * Decision table 1 (cityId existence) is already implemented in FabUseCaseImpl today.
 *
 * Decision table 2 (name uniqueness/trim/length) directly reuses the create_city rules
 * (global uniqueness, case-sensitive, trim-before-compare, 50-char limit) and is NOT YET
 * implemented in FabUseCaseImpl/Fab — those tests are expected to fail (red) until that
 * logic is added.
 *
 * Rule 6 (empty Fab table) is not representable at this mock level — it's indistinguishable
 * from Rule 1 here and belongs in a Repository-level integration test instead (see
 * decision-table.md).
 *
 * delete_fab's business logic is already implemented in FabUseCaseImpl.deleteFab — those tests
 * are regression coverage and are expected to pass immediately (green).
 */
@ExtendWith(MockitoExtension.class)
class FabUseCaseImplTest {

    @Mock
    private FabRepository fabRepository;

    @Mock
    private CityRepository cityRepository;

    @Mock
    private ParkingLotRepository parkingLotRepository;

    private FabUseCaseImpl newUseCase() {
        return new FabUseCaseImpl(fabRepository, cityRepository, parkingLotRepository);
    }

    @Test
    @DisplayName("Decision table 1, Rule 1: existing cityId with unique name succeeds")
    void createFab_withExistingCityAndUniqueName_succeeds() {
        FabUseCaseImpl fabUseCaseImpl = newUseCase();
        when(cityRepository.getById(1L)).thenReturn(Optional.of(mockCity(1L)));
        when(fabRepository.existsByName("1F-A")).thenReturn(false);
        when(fabRepository.save(any(Fab.class)))
                .thenAnswer(invocation -> new Fab(10L, invocation.getArgument(0, Fab.class).getCityId(),
                        invocation.getArgument(0, Fab.class).getName()));

        Fab result = fabUseCaseImpl.createFab(1L, "1F-A");

        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getCityId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("1F-A");
    }

    @Test
    @DisplayName("Decision table 1, Rule 2: non-existent cityId fails before name validation")
    void createFab_withNonExistentCity_throwsCityNotFoundException() {
        FabUseCaseImpl fabUseCaseImpl = newUseCase();
        when(cityRepository.getById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> fabUseCaseImpl.createFab(999L, "1F-A"))
                .isInstanceOf(CityNotFoundException.class);

        verify(fabRepository, never()).existsByName(anyString());
        verify(fabRepository, never()).save(any());
    }

    @Test
    @DisplayName("Decision table 2, Rule 2: exact duplicate name fails")
    void createFab_withDuplicateName_throwsIllegalArgumentException() {
        FabUseCaseImpl fabUseCaseImpl = newUseCase();
        when(cityRepository.getById(1L)).thenReturn(Optional.of(mockCity(1L)));
        when(fabRepository.existsByName("1F-A")).thenReturn(true);

        assertThatThrownBy(() -> fabUseCaseImpl.createFab(1L, "1F-A"))
                .isInstanceOf(IllegalArgumentException.class);

        verify(fabRepository, never()).save(any());
    }

    @Test
    @DisplayName("Decision table 2, Rule 3: blank name fails (existing domain validation, unaffected by the new rule)")
    void createFab_withBlankName_throwsIllegalArgumentException() {
        FabUseCaseImpl fabUseCaseImpl = newUseCase();
        when(cityRepository.getById(1L)).thenReturn(Optional.of(mockCity(1L)));

        assertThatThrownBy(() -> fabUseCaseImpl.createFab(1L, "   "))
                .isInstanceOf(IllegalArgumentException.class);

        verify(fabRepository, never()).existsByName(anyString());
        verify(fabRepository, never()).save(any());
    }

    @Test
    @DisplayName("Decision table 2, Rule 4: different case is NOT a duplicate")
    void createFab_withDifferentCase_succeeds() {
        FabUseCaseImpl fabUseCaseImpl = newUseCase();
        when(cityRepository.getById(1L)).thenReturn(Optional.of(mockCity(1L)));
        when(fabRepository.existsByName("1f-a")).thenReturn(false);
        when(fabRepository.save(any(Fab.class)))
                .thenAnswer(invocation -> new Fab(11L, invocation.getArgument(0, Fab.class).getCityId(),
                        invocation.getArgument(0, Fab.class).getName()));

        Fab result = fabUseCaseImpl.createFab(1L, "1f-a");

        assertThat(result.getName()).isEqualTo("1f-a");
    }

    @Test
    @DisplayName("Decision table 2, Rule 5: name differing only by leading/trailing whitespace IS a duplicate (must trim before compare)")
    void createFab_withWhitespaceOnlyDifference_throwsIllegalArgumentException() {
        FabUseCaseImpl fabUseCaseImpl = newUseCase();
        when(cityRepository.getById(1L)).thenReturn(Optional.of(mockCity(1L)));
        when(fabRepository.existsByName("1F-A")).thenReturn(true);

        assertThatThrownBy(() -> fabUseCaseImpl.createFab(1L, " 1F-A "))
                .isInstanceOf(IllegalArgumentException.class);

        // must check the TRIMMED value, not the raw input
        verify(fabRepository).existsByName("1F-A");
        verify(fabRepository, never()).save(any());
    }

    @Test
    @DisplayName("Decision table 2, Rule 7: name at exactly the 50-char limit succeeds")
    void createFab_withNameAtMaxLength_succeeds() {
        FabUseCaseImpl fabUseCaseImpl = newUseCase();
        String name = "A".repeat(50);
        when(cityRepository.getById(1L)).thenReturn(Optional.of(mockCity(1L)));
        when(fabRepository.existsByName(name)).thenReturn(false);
        when(fabRepository.save(any(Fab.class)))
                .thenAnswer(invocation -> new Fab(12L, invocation.getArgument(0, Fab.class).getCityId(),
                        invocation.getArgument(0, Fab.class).getName()));

        Fab result = fabUseCaseImpl.createFab(1L, name);

        assertThat(result.getName()).hasSize(50);
    }

    @Test
    @DisplayName("Decision table 2, Rule 8: name exceeding the 50-char limit fails")
    void createFab_withNameExceedingMaxLength_throwsIllegalArgumentException() {
        FabUseCaseImpl fabUseCaseImpl = newUseCase();
        String name = "A".repeat(51);
        when(cityRepository.getById(1L)).thenReturn(Optional.of(mockCity(1L)));

        assertThatThrownBy(() -> fabUseCaseImpl.createFab(1L, name))
                .isInstanceOf(IllegalArgumentException.class);

        verify(fabRepository, never()).existsByName(anyString());
        verify(fabRepository, never()).save(any());
    }

    @Test
    @DisplayName("delete_fab Rule 1: non-existent id fails")
    void deleteFab_withNonExistentId_throwsFabNotFoundException() {
        FabUseCaseImpl fabUseCaseImpl = newUseCase();
        when(fabRepository.getById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> fabUseCaseImpl.deleteFab(999L))
                .isInstanceOf(FabNotFoundException.class);

        verify(parkingLotRepository, never()).existsByFabId(any());
        verify(fabRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("delete_fab Rule 2: existing fab with no ParkingLots succeeds")
    void deleteFab_withNoParkingLots_succeeds() {
        FabUseCaseImpl fabUseCaseImpl = newUseCase();
        when(fabRepository.getById(1L)).thenReturn(Optional.of(mockFab(1L)));
        when(parkingLotRepository.existsByFabId(1L)).thenReturn(false);

        fabUseCaseImpl.deleteFab(1L);

        verify(fabRepository).deleteById(1L);
    }

    @Test
    @DisplayName("delete_fab Rule 3: existing fab with ParkingLots fails")
    void deleteFab_withExistingParkingLots_throwsFabInUseException() {
        FabUseCaseImpl fabUseCaseImpl = newUseCase();
        when(fabRepository.getById(1L)).thenReturn(Optional.of(mockFab(1L)));
        when(parkingLotRepository.existsByFabId(1L)).thenReturn(true);

        assertThatThrownBy(() -> fabUseCaseImpl.deleteFab(1L))
                .isInstanceOf(FabInUseException.class);

        verify(fabRepository, never()).deleteById(any());
    }

    private com.pms.domain.city.City mockCity(Long id) {
        return new com.pms.domain.city.City(id, "Taipei");
    }

    private Fab mockFab(Long id) {
        return new Fab(id, 1L, "1F-A");
    }
}
