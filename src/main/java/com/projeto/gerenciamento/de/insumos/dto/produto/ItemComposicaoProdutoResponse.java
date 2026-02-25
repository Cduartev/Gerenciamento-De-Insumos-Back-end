package com.projeto.gerenciamento.de.insumos.dto.produto;

import com.projeto.gerenciamento.de.insumos.domain.enumtype.UnidadeMedida;

import java.math.BigDecimal;

public record ItemComposicaoProdutoResponse(
        Long id,
        Long materiaPrimaId,
        String codigoMateriaPrima,
        String nomeMateriaPrima,
        UnidadeMedida unidadeMedida,
        BigDecimal quantidadeNecessaria
) {
}
