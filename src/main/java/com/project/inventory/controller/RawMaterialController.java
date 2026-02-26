package com.project.inventory.controller;

import com.project.inventory.dto.rawmaterial.UpdateRawMaterialRequest;
import com.project.inventory.dto.rawmaterial.CreateRawMaterialRequest;
import com.project.inventory.dto.rawmaterial.RawMaterialResponse;
import com.project.inventory.service.RawMaterialService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/raw-materials")
@RequiredArgsConstructor
public class RawMaterialController {

    private final RawMaterialService rawMaterialService;

    @GetMapping
    public ResponseEntity<List<RawMaterialResponse>> findAll() {
        return ResponseEntity.ok(rawMaterialService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RawMaterialResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(rawMaterialService.findById(id));
    }

    @PostMapping
    public ResponseEntity<RawMaterialResponse> create(@Valid @RequestBody CreateRawMaterialRequest request) {
        RawMaterialResponse response = rawMaterialService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RawMaterialResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRawMaterialRequest request
    ) {
        return ResponseEntity.ok(rawMaterialService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        rawMaterialService.delete(id);
        return ResponseEntity.noContent().build();
    }
}