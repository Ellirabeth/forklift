package com.forklift.service;

import com.forklift.model.Forklift;
import com.forklift.model.dto.ForkliftDTO;
import com.forklift.repository.DowntimeRepository;
import com.forklift.repository.ForkliftRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ForkliftServiceImpl implements ForkliftService {
    
    public final ForkliftRepository forkliftRepository;
    public final DowntimeRepository downtimeRepository;

    public ForkliftServiceImpl(ForkliftRepository forkliftRepository, DowntimeRepository downtimeRepository) {
        this.forkliftRepository = forkliftRepository;
        this.downtimeRepository = downtimeRepository;
    }

    @Override
    public List<ForkliftDTO> getAllForklifts() {
        return forkliftRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<ForkliftDTO> searchByNumber(String number) {
        return forkliftRepository.searchByNumber(number).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public ForkliftDTO getForkliftById(Long id) {
        return convertToDTO(findByIdOrThrow(id));
    }
    
    @Override
    public ForkliftDTO createForklift(ForkliftDTO forkliftDTO) {
        Forklift forklift = convertToEntity(forkliftDTO);
        forklift.setLastModified(LocalDateTime.now());
        forklift.setModifiedBy(resolveModifiedBy(forkliftDTO.getModifiedBy()));
        forklift.setIsActive(true);
        return convertToDTO(forkliftRepository.save(forklift));
    }
    
    @Override
    public ForkliftDTO updateForklift(Long id, ForkliftDTO forkliftDTO) {
        Forklift existingForklift = findByIdOrThrow(id);
        
        existingForklift.setBrand(forkliftDTO.getBrand());
        existingForklift.setNumber(forkliftDTO.getNumber());
        existingForklift.setLoadCapacity(forkliftDTO.getLoadCapacity());
        existingForklift.setModifiedBy(resolveModifiedBy(forkliftDTO.getModifiedBy()));
        existingForklift.setLastModified(LocalDateTime.now());
        
        return convertToDTO(forkliftRepository.save(existingForklift));
    }
    
    @Override
    public void deleteForklift(Long id) {
        Forklift forklift = findByIdOrThrow(id);
        
        if (downtimeRepository.existsByForklift(forklift)) {
            throw new IllegalStateException("Cannot delete forklift - has registered downtimes");
        }
        
        forkliftRepository.delete(forklift);
    }

    // В методе convertToDTO
    private ForkliftDTO convertToDTO(Forklift forklift) {
        ForkliftDTO dto = new ForkliftDTO();
        dto.setId(forklift.getId());
        dto.setBrand(forklift.getBrand());
        dto.setNumber(forklift.getNumber());
        dto.setLoadCapacity(forklift.getLoadCapacity());
        dto.setIsActive(forklift.getIsActive());
        dto.setLastModified(forklift.getLastModified());
        dto.setModifiedBy(forklift.getModifiedBy());
        dto.setHasDowntimes(downtimeRepository.existsByForklift(forklift));
        return dto;
    }

    // В методе convertToEntity
    private Forklift findByIdOrThrow(Long id) {
        return forkliftRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Forklift not found with id: " + id));
    }

    private String resolveModifiedBy(String modifiedBy) {
        return modifiedBy != null ? modifiedBy : "по умолчанию";
    }

    private Forklift convertToEntity(ForkliftDTO dto) {
        Forklift forklift = new Forklift();
        forklift.setBrand(dto.getBrand());
        forklift.setNumber(dto.getNumber());
        forklift.setLoadCapacity(dto.getLoadCapacity());
        forklift.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : true);
        return forklift;
    }
}
