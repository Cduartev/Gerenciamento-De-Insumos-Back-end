package com.projeto.gerenciamento.de.insumos.dto.planoproducao;

import java.math.BigDecimal;

public record ItemSugestaoProducaoResponse(
        Long produtoId,
        String codigoProduto,
        String nomeProduto,
        Integer quantidadeSugerida,
        BigDecimal valorUnitario,
        BigDecimal valorTotalItem
) {
}
