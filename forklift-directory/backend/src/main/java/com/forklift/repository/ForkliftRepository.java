package com.forklift.repository;

import com.forklift.model.Forklift;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ForkliftRepository extends JpaRepository<Forklift, Long> {
    Optional<Forklift> findByNumber(String number);
    
    @Query("SELECT f FROM Forklift f WHERE LOWER(f.number) LIKE LOWER(CONCAT('%', :number, '%'))")
    List<Forklift> searchByNumber(@Param("number") String number);
    
    boolean existsById(Long id);
}
