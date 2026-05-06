package com.forklift.model.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ForkliftDTO {
    private Long id;

    @NotBlank(message = "Марка обязательна")
    private String brand;

    @NotBlank(message = "Номер обязателен")
    private String number;

    @NotNull(message = "Грузоподъемность обязательна")
    @Positive(message = "Грузоподъемность должна быть положительной")
    private BigDecimal loadCapacity;

    private Boolean isActive;
    private LocalDateTime lastModified;
    private String modifiedBy;
    private Boolean hasDowntimes;
}
