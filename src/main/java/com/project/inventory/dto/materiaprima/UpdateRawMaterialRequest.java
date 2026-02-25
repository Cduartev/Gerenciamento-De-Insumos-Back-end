package com.project.inventory.dto.materiaprima;
import com.project.inventory.domain.enumtype.UnitOfMeasurement;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record UpdateRawMaterialRequest(
        @NotBlank(message = "O code é obrigatorio.")
        @Size(max = 50, message = "O code deve ter no maximo 50 caracteres.")
        String code,

        @NotBlank(message = "O name é obrigatorio.")
        @Size(max = 120, message = "O name deve ter no maximo 120 caracteres.")
        String name,

        @NotNull(message = "A quantity em stock é obrigatoria.")
        @DecimalMin(value = "0.0000", inclusive = true, message = "Stock quantity cannot be negative.")
        BigDecimal stockQuantity,

        @NotNull(message = "A unit de measurement é obrigatoria.")
        UnitOfMeasurement unitOfMeasurement
) {
}
