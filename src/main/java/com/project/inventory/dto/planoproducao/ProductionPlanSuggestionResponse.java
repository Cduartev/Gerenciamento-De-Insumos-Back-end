package com.project.inventory.dto.planproduction;

import java.math.BigDecimal;
import java.util.List;

public record ProductionPlanSuggestionResponse(
        BigDecimal totalSalesValue,
        Integer totalProducedQuantity,
        List<ProductionPlanItemResponse> suggestedItems,
        List<ConsumoRawMaterialResponse> rawMaterialConsumptions,
        List<SaldoRawMaterialResponse> rawMaterialBalances
) {
}
