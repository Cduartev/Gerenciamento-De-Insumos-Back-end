package com.projeto.gerenciamento.de.insumos.dto.materiaprima;

import com.projeto.gerenciamento.de.insumos.domain.enumtype.UnidadeMedida;

import java.math.BigDecimal;

public record MateriaPrimaResponse(
        Long id,
        String codigo,
        String nome,
        BigDecimal quantidadeEstoque,
        UnidadeMedida unidadeMedida
) {
}
