package com.project.inventory.dto.planproduction;

import com.project.inventory.domain.enumtype.UnitOfMeasurement;

import java.math.BigDecimal;

public record ConsumoRawMaterialResponse(
        Long rawMaterialId,
        String codeRawMaterial,
        String nameRawMaterial,
        UnitOfMeasurement unitOfMeasurement,
        BigDecimal consumedQuantity
) {
}
