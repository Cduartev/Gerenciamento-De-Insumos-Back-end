package com.projeto.gerenciamento.de.insumos.dto.produto;

import java.math.BigDecimal;
import java.util.List;

public record ProdutoResponse(
        Long id,
        String codigo,
        String nome,
        BigDecimal valor,
        List<ItemComposicaoProdutoResponse> itensComposicao
) {
}
