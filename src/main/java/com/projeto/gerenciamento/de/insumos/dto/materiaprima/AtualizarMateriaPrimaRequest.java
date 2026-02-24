package com.projeto.gerenciamento.de.insumos.dto.materiaprima;
import com.projeto.gerenciamento.de.insumos.domain.enumtype.UnidadeMedida;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record AtualizarMateriaPrimaRequest(
        @NotBlank(message = "O codigo é obrigatorio.")
        @Size(max = 50, message = "O codigo deve ter no maximo 50 caracteres.")
        String codigo,

        @NotBlank(message = "O nome é obrigatorio.")
        @Size(max = 120, message = "O nome deve ter no maximo 120 caracteres.")
        String nome,

        @NotNull(message = "A quantidade em estoque é obrigatoria.")
        @DecimalMin(value = "0.0000", inclusive = true, message = "A quantidade em estoque nao pode ser negativa.")
        BigDecimal quantidadeEstoque,

        @NotNull(message = "A unidade de medida é obrigatoria.")
        UnidadeMedida unidadeMedida
) {
}
