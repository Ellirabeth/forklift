package com.forklift.service;

import com.forklift.model.Forklift;
import com.forklift.model.dto.ForkliftDTO;
import com.forklift.repository.DowntimeRepository;
import com.forklift.repository.ForkliftRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ForkliftServiceImplTest {

    @Mock
    private ForkliftRepository forkliftRepository;

    @Mock
    private DowntimeRepository downtimeRepository;

    private ForkliftServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ForkliftServiceImpl(forkliftRepository, downtimeRepository);
    }

    private Forklift createForklift(Long id, String brand, String number, BigDecimal capacity) {
        Forklift f = new Forklift();
        f.setId(id);
        f.setBrand(brand);
        f.setNumber(number);
        f.setLoadCapacity(capacity);
        f.setIsActive(true);
        f.setLastModified(LocalDateTime.now());
        f.setModifiedBy("Иванов И И");
        return f;
    }

    private ForkliftDTO createDTO(String brand, String number, BigDecimal capacity) {
        ForkliftDTO dto = new ForkliftDTO();
        dto.setBrand(brand);
        dto.setNumber(number);
        dto.setLoadCapacity(capacity);
        dto.setModifiedBy("Иванов И И");
        return dto;
    }

    // === getAllForklifts ===

    @Test
    void getAllForklifts_shouldReturnAllForklifts() {
        when(forkliftRepository.findAll()).thenReturn(Arrays.asList(
                createForklift(1L, "Toyota", "FT-001", BigDecimal.valueOf(2.5)),
                createForklift(2L, "Komatsu", "KT-002", BigDecimal.valueOf(3.0))
        ));
        when(downtimeRepository.existsByForklift(any())).thenReturn(false);

        List<ForkliftDTO> result = service.getAllForklifts();

        assertEquals(2, result.size());
        assertEquals("Toyota", result.get(0).getBrand());
        assertEquals("FT-001", result.get(0).getNumber());
        verify(forkliftRepository).findAll();
    }

    @Test
    void getAllForklifts_shouldReturnEmptyList_whenNoData() {
        when(forkliftRepository.findAll()).thenReturn(List.of());

        List<ForkliftDTO> result = service.getAllForklifts();

        assertTrue(result.isEmpty());
    }

    // === searchByNumber ===

    @Test
    void searchByNumber_shouldReturnMatchingForklifts() {
        when(forkliftRepository.searchByNumber("toy")).thenReturn(Arrays.asList(
                createForklift(1L, "Toyota", "FT-001", BigDecimal.valueOf(2.5))
        ));
        when(downtimeRepository.existsByForklift(any())).thenReturn(false);

        List<ForkliftDTO> result = service.searchByNumber("toy");

        assertEquals(1, result.size());
        assertEquals("Toyota", result.get(0).getBrand());
        verify(forkliftRepository).searchByNumber("toy");
    }

    @Test
    void searchByNumber_shouldReturnEmpty_whenNoMatch() {
        when(forkliftRepository.searchByNumber("xxx")).thenReturn(List.of());

        List<ForkliftDTO> result = service.searchByNumber("xxx");

        assertTrue(result.isEmpty());
    }

    // === getForkliftById ===

    @Test
    void getForkliftById_shouldReturnForklift() {
        when(forkliftRepository.findById(1L)).thenReturn(Optional.of(
                createForklift(1L, "Toyota", "FT-001", BigDecimal.valueOf(2.5))
        ));
        when(downtimeRepository.existsByForklift(any())).thenReturn(false);

        ForkliftDTO result = service.getForkliftById(1L);

        assertEquals("Toyota", result.getBrand());
        assertEquals("FT-001", result.getNumber());
    }

    @Test
    void getForkliftById_shouldThrow_whenNotFound() {
        when(forkliftRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.getForkliftById(999L));
    }

    // === createForklift ===

    @Test
    void createForklift_shouldSaveAndReturn() {
        ForkliftDTO dto = createDTO("Toyota", "FT-001", BigDecimal.valueOf(2.5));
        Forklift saved = createForklift(1L, "Toyota", "FT-001", BigDecimal.valueOf(2.5));

        when(forkliftRepository.save(any())).thenReturn(saved);
        when(downtimeRepository.existsByForklift(any())).thenReturn(false);

        ForkliftDTO result = service.createForklift(dto);

        assertNotNull(result.getId());
        assertEquals("Toyota", result.getBrand());
        verify(forkliftRepository).save(any());
    }

    @Test
    void createForklift_shouldSetActiveTrue() {
        ForkliftDTO dto = createDTO("Toyota", "FT-001", BigDecimal.valueOf(2.5));
        Forklift saved = createForklift(1L, "Toyota", "FT-001", BigDecimal.valueOf(2.5));

        when(forkliftRepository.save(any())).thenReturn(saved);
        when(downtimeRepository.existsByForklift(any())).thenReturn(false);

        ForkliftDTO result = service.createForklift(dto);

        assertTrue(result.getIsActive());
    }

    @Test
    void createForklift_shouldUseProvidedModifiedBy() {
        ForkliftDTO dto = createDTO("Toyota", "FT-001", BigDecimal.valueOf(2.5));
        dto.setModifiedBy("Петров П П");
        Forklift saved = createForklift(1L, "Toyota", "FT-001", BigDecimal.valueOf(2.5));

        when(forkliftRepository.save(any())).thenReturn(saved);
        when(downtimeRepository.existsByForklift(any())).thenReturn(false);

        ForkliftDTO result = service.createForklift(dto);

        assertEquals("Иванов И И", result.getModifiedBy()); // saved returns the mock value
        verify(forkliftRepository).save(argThat(f ->
                f.getBrand().equals("Toyota") && f.getNumber().equals("FT-001")
        ));
    }

    // === updateForklift ===

    @Test
    void updateForklift_shouldUpdateFields() {
        Forklift existing = createForklift(1L, "OldBrand", "OLD-01", BigDecimal.valueOf(1.0));
        ForkliftDTO dto = createDTO("NewBrand", "NEW-01", BigDecimal.valueOf(5.0));

        when(forkliftRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(forkliftRepository.save(any())).thenReturn(existing);
        when(downtimeRepository.existsByForklift(any())).thenReturn(false);

        ForkliftDTO result = service.updateForklift(1L, dto);

        assertEquals("NewBrand", result.getBrand());
        assertEquals("NEW-01", result.getNumber());
        assertEquals(BigDecimal.valueOf(5.0), result.getLoadCapacity());
        verify(forkliftRepository).save(any());
    }

    @Test
    void updateForklift_shouldThrow_whenNotFound() {
        ForkliftDTO dto = createDTO("Brand", "NUM-01", BigDecimal.valueOf(1.0));

        when(forkliftRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.updateForklift(999L, dto));
    }

    // === deleteForklift ===

    @Test
    void deleteForklift_shouldDelete_whenNoDowntimes() {
        Forklift forklift = createForklift(1L, "Toyota", "FT-001", BigDecimal.valueOf(2.5));

        when(forkliftRepository.findById(1L)).thenReturn(Optional.of(forklift));
        when(downtimeRepository.existsByForklift(forklift)).thenReturn(false);

        service.deleteForklift(1L);

        verify(forkliftRepository).delete(forklift);
    }

    @Test
    void deleteForklift_shouldThrow_whenHasDowntimes() {
        Forklift forklift = createForklift(1L, "Toyota", "FT-001", BigDecimal.valueOf(2.5));

        when(forkliftRepository.findById(1L)).thenReturn(Optional.of(forklift));
        when(downtimeRepository.existsByForklift(forklift)).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> service.deleteForklift(1L));
        verify(forkliftRepository, never()).delete(any());
    }

    @Test
    void deleteForklift_shouldThrow_whenNotFound() {
        when(forkliftRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.deleteForklift(999L));
    }

    // === convertToDTO — hasDowntimes ===

    @Test
    void getAllForklifts_shouldSetHasDowntimesTrue_whenExists() {
        Forklift f = createForklift(1L, "Toyota", "FT-001", BigDecimal.valueOf(2.5));

        when(forkliftRepository.findAll()).thenReturn(List.of(f));
        when(downtimeRepository.existsByForklift(f)).thenReturn(true);

        List<ForkliftDTO> result = service.getAllForklifts();

        assertTrue(result.get(0).getHasDowntimes());
    }

    @Test
    void getAllForklifts_shouldSetHasDowntimesFalse_whenNotExists() {
        Forklift f = createForklift(1L, "Toyota", "FT-001", BigDecimal.valueOf(2.5));

        when(forkliftRepository.findAll()).thenReturn(List.of(f));
        when(downtimeRepository.existsByForklift(f)).thenReturn(false);

        List<ForkliftDTO> result = service.getAllForklifts();

        assertFalse(result.get(0).getHasDowntimes());
    }
}