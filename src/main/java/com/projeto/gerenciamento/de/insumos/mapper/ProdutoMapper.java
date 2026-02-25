package com.projeto.gerenciamento.de.insumos.mapper;

import com.projeto.gerenciamento.de.insumos.domain.entity.ItemComposicaoProduto;
import com.projeto.gerenciamento.de.insumos.domain.entity.Produto;
import com.projeto.gerenciamento.de.insumos.dto.produto.ItemComposicaoProdutoResponse;
import com.projeto.gerenciamento.de.insumos.dto.produto.ProdutoResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProdutoMapper {

    public ProdutoResponse paraResponse(Produto produto) {
        List<ItemComposicaoProdutoResponse> itens = produto.getItensComposicao()
                .stream()
                .map(this::paraItemResponse)
                .toList();

        return new ProdutoResponse(
                produto.getId(),
                produto.getCodigo(),
                produto.getNome(),
                produto.getValor(),
                itens
        );
    }

    private ItemComposicaoProdutoResponse paraItemResponse(ItemComposicaoProduto item) {
        return new ItemComposicaoProdutoResponse(
                item.getId(),
                item.getMateriaPrima().getId(),
                item.getMateriaPrima().getCodigo(),
                item.getMateriaPrima().getNome(),
                item.getMateriaPrima().getUnidadeMedida(),
                item.getQuantidadeNecessaria()
        );
    }
}
