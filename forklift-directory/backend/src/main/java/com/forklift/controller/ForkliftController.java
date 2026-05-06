package com.forklift.controller;

import com.forklift.model.dto.ForkliftDTO;
import com.forklift.service.ForkliftService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/forklifts")

public class ForkliftController {

    public final ForkliftService forkliftService;

    public ForkliftController(ForkliftService forkliftService) {
        this.forkliftService = forkliftService;
    }

    @GetMapping
    public ResponseEntity<List<ForkliftDTO>> getAllForklifts() {
        return ResponseEntity.ok(forkliftService.getAllForklifts());
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<ForkliftDTO>> searchForklifts(@RequestParam(required = false) String number) {
        if (number == null || number.trim().isEmpty()) {
            return ResponseEntity.ok(forkliftService.getAllForklifts());
        }
        return ResponseEntity.ok(forkliftService.searchByNumber(number));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ForkliftDTO> getForkliftById(@PathVariable Long id) {
        return ResponseEntity.ok(forkliftService.getForkliftById(id));
    }
    
    @PostMapping
    public ResponseEntity<ForkliftDTO> createForklift(@Valid @RequestBody ForkliftDTO forkliftDTO) {
        return new ResponseEntity<>(forkliftService.createForklift(forkliftDTO), HttpStatus.CREATED);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ForkliftDTO> updateForklift(@PathVariable Long id, @Valid @RequestBody ForkliftDTO forkliftDTO) {
        return ResponseEntity.ok(forkliftService.updateForklift(id, forkliftDTO));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteForklift(@PathVariable Long id) {
        forkliftService.deleteForklift(id);
        return ResponseEntity.noContent().build();
    }
}
