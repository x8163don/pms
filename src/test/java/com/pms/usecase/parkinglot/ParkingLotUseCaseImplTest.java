package com.pms.usecase.parkinglot;

import com.pms.domain.parkinglot.ParkingLot;
import com.pms.domain.parkinglot.Zone;
import com.pms.usecase.fab.FabNotFoundException;
import com.pms.usecase.fab.FabRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Covers the create_parking_lot decision table in
 * src/test/resources/create_parking_lot/decision-table.md, and the delete_parking_lot decision
 * table in src/test/resources/delete_parking_lot/decision-table.md.
 *
 * Decision table 1 (fabId existence) is already implemented in ParkingLotUseCaseImpl today.
 *
 * Decision table 2 (name uniqueness/trim/length) directly reuses the create_city/create_fab
 * rules (global uniqueness, case-sensitive, trim-before-compare, 50-char limit) and is NOT YET
 * implemented in ParkingLotUseCaseImpl/ParkingLot — those tests are expected to fail (red) until
 * that logic is added.
 *
 * Decision table 3 (regularTotal/flexibleTotal) — negative rejection is existing domain
 * behaviour; the zero-boundary cases are new coverage, not new logic.
 *
 * Rule 6 of decision table 2 (empty ParkingLot table) is not representable at this mock level —
 * it's indistinguishable from Rule 1 here and belongs in a Repository-level integration test
 * instead (see decision-table.md).
 *
 * Per the confirmed aggregate rule, ParkingLot's regularTotal/flexibleTotal are informational
 * only and are never cross-validated against its Zones' totals, so addZone/removeZone are out
 * of scope for the create decision table.
 *
 * delete_parking_lot's business logic is already implemented in
 * ParkingLotUseCaseImpl.deleteParkingLot — those tests are regression coverage and are expected
 * to pass immediately (green). Unlike delete_city/delete_fab, the "has children" check reads
 * directly from the already-loaded aggregate's `zones` list rather than a separate Repository
 * existsBy query, since Zone has no independent Repository.
 */
@ExtendWith(MockitoExtension.class)
class ParkingLotUseCaseImplTest {

    @Mock
    private ParkingLotRepository parkingLotRepository;

    @Mock
    private FabRepository fabRepository;

    private ParkingLotUseCaseImpl newUseCase() {
        return new ParkingLotUseCaseImpl(parkingLotRepository, fabRepository);
    }

    private com.pms.domain.fab.Fab mockFab(Long id) {
        return new com.pms.domain.fab.Fab(id, 1L, "Xinyi");
    }

    private ParkingLot savedParkingLot(Long id, Long fabId, String name, int regularTotal, int flexibleTotal) {
        return new ParkingLot(id, fabId, name, regularTotal, regularTotal, flexibleTotal, flexibleTotal, List.of());
    }

