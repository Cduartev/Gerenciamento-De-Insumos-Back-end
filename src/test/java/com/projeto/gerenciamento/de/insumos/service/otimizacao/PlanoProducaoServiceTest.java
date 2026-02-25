package com.projeto.gerenciamento.de.insumos.service.otimizacao;

import com.projeto.gerenciamento.de.insumos.domain.entity.ItemComposicaoProduto;
import com.projeto.gerenciamento.de.insumos.domain.entity.MateriaPrima;
import com.projeto.gerenciamento.de.insumos.domain.entity.Produto;
import com.projeto.gerenciamento.de.insumos.domain.enumtype.UnidadeMedida;
import com.projeto.gerenciamento.de.insumos.domain.repository.MateriaPrimaRepository;
import com.projeto.gerenciamento.de.insumos.domain.repository.ProdutoRepository;
import com.projeto.gerenciamento.de.insumos.dto.planoproducao.ConsumoMateriaPrimaResponse;
import com.projeto.gerenciamento.de.insumos.dto.planoproducao.ItemSugestaoProducaoResponse;
import com.projeto.gerenciamento.de.insumos.dto.planoproducao.SaldoMateriaPrimaResponse;
import com.projeto.gerenciamento.de.insumos.dto.planoproducao.SugestaoPlanoProducaoResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("PlanoProducaoService — Lógica de Otimização")
class PlanoProducaoServiceTest {

    @Mock
    private ProdutoRepository produtoRepository;

    @Mock
    private MateriaPrimaRepository materiaPrimaRepository;

    @InjectMocks
    private PlanoProducaoService planoProducaoService;

    private MateriaPrima criarMateriaPrima(long id, String codigo, String nome,
                                          BigDecimal estoque, UnidadeMedida unidade) {
        return MateriaPrima.builder()
                .id(id)
                .codigo(codigo)
                .nome(nome)
                .quantidadeEstoque(estoque)
                .unidadeMedida(unidade)
                .build();
    }

    private Produto criarProduto(Long id, String codigo, String nome, BigDecimal valor) {
        return Produto.builder()
                .id(id)
                .codigo(codigo)
                .nome(nome)
                .valor(valor)
                .itensComposicao(new ArrayList<>())
                .build();
    }

    private void adicionarComposicao(Produto produto, MateriaPrima mp, BigDecimal qtdNecessaria) {
        ItemComposicaoProduto item = ItemComposicaoProduto.builder()
                .materiaPrima(mp)
                .quantidadeNecessaria(qtdNecessaria)
                .produto(produto)
                .build();
        produto.getItensComposicao().add(item);
    }

