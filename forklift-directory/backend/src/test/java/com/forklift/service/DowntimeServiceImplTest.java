package com.forklift.service;

import com.forklift.model.Downtime;
import com.forklift.model.Forklift;
import com.forklift.model.dto.DowntimeDTO;
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
class DowntimeServiceImplTest {

    @Mock
    private DowntimeRepository downtimeRepository;

    @Mock
    private ForkliftRepository forkliftRepository;

    private DowntimeServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new DowntimeServiceImpl(downtimeRepository, forkliftRepository);
    }

    private Forklift createForklift(Long id) {
        Forklift f = new Forklift();
        f.setId(id);
        f.setBrand("Toyota");
        f.setNumber("FT-001");
        f.setLoadCapacity(BigDecimal.valueOf(2.5));
        f.setIsActive(true);
        return f;
    }

    private Downtime createDowntime(Long id, Long forkliftId, LocalDateTime start, LocalDateTime end, String desc) {
        Downtime d = new Downtime();
        d.setId(id);
        d.setForklift(createForklift(forkliftId));
        d.setStartTime(start);
        d.setEndTime(end);
        d.setDescription(desc);
        return d;
    }

    private DowntimeDTO createDTO(Long forkliftId, LocalDateTime start, LocalDateTime end, String desc) {
        DowntimeDTO dto = new DowntimeDTO();
        dto.setForkliftId(forkliftId);
        dto.setStartTime(start);
        dto.setEndTime(end);
        dto.setDescription(desc);
        return dto;
    }

    // === getDowntimesByForklift ===

    @Test
    void getDowntimesByForklift_shouldReturnDowntimes() {
        LocalDateTime start = LocalDateTime.of(2026, 5, 5, 10, 0);
        LocalDateTime end = LocalDateTime.of(2026, 5, 5, 12, 30);

        when(downtimeRepository.findByForkliftIdOrderByStartTimeDesc(1L))
                .thenReturn(Arrays.asList(
                        createDowntime(1L, 1L, start, end, "Неисправность")
                ));

        List<DowntimeDTO> result = service.getDowntimesByForklift(1L);

        assertEquals(1, result.size());
        assertEquals("Неисправность", result.get(0).getDescription());
        verify(downtimeRepository).findByForkliftIdOrderByStartTimeDesc(1L);
    }

    @Test
    void getDowntimesByForklift_shouldReturnEmpty_whenNoDowntimes() {
        when(downtimeRepository.findByForkliftIdOrderByStartTimeDesc(1L))
                .thenReturn(List.of());

        List<DowntimeDTO> result = service.getDowntimesByForklift(1L);

        assertTrue(result.isEmpty());
    }

    @Test
    void getDowntimesByForklift_shouldSortDescByStartTime() {
        LocalDateTime t1 = LocalDateTime.of(2026, 5, 5, 10, 0);
        LocalDateTime t2 = LocalDateTime.of(2026, 5, 5, 8, 0);

        when(downtimeRepository.findByForkliftIdOrderByStartTimeDesc(1L))
                .thenReturn(Arrays.asList(
                        createDowntime(2L, 1L, t1, null, "Позже"),
                        createDowntime(1L, 1L, t2, null, "Раньше")
                ));

        List<DowntimeDTO> result = service.getDowntimesByForklift(1L);

        assertEquals(2, result.size());
        assertEquals("Позже", result.get(0).getDescription());
    }

    // === createDowntime ===

    @Test
    void createDowntime_shouldSaveAndReturn() {
        Forklift forklift = createForklift(1L);
        LocalDateTime start = LocalDateTime.of(2026, 5, 5, 10, 0);
        DowntimeDTO dto = createDTO(1L, start, null, "Поломка");
        Downtime saved = createDowntime(1L, 1L, start, null, "Поломка");

        when(forkliftRepository.findById(1L)).thenReturn(Optional.of(forklift));
        when(downtimeRepository.save(any())).thenReturn(saved);

        DowntimeDTO result = service.createDowntime(dto);

        assertNotNull(result.getId());
        assertEquals("Поломка", result.getDescription());
        verify(downtimeRepository).save(any());
    }

    @Test
    void createDowntime_shouldThrow_whenForkliftNotFound() {
        DowntimeDTO dto = createDTO(999L, LocalDateTime.now(), null, "Тест");

        when(forkliftRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.createDowntime(dto));
    }

    // === updateDowntime ===

    @Test
    void updateDowntime_shouldUpdateFields() {
        LocalDateTime start = LocalDateTime.of(2026, 5, 5, 10, 0);
        LocalDateTime end = LocalDateTime.of(2026, 5, 5, 12, 0);
        Downtime existing = createDowntime(1L, 1L, start, end, "Старая причина");
        DowntimeDTO dto = createDTO(1L, start, end, "Новая причина");

        when(downtimeRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(downtimeRepository.save(any())).thenReturn(existing);

        DowntimeDTO result = service.updateDowntime(1L, dto);

        assertEquals("Новая причина", result.getDescription());
        verify(downtimeRepository).save(any());
    }

    @Test
    void updateDowntime_shouldThrow_whenNotFound() {
        DowntimeDTO dto = createDTO(1L, LocalDateTime.now(), null, "Причина");

        when(downtimeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.updateDowntime(999L, dto));
    }

    // === deleteDowntime ===

    @Test
    void deleteDowntime_shouldDelete_whenExists() {
        when(downtimeRepository.existsById(1L)).thenReturn(true);

        service.deleteDowntime(1L);

        verify(downtimeRepository).deleteById(1L);
    }

    @Test
    void deleteDowntime_shouldThrow_whenNotFound() {
        when(downtimeRepository.existsById(999L)).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> service.deleteDowntime(999L));
        verify(downtimeRepository, never()).deleteById(any());
    }

    // === convertToDTO — duration ===

    @Test
    void convertToDTO_shouldCalculateDuration_whenEndTimeProvided() {
        LocalDateTime start = LocalDateTime.of(2026, 5, 5, 10, 0);
        LocalDateTime end = LocalDateTime.of(2026, 5, 5, 12, 30);
        Downtime d = createDowntime(1L, 1L, start, end, "Тест");

        when(downtimeRepository.findByForkliftIdOrderByStartTimeDesc(1L))
                .thenReturn(List.of(d));

        List<DowntimeDTO> result = service.getDowntimesByForklift(1L);

        assertEquals("2 h 30 min", result.get(0).getDowntimeDuration());
    }

    @Test
    void convertToDTO_shouldCalculateDuration_whenEndTimeNull() {
        LocalDateTime start = LocalDateTime.of(2026, 5, 5, 10, 0);
        Downtime d = createDowntime(1L, 1L, start, null, "Тест");

        when(downtimeRepository.findByForkliftIdOrderByStartTimeDesc(1L))
                .thenReturn(List.of(d));

        List<DowntimeDTO> result = service.getDowntimesByForklift(1L);

        assertNotNull(result.get(0).getDowntimeDuration());
        assertTrue(result.get(0).getDowntimeDuration().contains("h"));
    }
}