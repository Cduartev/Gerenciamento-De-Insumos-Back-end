package com.project.inventory.dto.product;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ProductCompositionItemRequest(

        @NotNull(message = "O id da materia-prima é obrigatorio.")
        Long rawMaterialId,
        @NotNull(message = "A quantity necessaria é obrigatoria.")
        @DecimalMin(value = "0.0001", inclusive = true, message = "A quantity necessaria deve ser maior que zero.")
        BigDecimal requiredQuantity
) {
}
