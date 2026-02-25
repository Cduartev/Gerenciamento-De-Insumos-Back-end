package com.project.inventory.dto.planproduction;

import java.math.BigDecimal;

public record ProductionPlanItemResponse(
        Long productId,
        String codeProduct,
        String nameProduct,
        Integer suggestedQuantity,
        BigDecimal priceUnitario,
        BigDecimal totalItemValue
) {
}
