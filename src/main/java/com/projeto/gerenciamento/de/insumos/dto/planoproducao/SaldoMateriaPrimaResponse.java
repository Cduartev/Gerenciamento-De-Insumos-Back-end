package com.projeto.gerenciamento.de.insumos.dto.planoproducao;

import com.projeto.gerenciamento.de.insumos.domain.enumtype.UnidadeMedida;

import java.math.BigDecimal;

public record SaldoMateriaPrimaResponse(
        Long materiaPrimaId,
        String codigoMateriaPrima,
        String nomeMateriaPrima,
        UnidadeMedida unidadeMedida,
        BigDecimal quantidadeInicial,
        BigDecimal quantidadeConsumida,
        BigDecimal quantidadeSaldo
) {
}
