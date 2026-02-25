package com.project.inventory.dto.product;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public record CreateProductRequest(

        @NotBlank(message = "O code é obrigatorio.")
        @Size(max = 50, message = "O code deve ter no maximo 50 caracteres.")
        String code,

        @NotBlank(message = "O name é obrigatorio.")
        @Size(max = 120, message = "O name deve ter no maximo 120 caracteres.")
        String name,

        @NotNull(message = "O price é obrigatorio.")
        @DecimalMin(value = "0.01", inclusive = true, message = "O price deve ser maior que zero.")
        BigDecimal price,

        @NotEmpty(message = "A composition do product é obrigatoria e deve possuir ao menos um item.")
        List<@Valid ProductCompositionItemRequest> compositionItems
) {
}
