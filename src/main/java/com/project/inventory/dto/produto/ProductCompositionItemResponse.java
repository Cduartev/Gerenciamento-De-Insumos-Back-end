package com.project.inventory.dto.product;

import com.project.inventory.domain.enumtype.UnitOfMeasurement;

import java.math.BigDecimal;

public record ProductCompositionItemResponse(
        Long id,
        Long rawMaterialId,
        String codeRawMaterial,
        String nameRawMaterial,
        UnitOfMeasurement unitOfMeasurement,
        BigDecimal requiredQuantity
) {
}
