package com.forklift.service;

import com.forklift.model.dto.DowntimeDTO;
import java.util.List;

public interface DowntimeService {
    List<DowntimeDTO> getDowntimesByForklift(Long forkliftId);
    DowntimeDTO createDowntime(DowntimeDTO downtimeDTO);
    DowntimeDTO updateDowntime(Long id, DowntimeDTO downtimeDTO);
    void deleteDowntime(Long id);
}
