package com.forklift.service;

import com.forklift.model.Downtime;
import com.forklift.model.Forklift;
import com.forklift.model.dto.DowntimeDTO;
import com.forklift.repository.DowntimeRepository;
import com.forklift.repository.ForkliftRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class DowntimeServiceImpl implements DowntimeService {
    
    public final DowntimeRepository downtimeRepository;
    public final ForkliftRepository forkliftRepository;

    public DowntimeServiceImpl(DowntimeRepository downtimeRepository, ForkliftRepository forkliftRepository) {
        this.downtimeRepository = downtimeRepository;
        this.forkliftRepository = forkliftRepository;
    }

    @Override
    public List<DowntimeDTO> getDowntimesByForklift(Long forkliftId) {
        return downtimeRepository.findByForkliftIdOrderByStartTimeDesc(forkliftId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public DowntimeDTO createDowntime(DowntimeDTO downtimeDTO) {
        Forklift forklift = forkliftRepository.findById(downtimeDTO.getForkliftId())
                .orElseThrow(() -> new EntityNotFoundException("Forklift not found"));
        
        Downtime downtime = new Downtime();
        downtime.setForklift(forklift);
        downtime.setStartTime(downtimeDTO.getStartTime());
        downtime.setEndTime(downtimeDTO.getEndTime());
        downtime.setDescription(downtimeDTO.getDescription());
        
        return convertToDTO(downtimeRepository.save(downtime));
    }
    
    @Override
    public DowntimeDTO updateDowntime(Long id, DowntimeDTO downtimeDTO) {
        Downtime downtime = downtimeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Downtime not found"));
        
        downtime.setStartTime(downtimeDTO.getStartTime());
        downtime.setEndTime(downtimeDTO.getEndTime());
        downtime.setDescription(downtimeDTO.getDescription());
        
        return convertToDTO(downtimeRepository.save(downtime));
    }
    
    @Override
    public void deleteDowntime(Long id) {
        if (!downtimeRepository.existsById(id)) {
            throw new EntityNotFoundException("Downtime not found");
        }
        downtimeRepository.deleteById(id);
    }
    
    private DowntimeDTO convertToDTO(Downtime downtime) {
        DowntimeDTO dto = new DowntimeDTO();
        dto.setId(downtime.getId());
        dto.setForkliftId(downtime.getForklift().getId());
        dto.setStartTime(downtime.getStartTime());
        dto.setEndTime(downtime.getEndTime());
        dto.setDescription(downtime.getDescription());
        dto.setCreatedAt(downtime.getCreatedAt());
        
        LocalDateTime end = downtime.getEndTime() != null ? downtime.getEndTime() : LocalDateTime.now();
        long minutes = Duration.between(downtime.getStartTime(), end).toMinutes();
        long hours = minutes / 60;
        long remainingMinutes = minutes % 60;
        dto.setDowntimeDuration(String.format("%d h %d min", hours, remainingMinutes));
        
        return dto;
    }
}
