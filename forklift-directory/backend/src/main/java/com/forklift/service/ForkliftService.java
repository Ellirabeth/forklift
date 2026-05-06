package com.forklift.service;

import com.forklift.model.dto.ForkliftDTO;
import java.util.List;

public interface ForkliftService {
    List<ForkliftDTO> getAllForklifts();
    List<ForkliftDTO> searchByNumber(String number);
    ForkliftDTO getForkliftById(Long id);
    ForkliftDTO createForklift(ForkliftDTO forkliftDTO);
    ForkliftDTO updateForklift(Long id, ForkliftDTO forkliftDTO);
    void deleteForklift(Long id);
}
