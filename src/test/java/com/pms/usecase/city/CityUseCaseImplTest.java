package com.pms.usecase.city;

import com.pms.domain.city.City;
import com.pms.usecase.fab.FabRepository;
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
 * Covers the create_city decision table in src/test/resources/create_city/decision-table.md,
 * and the delete_city decision table in src/test/resources/delete_city/decision-table.md.
 *
 * Rules 2, 4, 5, 7, 8 of create_city exercise the name-uniqueness/trim/length rules, which are
 * NOT YET implemented in CityUseCaseImpl/City — those tests are expected to fail
 * (red) until that logic is added. Rules 1 and 3 already pass today.
 *
 * Rule 6 (empty city table) is not representable at this mock level — it's
 * indistinguishable from Rule 1 here and belongs in a Repository-level
 * integration test instead (see decision-table.md).
 *
 * delete_city's business logic is already implemented in CityUseCaseImpl.deleteCity — those
 * tests are regression coverage and are expected to pass immediately (green).
 */
@ExtendWith(MockitoExtension.class)
class CityUseCaseImplTest {

    @Mock
    private CityRepository cityRepository;

    @Mock
    private FabRepository fabRepository;

    private CityUseCaseImpl cityUseCaseImpl;

    private CityUseCaseImpl newUseCase() {
        return new CityUseCaseImpl(cityRepository, fabRepository);
    }

    @Test
    @DisplayName("Rule 1: unique name succeeds")
    void createCity_withUniqueName_succeeds() {
        cityUseCaseImpl = newUseCase();
        when(cityRepository.existsByName("Taipei")).thenReturn(false);
        when(cityRepository.save(any(City.class)))
                .thenAnswer(invocation -> new City(1L, invocation.getArgument(0, City.class).getName()));

        City result = cityUseCaseImpl.createCity("Taipei");

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Taipei");
    }

    @Test
    @DisplayName("Rule 2: exact duplicate name fails")
    void createCity_withDuplicateName_throwsIllegalArgumentException() {
        cityUseCaseImpl = newUseCase();
        when(cityRepository.existsByName("Taipei")).thenReturn(true);

        assertThatThrownBy(() -> cityUseCaseImpl.createCity("Taipei"))
                .isInstanceOf(IllegalArgumentException.class);

        verify(cityRepository, never()).save(any());
    }

    @Test
    @DisplayName("Rule 3: blank name fails (existing domain validation, unaffected by the new rule)")
    void createCity_withBlankName_throwsIllegalArgumentException() {
        cityUseCaseImpl = newUseCase();

        assertThatThrownBy(() -> cityUseCaseImpl.createCity("   "))
                .isInstanceOf(IllegalArgumentException.class);

        verify(cityRepository, never()).existsByName(anyString());
        verify(cityRepository, never()).save(any());
    }

    @Test
    @DisplayName("Rule 4: different case is NOT a duplicate")
    void createCity_withDifferentCase_succeeds() {
        cityUseCaseImpl = newUseCase();
        when(cityRepository.existsByName("taipei")).thenReturn(false);
        when(cityRepository.save(any(City.class)))
                .thenAnswer(invocation -> new City(2L, invocation.getArgument(0, City.class).getName()));

        City result = cityUseCaseImpl.createCity("taipei");

        assertThat(result.getName()).isEqualTo("taipei");
    }

    @Test
    @DisplayName("Rule 5: name differing only by leading/trailing whitespace IS a duplicate (must trim before compare)")
    void createCity_withWhitespaceOnlyDifference_throwsIllegalArgumentException() {
        cityUseCaseImpl = newUseCase();
        when(cityRepository.existsByName("Taipei")).thenReturn(true);

        assertThatThrownBy(() -> cityUseCaseImpl.createCity(" Taipei "))
                .isInstanceOf(IllegalArgumentException.class);

        // must check the TRIMMED value, not the raw input
        verify(cityRepository).existsByName("Taipei");
        verify(cityRepository, never()).save(any());
    }

    @Test
    @DisplayName("Rule 7: name at exactly the 50-char limit succeeds")
    void createCity_withNameAtMaxLength_succeeds() {
        cityUseCaseImpl = newUseCase();
        String name = "A".repeat(50);
        when(cityRepository.existsByName(name)).thenReturn(false);
        when(cityRepository.save(any(City.class)))
                .thenAnswer(invocation -> new City(3L, invocation.getArgument(0, City.class).getName()));

        City result = cityUseCaseImpl.createCity(name);

        assertThat(result.getName()).hasSize(50);
    }

    @Test
    @DisplayName("Rule 8: name exceeding the 50-char limit fails")
    void createCity_withNameExceedingMaxLength_throwsIllegalArgumentException() {
        cityUseCaseImpl = newUseCase();
        String name = "A".repeat(51);

        assertThatThrownBy(() -> cityUseCaseImpl.createCity(name))
                .isInstanceOf(IllegalArgumentException.class);

        verify(cityRepository, never()).existsByName(anyString());
        verify(cityRepository, never()).save(any());
    }

    @Test
    @DisplayName("delete_city Rule 1: non-existent id fails")
    void deleteCity_withNonExistentId_throwsCityNotFoundException() {
        cityUseCaseImpl = newUseCase();
        when(cityRepository.getById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cityUseCaseImpl.deleteCity(999L))
                .isInstanceOf(CityNotFoundException.class);

        verify(fabRepository, never()).existsByCityId(any());
        verify(cityRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("delete_city Rule 2: existing city with no Fabs succeeds")
    void deleteCity_withNoFabs_succeeds() {
        cityUseCaseImpl = newUseCase();
        when(cityRepository.getById(1L)).thenReturn(Optional.of(new City(1L, "Taipei")));
        when(fabRepository.existsByCityId(1L)).thenReturn(false);

        cityUseCaseImpl.deleteCity(1L);

        verify(cityRepository).deleteById(1L);
    }

    @Test
    @DisplayName("delete_city Rule 3: existing city with Fabs fails")
    void deleteCity_withExistingFabs_throwsCityInUseException() {
        cityUseCaseImpl = newUseCase();
        when(cityRepository.getById(1L)).thenReturn(Optional.of(new City(1L, "Taipei")));
        when(fabRepository.existsByCityId(1L)).thenReturn(true);

        assertThatThrownBy(() -> cityUseCaseImpl.deleteCity(1L))
                .isInstanceOf(CityInUseException.class);

        verify(cityRepository, never()).deleteById(any());
    }
}
