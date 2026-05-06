package com.forklift.repository;

import com.forklift.model.Downtime;
import com.forklift.model.Forklift;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DowntimeRepository extends JpaRepository<Downtime, Long> {
    List<Downtime> findByForkliftOrderByStartTimeDesc(Forklift forklift);
    
    boolean existsByForklift(Forklift forklift);
    
    @Query("SELECT d FROM Downtime d WHERE d.forklift.id = :forkliftId ORDER BY d.startTime DESC")
    List<Downtime> findByForkliftIdOrderByStartTimeDesc(@Param("forkliftId") Long forkliftId);
}
