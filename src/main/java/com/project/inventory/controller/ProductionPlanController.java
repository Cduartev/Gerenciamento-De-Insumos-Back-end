package com.project.inventory.controller;

import com.project.inventory.dto.planproduction.ProductionPlanSuggestionResponse;
import com.project.inventory.service.optimization.ProductionPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/plans-production")
@RequiredArgsConstructor
public class ProductionPlanController {

    private final ProductionPlanService productionPlanService;

    @PostMapping("/suggest")
    public ResponseEntity<ProductionPlanSuggestionResponse> suggestOptimalPlan() {
        return ResponseEntity.ok(productionPlanService.suggestOptimalPlan());
    }
}