    @Test
    @DisplayName("Decision table 1, Rule 1: existing fabId with unique name succeeds")
    void createParkingLot_withExistingFabAndUniqueName_succeeds() {
        ParkingLotUseCaseImpl useCase = newUseCase();
        when(fabRepository.getById(1L)).thenReturn(Optional.of(mockFab(1L)));
        when(parkingLotRepository.existsByName("B1")).thenReturn(false);
        when(parkingLotRepository.save(any(ParkingLot.class)))
                .thenAnswer(invocation -> savedParkingLot(100L,
                        invocation.getArgument(0, ParkingLot.class).getFabId(),
                        invocation.getArgument(0, ParkingLot.class).getName(), 10, 5));

        ParkingLot result = useCase.createParkingLot(1L, "B1", 10, 5);

        assertThat(result.getId()).isEqualTo(100L);
        assertThat(result.getFabId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("B1");
        assertThat(result.getZones()).isEmpty();
    }

    @Test
    @DisplayName("Decision table 1, Rule 2: non-existent fabId fails before name/quantity validation")
    void createParkingLot_withNonExistentFab_throwsFabNotFoundException() {
        ParkingLotUseCaseImpl useCase = newUseCase();
        when(fabRepository.getById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.createParkingLot(999L, "B1", 10, 5))
                .isInstanceOf(FabNotFoundException.class);

        verify(parkingLotRepository, never()).existsByName(anyString());
        verify(parkingLotRepository, never()).save(any());
    }

    @Test
    @DisplayName("Decision table 2, Rule 2: exact duplicate name fails")
    void createParkingLot_withDuplicateName_throwsIllegalArgumentException() {
        ParkingLotUseCaseImpl useCase = newUseCase();
        when(fabRepository.getById(1L)).thenReturn(Optional.of(mockFab(1L)));
        when(parkingLotRepository.existsByName("B1")).thenReturn(true);

        assertThatThrownBy(() -> useCase.createParkingLot(1L, "B1", 10, 5))
                .isInstanceOf(IllegalArgumentException.class);

        verify(parkingLotRepository, never()).save(any());
    }

    @Test
    @DisplayName("Decision table 2, Rule 3: blank name fails (existing domain validation, unaffected by the new rule)")
    void createParkingLot_withBlankName_throwsIllegalArgumentException() {
        ParkingLotUseCaseImpl useCase = newUseCase();
        when(fabRepository.getById(1L)).thenReturn(Optional.of(mockFab(1L)));

        assertThatThrownBy(() -> useCase.createParkingLot(1L, "   ", 10, 5))
                .isInstanceOf(IllegalArgumentException.class);

        verify(parkingLotRepository, never()).existsByName(anyString());
        verify(parkingLotRepository, never()).save(any());
    }

    @Test
    @DisplayName("Decision table 2, Rule 4: different case is NOT a duplicate")
    void createParkingLot_withDifferentCase_succeeds() {
        ParkingLotUseCaseImpl useCase = newUseCase();
        when(fabRepository.getById(1L)).thenReturn(Optional.of(mockFab(1L)));
        when(parkingLotRepository.existsByName("b1")).thenReturn(false);
        when(parkingLotRepository.save(any(ParkingLot.class)))
                .thenAnswer(invocation -> savedParkingLot(101L,
                        invocation.getArgument(0, ParkingLot.class).getFabId(),
                        invocation.getArgument(0, ParkingLot.class).getName(), 10, 5));

        ParkingLot result = useCase.createParkingLot(1L, "b1", 10, 5);

        assertThat(result.getName()).isEqualTo("b1");
    }

    @Test
    @DisplayName("Decision table 2, Rule 5: name differing only by leading/trailing whitespace IS a duplicate (must trim before compare)")
    void createParkingLot_withWhitespaceOnlyDifference_throwsIllegalArgumentException() {
        ParkingLotUseCaseImpl useCase = newUseCase();
        when(fabRepository.getById(1L)).thenReturn(Optional.of(mockFab(1L)));
        when(parkingLotRepository.existsByName("B1")).thenReturn(true);

        assertThatThrownBy(() -> useCase.createParkingLot(1L, " B1 ", 10, 5))
                .isInstanceOf(IllegalArgumentException.class);

        // must check the TRIMMED value, not the raw input
        verify(parkingLotRepository).existsByName("B1");
        verify(parkingLotRepository, never()).save(any());
    }

    @Test
    @DisplayName("Decision table 2, Rule 7: name at exactly the 50-char limit succeeds")
    void createParkingLot_withNameAtMaxLength_succeeds() {
        ParkingLotUseCaseImpl useCase = newUseCase();
        String name = "A".repeat(50);
        when(fabRepository.getById(1L)).thenReturn(Optional.of(mockFab(1L)));
        when(parkingLotRepository.existsByName(name)).thenReturn(false);
        when(parkingLotRepository.save(any(ParkingLot.class)))
                .thenAnswer(invocation -> savedParkingLot(102L,
                        invocation.getArgument(0, ParkingLot.class).getFabId(),
                        invocation.getArgument(0, ParkingLot.class).getName(), 10, 5));

        ParkingLot result = useCase.createParkingLot(1L, name, 10, 5);

        assertThat(result.getName()).hasSize(50);
    }

    @Test
    @DisplayName("Decision table 2, Rule 8: name exceeding the 50-char limit fails")
    void createParkingLot_withNameExceedingMaxLength_throwsIllegalArgumentException() {
        ParkingLotUseCaseImpl useCase = newUseCase();
        String name = "A".repeat(51);
        when(fabRepository.getById(1L)).thenReturn(Optional.of(mockFab(1L)));

        assertThatThrownBy(() -> useCase.createParkingLot(1L, name, 10, 5))
                .isInstanceOf(IllegalArgumentException.class);

        verify(parkingLotRepository, never()).existsByName(anyString());
        verify(parkingLotRepository, never()).save(any());
    }

    @Test
    @DisplayName("Decision table 3, Rule 2: negative regularTotal fails")
    void createParkingLot_withNegativeRegularTotal_throwsIllegalArgumentException() {
        ParkingLotUseCaseImpl useCase = newUseCase();
        when(fabRepository.getById(1L)).thenReturn(Optional.of(mockFab(1L)));

        assertThatThrownBy(() -> useCase.createParkingLot(1L, "B1", -1, 5))
                .isInstanceOf(IllegalArgumentException.class);

        verify(parkingLotRepository, never()).save(any());
    }

    @Test
    @DisplayName("Decision table 3, Rule 3: regularTotal of 0 with positive flexibleTotal succeeds")
    void createParkingLot_withZeroRegularTotal_succeeds() {
        ParkingLotUseCaseImpl useCase = newUseCase();
        when(fabRepository.getById(1L)).thenReturn(Optional.of(mockFab(1L)));
        when(parkingLotRepository.existsByName("B1")).thenReturn(false);
        when(parkingLotRepository.save(any(ParkingLot.class)))
                .thenAnswer(invocation -> savedParkingLot(103L,
                        invocation.getArgument(0, ParkingLot.class).getFabId(),
                        invocation.getArgument(0, ParkingLot.class).getName(), 0, 10));

        ParkingLot result = useCase.createParkingLot(1L, "B1", 0, 10);

        assertThat(result.getRegularTotal()).isZero();
        assertThat(result.getFlexibleTotal()).isEqualTo(10);
    }

    @Test
    @DisplayName("Decision table 3, Rule 4: both regularTotal and flexibleTotal at 0 succeeds (informational-only fields)")
    void createParkingLot_withBothTotalsZero_succeeds() {
        ParkingLotUseCaseImpl useCase = newUseCase();
        when(fabRepository.getById(1L)).thenReturn(Optional.of(mockFab(1L)));
        when(parkingLotRepository.existsByName("B1")).thenReturn(false);
        when(parkingLotRepository.save(any(ParkingLot.class)))
                .thenAnswer(invocation -> savedParkingLot(104L,
                        invocation.getArgument(0, ParkingLot.class).getFabId(),
                        invocation.getArgument(0, ParkingLot.class).getName(), 0, 0));

        ParkingLot result = useCase.createParkingLot(1L, "B1", 0, 0);

        assertThat(result.getRegularTotal()).isZero();
        assertThat(result.getFlexibleTotal()).isZero();
        assertThat(result.getZones()).isEmpty();
    }

    @Test
    @DisplayName("delete_parking_lot Rule 1: non-existent id fails")
    void deleteParkingLot_withNonExistentId_throwsParkingLotNotFoundException() {
        ParkingLotUseCaseImpl useCase = newUseCase();
        when(parkingLotRepository.getById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.deleteParkingLot(999L))
                .isInstanceOf(ParkingLotNotFoundException.class);

        verify(parkingLotRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("delete_parking_lot Rule 2: existing ParkingLot with no Zones succeeds")
    void deleteParkingLot_withNoZones_succeeds() {
        ParkingLotUseCaseImpl useCase = newUseCase();
        when(parkingLotRepository.getById(1L)).thenReturn(Optional.of(savedParkingLot(1L, 1L, "B1", 10, 5)));

        useCase.deleteParkingLot(1L);

        verify(parkingLotRepository).deleteById(1L);
    }

    @Test
    @DisplayName("delete_parking_lot Rule 3: existing ParkingLot with Zones fails")
    void deleteParkingLot_withExistingZones_throwsParkingLotInUseException() {
        ParkingLotUseCaseImpl useCase = newUseCase();
        Zone zone = new Zone("1F", "A", 10, 5, 0, 0);
        ParkingLot lotWithZone = new ParkingLot(1L, 1L, "B1", 10, 10, 5, 5, List.of(zone));
        when(parkingLotRepository.getById(1L)).thenReturn(Optional.of(lotWithZone));

        assertThatThrownBy(() -> useCase.deleteParkingLot(1L))
                .isInstanceOf(ParkingLotInUseException.class);

        verify(parkingLotRepository, never()).deleteById(any());
    }
}
