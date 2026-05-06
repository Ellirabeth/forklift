package com.forklift.controller;

import com.forklift.model.dto.DowntimeDTO;
import com.forklift.service.DowntimeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.util.List;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/downtimes")
@RequiredArgsConstructor
public class DowntimeController {
    
    private final DowntimeService downtimeService;
    
    @GetMapping("/forklift/{forkliftId}")
    public ResponseEntity<List<DowntimeDTO>> getDowntimesByForklift(@PathVariable Long forkliftId) {
        return ResponseEntity.ok(downtimeService.getDowntimesByForklift(forkliftId));
    }
    
    @PostMapping
    public ResponseEntity<DowntimeDTO> createDowntime(@Valid @RequestBody DowntimeDTO downtimeDTO) {
        return new ResponseEntity<>(downtimeService.createDowntime(downtimeDTO), HttpStatus.CREATED);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<DowntimeDTO> updateDowntime(@PathVariable Long id, @Valid @RequestBody DowntimeDTO downtimeDTO) {
        return ResponseEntity.ok(downtimeService.updateDowntime(id, downtimeDTO));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDowntime(@PathVariable Long id) {
        downtimeService.deleteDowntime(id);
        return ResponseEntity.noContent().build();
    }
}