    @Test
    @DisplayName("Deve retornar plano vazio quando não há produtos cadastrados")
    void deveRetornarPlanoVazio_quandoNaoHaProdutos() {
        MateriaPrima mp = criarMateriaPrima(1L, "MP-001", "Farinha", new BigDecimal("100"), UnidadeMedida.QUILOGRAMA);
        when(produtoRepository.findAll()).thenReturn(List.of());
        when(materiaPrimaRepository.findAll()).thenReturn(List.of(mp));

        SugestaoPlanoProducaoResponse resposta = planoProducaoService.sugerirPlanoOtimo();

        assertThat(resposta.valorTotalVenda()).isEqualByComparingTo(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        assertThat(resposta.quantidadeTotalProduzida()).isZero();
        assertThat(resposta.itensSugeridos()).isEmpty();
        assertThat(resposta.consumosMateriasPrimas()).isEmpty();
        assertThat(resposta.saldosMateriasPrimas()).hasSize(1);

        SaldoMateriaPrimaResponse saldo = resposta.saldosMateriasPrimas().get(0);
        assertThat(saldo.quantidadeInicial()).isEqualByComparingTo(new BigDecimal("100"));
        assertThat(saldo.quantidadeConsumida()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(saldo.quantidadeSaldo()).isEqualByComparingTo(new BigDecimal("100"));
    }

    @Test
    @DisplayName("Deve ignorar produtos sem composição válida")
    void deveIgnorarProdutosSemComposicao() {
        MateriaPrima mp = criarMateriaPrima(1L, "MP-001", "Farinha", new BigDecimal("100"), UnidadeMedida.QUILOGRAMA);

        Produto produtoSemComposicao = criarProduto(1L, "P-001", "Bolo Simples", new BigDecimal("50.00"));

        when(produtoRepository.findAll()).thenReturn(List.of(produtoSemComposicao));
        when(materiaPrimaRepository.findAll()).thenReturn(List.of(mp));

        SugestaoPlanoProducaoResponse resposta = planoProducaoService.sugerirPlanoOtimo();

        assertThat(resposta.quantidadeTotalProduzida()).isZero();
        assertThat(resposta.itensSugeridos()).isEmpty();
    }

    @Test
    @DisplayName("Deve calcular produção de um produto simples com uma matéria-prima")
    void deveCalcularProducaoProdutoSimples() {
        MateriaPrima farinha = criarMateriaPrima(1L, "MP-001", "Farinha", new BigDecimal("10"), UnidadeMedida.QUILOGRAMA);

        Produto bolo = criarProduto(1L, "P-001", "Bolo", new BigDecimal("25.00"));
        adicionarComposicao(bolo, farinha, new BigDecimal("2"));

        when(produtoRepository.findAll()).thenReturn(List.of(bolo));
        when(materiaPrimaRepository.findAll()).thenReturn(List.of(farinha));

        SugestaoPlanoProducaoResponse resposta = planoProducaoService.sugerirPlanoOtimo();

        assertThat(resposta.quantidadeTotalProduzida()).isEqualTo(5);
        assertThat(resposta.valorTotalVenda()).isEqualByComparingTo(new BigDecimal("125.00"));
        assertThat(resposta.itensSugeridos()).hasSize(1);

        ItemSugestaoProducaoResponse item = resposta.itensSugeridos().get(0);
        assertThat(item.produtoId()).isEqualTo(1L);
        assertThat(item.quantidadeSugerida()).isEqualTo(5);
        assertThat(item.valorTotalItem()).isEqualByComparingTo(new BigDecimal("125.00"));
    }

    @Test
    @DisplayName("Deve respeitar a matéria-prima limitante (gargalo) quando produto tem múltiplas MPs")
    void deveRespeitarMateriaPrimaLimitante() {
        MateriaPrima farinha = criarMateriaPrima(1L, "MP-001", "Farinha", new BigDecimal("20"), UnidadeMedida.QUILOGRAMA);
        MateriaPrima acucar = criarMateriaPrima(2L, "MP-002", "Açúcar", new BigDecimal("3"), UnidadeMedida.QUILOGRAMA);

        Produto bolo = criarProduto(1L, "P-001", "Bolo", new BigDecimal("30.00"));
        adicionarComposicao(bolo, farinha, new BigDecimal("2"));
        adicionarComposicao(bolo, acucar, new BigDecimal("1"));

        when(produtoRepository.findAll()).thenReturn(List.of(bolo));
        when(materiaPrimaRepository.findAll()).thenReturn(List.of(farinha, acucar));

        SugestaoPlanoProducaoResponse resposta = planoProducaoService.sugerirPlanoOtimo();

        assertThat(resposta.quantidadeTotalProduzida()).isEqualTo(3);
        assertThat(resposta.valorTotalVenda()).isEqualByComparingTo(new BigDecimal("90.00"));
    }

    @Test
    @DisplayName("Deve otimizar dois produtos que competem pela mesma matéria-prima para maximizar valor")
    void deveOtimizarDoisProdutosCompetindoPorMP() {
        MateriaPrima farinha = criarMateriaPrima(1L, "MP-001", "Farinha", new BigDecimal("10"), UnidadeMedida.QUILOGRAMA);

        Produto bolo = criarProduto(1L, "P-001", "Bolo", new BigDecimal("50.00"));
        adicionarComposicao(bolo, farinha, new BigDecimal("5"));

        Produto pao = criarProduto(2L, "P-002", "Pão", new BigDecimal("15.00"));
        adicionarComposicao(pao, farinha, new BigDecimal("2"));

        when(produtoRepository.findAll()).thenReturn(List.of(bolo, pao));
        when(materiaPrimaRepository.findAll()).thenReturn(List.of(farinha));

        SugestaoPlanoProducaoResponse resposta = planoProducaoService.sugerirPlanoOtimo();

        assertThat(resposta.valorTotalVenda()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(resposta.quantidadeTotalProduzida()).isEqualTo(2);
    }

    @Test
    @DisplayName("Deve desempatar por quantidade quando valor total é igual")
    void deveDesempatarPorQuantidade_quandoValorIgual() {
        MateriaPrima farinha = criarMateriaPrima(1L, "MP-001", "Farinha", new BigDecimal("10"), UnidadeMedida.QUILOGRAMA);

        Produto produtoA = criarProduto(1L, "P-001", "Produto A", new BigDecimal("50.00"));
        adicionarComposicao(produtoA, farinha, new BigDecimal("5"));

        Produto produtoB = criarProduto(2L, "P-002", "Produto B", new BigDecimal("20.00"));
        adicionarComposicao(produtoB, farinha, new BigDecimal("2"));

        when(produtoRepository.findAll()).thenReturn(List.of(produtoA, produtoB));
        when(materiaPrimaRepository.findAll()).thenReturn(List.of(farinha));

        SugestaoPlanoProducaoResponse resposta = planoProducaoService.sugerirPlanoOtimo();

        assertThat(resposta.valorTotalVenda()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(resposta.quantidadeTotalProduzida()).isEqualTo(5);
    }

    @Test
    @DisplayName("Deve retornar produção zerada quando estoque é zero")
    void deveRetornarProducaoZerada_quandoEstoqueZero() {
        MateriaPrima farinha = criarMateriaPrima(1L, "MP-001", "Farinha", BigDecimal.ZERO, UnidadeMedida.QUILOGRAMA);

        Produto bolo = criarProduto(1L, "P-001", "Bolo", new BigDecimal("25.00"));
        adicionarComposicao(bolo, farinha, new BigDecimal("2"));

        when(produtoRepository.findAll()).thenReturn(List.of(bolo));
        when(materiaPrimaRepository.findAll()).thenReturn(List.of(farinha));

        SugestaoPlanoProducaoResponse resposta = planoProducaoService.sugerirPlanoOtimo();

        assertThat(resposta.quantidadeTotalProduzida()).isZero();
        assertThat(resposta.valorTotalVenda()).isEqualByComparingTo(BigDecimal.ZERO.setScale(2));
        assertThat(resposta.itensSugeridos()).isEmpty();
    }

    @Test
    @DisplayName("Deve calcular consumos e saldos corretamente nos DTOs de resposta")
    void deveCalcularConsumosESaldosCorretamente() {
        MateriaPrima farinha = criarMateriaPrima(1L, "MP-001", "Farinha", new BigDecimal("10"), UnidadeMedida.QUILOGRAMA);
        MateriaPrima acucar = criarMateriaPrima(2L, "MP-002", "Açúcar", new BigDecimal("5"), UnidadeMedida.QUILOGRAMA);

        Produto bolo = criarProduto(1L, "P-001", "Bolo", new BigDecimal("30.00"));
        adicionarComposicao(bolo, farinha, new BigDecimal("2"));
        adicionarComposicao(bolo, acucar, new BigDecimal("1"));

        when(produtoRepository.findAll()).thenReturn(List.of(bolo));
        when(materiaPrimaRepository.findAll()).thenReturn(List.of(farinha, acucar));

        SugestaoPlanoProducaoResponse resposta = planoProducaoService.sugerirPlanoOtimo();

        assertThat(resposta.quantidadeTotalProduzida()).isEqualTo(5);

        assertThat(resposta.consumosMateriasPrimas()).hasSize(2);

        ConsumoMateriaPrimaResponse consumoFarinha = resposta.consumosMateriasPrimas().stream()
                .filter(c -> c.codigoMateriaPrima().equals("MP-001"))
                .findFirst().orElseThrow();
        assertThat(consumoFarinha.quantidadeConsumida()).isEqualByComparingTo(new BigDecimal("10"));

        ConsumoMateriaPrimaResponse consumoAcucar = resposta.consumosMateriasPrimas().stream()
                .filter(c -> c.codigoMateriaPrima().equals("MP-002"))
                .findFirst().orElseThrow();
        assertThat(consumoAcucar.quantidadeConsumida()).isEqualByComparingTo(new BigDecimal("5"));

        assertThat(resposta.saldosMateriasPrimas()).hasSize(2);

        SaldoMateriaPrimaResponse saldoFarinha = resposta.saldosMateriasPrimas().stream()
                .filter(s -> s.codigoMateriaPrima().equals("MP-001"))
                .findFirst().orElseThrow();
        assertThat(saldoFarinha.quantidadeInicial()).isEqualByComparingTo(new BigDecimal("10"));
        assertThat(saldoFarinha.quantidadeConsumida()).isEqualByComparingTo(new BigDecimal("10"));
        assertThat(saldoFarinha.quantidadeSaldo()).isEqualByComparingTo(BigDecimal.ZERO);

        SaldoMateriaPrimaResponse saldoAcucar = resposta.saldosMateriasPrimas().stream()
                .filter(s -> s.codigoMateriaPrima().equals("MP-002"))
                .findFirst().orElseThrow();
        assertThat(saldoAcucar.quantidadeInicial()).isEqualByComparingTo(new BigDecimal("5"));
        assertThat(saldoAcucar.quantidadeConsumida()).isEqualByComparingTo(new BigDecimal("5"));
        assertThat(saldoAcucar.quantidadeSaldo()).isEqualByComparingTo(BigDecimal.ZERO);
    }
}
