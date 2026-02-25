package com.projeto.gerenciamento.de.insumos.dto.planoproducao;

import java.math.BigDecimal;
import java.util.List;

public record SugestaoPlanoProducaoResponse(
        BigDecimal valorTotalVenda,
        Integer quantidadeTotalProduzida,
        List<ItemSugestaoProducaoResponse> itensSugeridos,
        List<ConsumoMateriaPrimaResponse> consumosMateriasPrimas,
        List<SaldoMateriaPrimaResponse> saldosMateriasPrimas
) {
}
