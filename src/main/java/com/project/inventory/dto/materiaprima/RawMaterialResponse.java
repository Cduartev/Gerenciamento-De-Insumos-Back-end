package com.project.inventory.dto.materiaprima;

import com.project.inventory.domain.enumtype.UnitOfMeasurement;

import java.math.BigDecimal;

public record RawMaterialResponse(
        Long id,
        String code,
        String name,
        BigDecimal stockQuantity,
        UnitOfMeasurement unitOfMeasurement
) {
}
