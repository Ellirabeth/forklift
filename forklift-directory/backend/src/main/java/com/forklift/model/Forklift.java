package com.forklift.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "forklifts")
public class Forklift {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String brand;

    @Column(nullable = false, unique = true, length = 50)
    private String number;

    @Column(name = "load_capacity", nullable = false, precision = 10, scale = 3)
    private BigDecimal loadCapacity;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "last_modified")
    private LocalDateTime lastModified;

    @Column(name = "modified_by", length = 255)
    private String modifiedBy;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        lastModified = LocalDateTime.now();
    }
}