package com.forklift.model.dto;

import lombok.Data;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
public class DowntimeDTO {
    private Long id;
    
    @NotNull(message = "Forklift ID is required")
    private Long forkliftId;
    
    @NotNull(message = "Start time is required")
    private LocalDateTime startTime;
    
    private LocalDateTime endTime;
    private String description;
    private String downtimeDuration;
    private LocalDateTime createdAt;
}
