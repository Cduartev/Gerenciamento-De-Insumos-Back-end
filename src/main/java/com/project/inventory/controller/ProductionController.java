package com.project.inventory.controller;

import com.project.inventory.dto.production.ProductionRequest;
import com.project.inventory.service.ProductionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/productions")
@RequiredArgsConstructor
public class ProductionController {

    private final ProductionService productionService;

    @PostMapping
    public ResponseEntity<Void> produce(@Valid @RequestBody ProductionRequest request) {
        productionService.produce(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
