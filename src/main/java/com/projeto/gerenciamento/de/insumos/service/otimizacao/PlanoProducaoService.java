package com.projeto.gerenciamento.de.insumos.service.otimizacao;

import com.projeto.gerenciamento.de.insumos.domain.entity.ItemComposicaoProduto;
import com.projeto.gerenciamento.de.insumos.domain.entity.MateriaPrima;
import com.projeto.gerenciamento.de.insumos.domain.entity.Produto;
import com.projeto.gerenciamento.de.insumos.domain.repository.MateriaPrimaRepository;
import com.projeto.gerenciamento.de.insumos.domain.repository.ProdutoRepository;
import com.projeto.gerenciamento.de.insumos.dto.planoproducao.ConsumoMateriaPrimaResponse;
import com.projeto.gerenciamento.de.insumos.dto.planoproducao.ItemSugestaoProducaoResponse;
import com.projeto.gerenciamento.de.insumos.dto.planoproducao.SaldoMateriaPrimaResponse;
import com.projeto.gerenciamento.de.insumos.dto.planoproducao.SugestaoPlanoProducaoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlanoProducaoService {

    private static final BigDecimal ZERO = BigDecimal.ZERO;

    private final ProdutoRepository produtoRepository;
    private final MateriaPrimaRepository materiaPrimaRepository;

    @Transactional(readOnly = true)
    public SugestaoPlanoProducaoResponse sugerirPlanoOtimo() {
        List<Produto> produtos = produtoRepository.findAll()
                .stream()
                .filter(this::produtoTemComposicaoValida)
                .sorted(Comparator
                        .comparing(Produto::getValor).reversed()
                        .thenComparing(Produto::getCodigo))
                .toList();

        List<MateriaPrima> materiasPrimas = materiaPrimaRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(MateriaPrima::getCodigo))
                .toList();

        Map<Long, BigDecimal> estoqueInicial = criarMapaEstoqueInicial(materiasPrimas);

        if (produtos.isEmpty()) {
            return montarRespostaSemProducao(materiasPrimas, estoqueInicial);
        }

        EstadoBusca estado = new EstadoBusca(produtos.size());
        Map<Long, BigDecimal> estoqueDisponivel = new HashMap<>(estoqueInicial);

        buscarMelhorCombinacao(
                0,
                produtos,
                estoqueDisponivel,
                estado,
                ZERO,
                0
        );

        return montarResposta(produtos, materiasPrimas, estoqueInicial, estado.melhorQuantidades);
    }

    private boolean produtoTemComposicaoValida(Produto produto) {
        return produto.getItensComposicao() != null && !produto.getItensComposicao().isEmpty();
    }

    private Map<Long, BigDecimal> criarMapaEstoqueInicial(List<MateriaPrima> materiasPrimas) {
        Map<Long, BigDecimal> estoqueInicial = new HashMap<>();

        for (MateriaPrima materiaPrima : materiasPrimas) {
            estoqueInicial.put(
                    materiaPrima.getId(),
                    valorNaoNulo(materiaPrima.getQuantidadeEstoque())
            );
        }

        return estoqueInicial;
    }

    private SugestaoPlanoProducaoResponse montarRespostaSemProducao(
            List<MateriaPrima> materiasPrimas,
            Map<Long, BigDecimal> estoqueInicial
    ) {
        List<SaldoMateriaPrimaResponse> saldos = materiasPrimas.stream()
                .map(materiaPrima -> new SaldoMateriaPrimaResponse(
                        materiaPrima.getId(),
                        materiaPrima.getCodigo(),
                        materiaPrima.getNome(),
                        materiaPrima.getUnidadeMedida(),
                        estoqueInicial.getOrDefault(materiaPrima.getId(), ZERO),
                        ZERO,
                        estoqueInicial.getOrDefault(materiaPrima.getId(), ZERO)
                ))
                .toList();

        return new SugestaoPlanoProducaoResponse(
                ZERO.setScale(2, RoundingMode.HALF_UP),
                0,
                List.of(),
                List.of(),
                saldos
        );
    }

    private void buscarMelhorCombinacao(
            int indiceProduto,
            List<Produto> produtos,
            Map<Long, BigDecimal> estoqueDisponivel,
            EstadoBusca estado,
            BigDecimal valorTotalAtual,
            int quantidadeTotalAtual
    ) {
        if (indiceProduto == produtos.size()) {
            avaliarMelhorSolucao(estado, valorTotalAtual, quantidadeTotalAtual);
            return;
        }

        Produto produtoAtual = produtos.get(indiceProduto);
        int maximoUnidades = calcularMaximoUnidadesFabricaveis(produtoAtual, estoqueDisponivel);

        for (int quantidade = maximoUnidades; quantidade >= 0; quantidade--) {
            estado.quantidadesAtuais[indiceProduto] = quantidade;

            if (quantidade > 0) {
                consumirMateriasPrimas(produtoAtual, quantidade, estoqueDisponivel);
            }

            BigDecimal novoValorTotal = valorTotalAtual.add(
                    produtoAtual.getValor().multiply(BigDecimal.valueOf(quantidade))
            );
            int novaQuantidadeTotal = quantidadeTotalAtual + quantidade;

            buscarMelhorCombinacao(
                    indiceProduto + 1,
                    produtos,
                    estoqueDisponivel,
                    estado,
                    novoValorTotal,
                    novaQuantidadeTotal
            );

            if (quantidade > 0) {
                devolverMateriasPrimas(produtoAtual, quantidade, estoqueDisponivel);
            }
        }

        estado.quantidadesAtuais[indiceProduto] = 0;
    }

    private int calcularMaximoUnidadesFabricaveis(Produto produto, Map<Long, BigDecimal> estoqueDisponivel) {
        int maximo = Integer.MAX_VALUE;

        for (ItemComposicaoProduto item : produto.getItensComposicao()) {
            Long materiaPrimaId = item.getMateriaPrima().getId();
            BigDecimal quantidadeDisponivel = estoqueDisponivel.getOrDefault(materiaPrimaId, ZERO);
            BigDecimal quantidadeNecessaria = item.getQuantidadeNecessaria();

            if (quantidadeNecessaria == null || quantidadeNecessaria.compareTo(ZERO) <= 0) {
                return 0;
            }

            int possivelComEssaMateriaPrima = quantidadeDisponivel
                    .divide(quantidadeNecessaria, 0, RoundingMode.DOWN)
                    .intValue();

            maximo = Math.min(maximo, possivelComEssaMateriaPrima);

            if (maximo == 0) {
                return 0;
            }
        }

        return maximo == Integer.MAX_VALUE ? 0 : maximo;
    }

    private void consumirMateriasPrimas(
            Produto produto,
            int quantidadeProduto,
            Map<Long, BigDecimal> estoqueDisponivel
    ) {
        BigDecimal multiplicador = BigDecimal.valueOf(quantidadeProduto);

        for (ItemComposicaoProduto item : produto.getItensComposicao()) {
            Long materiaPrimaId = item.getMateriaPrima().getId();
            BigDecimal consumo = item.getQuantidadeNecessaria().multiply(multiplicador);

            BigDecimal atual = estoqueDisponivel.getOrDefault(materiaPrimaId, ZERO);
            estoqueDisponivel.put(materiaPrimaId, atual.subtract(consumo));
        }
    }

    private void devolverMateriasPrimas(
            Produto produto,
            int quantidadeProduto,
            Map<Long, BigDecimal> estoqueDisponivel
    ) {
        BigDecimal multiplicador = BigDecimal.valueOf(quantidadeProduto);

        for (ItemComposicaoProduto item : produto.getItensComposicao()) {
            Long materiaPrimaId = item.getMateriaPrima().getId();
            BigDecimal devolucao = item.getQuantidadeNecessaria().multiply(multiplicador);

            BigDecimal atual = estoqueDisponivel.getOrDefault(materiaPrimaId, ZERO);
            estoqueDisponivel.put(materiaPrimaId, atual.add(devolucao));
        }
    }

    private void avaliarMelhorSolucao(
            EstadoBusca estado,
            BigDecimal valorTotalAtual,
            int quantidadeTotalAtual
    ) {
        int comparacaoValor = valorTotalAtual.compareTo(estado.melhorValorTotal);

        boolean deveSubstituirMelhor = comparacaoValor > 0
                || (comparacaoValor == 0 && quantidadeTotalAtual > estado.melhorQuantidadeTotal);

        if (deveSubstituirMelhor) {
            estado.melhorValorTotal = valorTotalAtual;
            estado.melhorQuantidadeTotal = quantidadeTotalAtual;
            estado.melhorQuantidades = Arrays.copyOf(estado.quantidadesAtuais, estado.quantidadesAtuais.length);
        }
    }

    private SugestaoPlanoProducaoResponse montarResposta(
            List<Produto> produtos,
            List<MateriaPrima> materiasPrimas,
            Map<Long, BigDecimal> estoqueInicial,
            int[] quantidadesSugeridas
    ) {
        Map<Long, BigDecimal> estoqueFinal = new HashMap<>(estoqueInicial);

        List<ItemSugestaoProducaoResponse> itensSugeridos = new ArrayList<>();
        BigDecimal valorTotalVenda = ZERO;
        int quantidadeTotalProduzida = 0;

        for (int i = 0; i < produtos.size(); i++) {
            int quantidade = quantidadesSugeridas[i];

            if (quantidade <= 0) {
                continue;
            }

            Produto produto = produtos.get(i);

            BigDecimal valorTotalItem = produto.getValor().multiply(BigDecimal.valueOf(quantidade));
            valorTotalVenda = valorTotalVenda.add(valorTotalItem);
            quantidadeTotalProduzida += quantidade;

            itensSugeridos.add(new ItemSugestaoProducaoResponse(
                    produto.getId(),
                    produto.getCodigo(),
                    produto.getNome(),
                    quantidade,
                    produto.getValor(),
                    valorTotalItem
            ));

            aplicarConsumoNoMapaEstoqueFinal(produto, quantidade, estoqueFinal);
        }

        List<ConsumoMateriaPrimaResponse> consumos = new ArrayList<>();
        List<SaldoMateriaPrimaResponse> saldos = new ArrayList<>();

        for (MateriaPrima materiaPrima : materiasPrimas) {
            Long id = materiaPrima.getId();
            BigDecimal quantidadeInicial = estoqueInicial.getOrDefault(id, ZERO);
            BigDecimal quantidadeSaldo = estoqueFinal.getOrDefault(id, ZERO);
            BigDecimal quantidadeConsumida = quantidadeInicial.subtract(quantidadeSaldo);

            if (quantidadeConsumida.compareTo(ZERO) > 0) {
                consumos.add(new ConsumoMateriaPrimaResponse(
                        materiaPrima.getId(),
                        materiaPrima.getCodigo(),
                        materiaPrima.getNome(),
                        materiaPrima.getUnidadeMedida(),
                        quantidadeConsumida
                ));
            }

            saldos.add(new SaldoMateriaPrimaResponse(
                    materiaPrima.getId(),
                    materiaPrima.getCodigo(),
                    materiaPrima.getNome(),
                    materiaPrima.getUnidadeMedida(),
                    quantidadeInicial,
                    quantidadeConsumida.max(ZERO),
                    quantidadeSaldo
            ));
        }

        return new SugestaoPlanoProducaoResponse(
                valorTotalVenda.setScale(2, RoundingMode.HALF_UP),
                quantidadeTotalProduzida,
                itensSugeridos,
                consumos,
                saldos
        );
    }

    private void aplicarConsumoNoMapaEstoqueFinal(
            Produto produto,
            int quantidadeProduto,
            Map<Long, BigDecimal> estoqueFinal
    ) {
        BigDecimal multiplicador = BigDecimal.valueOf(quantidadeProduto);

        for (ItemComposicaoProduto item : produto.getItensComposicao()) {
            Long materiaPrimaId = item.getMateriaPrima().getId();
            BigDecimal consumo = item.getQuantidadeNecessaria().multiply(multiplicador);

            BigDecimal atual = estoqueFinal.getOrDefault(materiaPrimaId, ZERO);
            estoqueFinal.put(materiaPrimaId, atual.subtract(consumo));
        }
    }

    private BigDecimal valorNaoNulo(BigDecimal valor) {
        return valor == null ? ZERO : valor;
    }

    private static class EstadoBusca {
        private final int[] quantidadesAtuais;
        private int[] melhorQuantidades;
        private BigDecimal melhorValorTotal;
        private int melhorQuantidadeTotal;

        private EstadoBusca(int quantidadeProdutos) {
            this.quantidadesAtuais = new int[quantidadeProdutos];
            this.melhorQuantidades = new int[quantidadeProdutos];
            this.melhorValorTotal = ZERO;
            this.melhorQuantidadeTotal = 0;
        }
    }
}
