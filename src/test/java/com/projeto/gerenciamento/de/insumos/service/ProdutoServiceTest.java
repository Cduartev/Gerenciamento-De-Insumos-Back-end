package com.projeto.gerenciamento.de.insumos.service;

import com.projeto.gerenciamento.de.insumos.domain.entity.MateriaPrima;
import com.projeto.gerenciamento.de.insumos.domain.entity.Produto;
import com.projeto.gerenciamento.de.insumos.domain.enumtype.UnidadeMedida;
import com.projeto.gerenciamento.de.insumos.domain.repository.MateriaPrimaRepository;
import com.projeto.gerenciamento.de.insumos.domain.repository.ProdutoRepository;
import com.projeto.gerenciamento.de.insumos.dto.produto.*;
import com.projeto.gerenciamento.de.insumos.exception.RecursoNaoEncontradoException;
import com.projeto.gerenciamento.de.insumos.exception.RegraDeNegocioException;
import com.projeto.gerenciamento.de.insumos.mapper.ProdutoMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProdutoService — CRUD de Produto")
class ProdutoServiceTest {

    @Mock
    private ProdutoRepository produtoRepository;

    @Mock
    private MateriaPrimaRepository materiaPrimaRepository;

    @Mock
    private ProdutoMapper produtoMapper;

    @InjectMocks
    private ProdutoService produtoService;


    private Produto criarProduto(Long id, String codigo, String nome, BigDecimal valor) {
        return Produto.builder()
                .id(id)
                .codigo(codigo)
                .nome(nome)
                .valor(valor)
                .itensComposicao(new ArrayList<>())
                .build();
    }

    private MateriaPrima criarMateriaPrima(long id, String codigo) {
        return MateriaPrima.builder()
                .id(id)
                .codigo(codigo)
                .nome("MP " + codigo)
                .quantidadeEstoque(new BigDecimal("100"))
                .unidadeMedida(UnidadeMedida.QUILOGRAMA)
                .build();
    }

    private ProdutoResponse criarResponse(Long id, String codigo, String nome) {
        return new ProdutoResponse(id, codigo, nome, new BigDecimal("50.00"), List.of());
    }


    @Test
    @DisplayName("listarTodos: deve retornar lista de produtos mapeados")
    void listarTodos_deveRetornarListaMapeada() {
        Produto produto = criarProduto(1L, "P-001", "Bolo", new BigDecimal("50.00"));
        ProdutoResponse response = criarResponse(1L, "P-001", "Bolo");

        when(produtoRepository.findAll()).thenReturn(List.of(produto));
        when(produtoMapper.paraResponse(produto)).thenReturn(response);

        List<ProdutoResponse> resultado = produtoService.listarTodos();

        assertThat(resultado).hasSize(1).containsExactly(response);
    }


    @Test
    @DisplayName("buscarPorId: deve retornar response quando encontra")
    void buscarPorId_deveRetornarResponse() {
        Produto produto = criarProduto(1L, "P-001", "Bolo", new BigDecimal("50.00"));
        ProdutoResponse response = criarResponse(1L, "P-001", "Bolo");

        when(produtoRepository.buscarPorIdComComposicao(1L)).thenReturn(Optional.of(produto));
        when(produtoMapper.paraResponse(produto)).thenReturn(response);

        ProdutoResponse resultado = produtoService.buscarPorId(1L);

        assertThat(resultado).isEqualTo(response);
    }

    @Test
    @DisplayName("buscarPorId: deve lançar RecursoNaoEncontradoException quando não encontra")
    void buscarPorId_deveLancarExcecao() {
        when(produtoRepository.buscarPorIdComComposicao(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> produtoService.buscarPorId(999L))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessageContaining("999");
    }


    @Test
    @DisplayName("criar: deve salvar produto com composição e retornar response")
    void criar_deveSalvarComSucesso() {
        MateriaPrima mp = criarMateriaPrima(10L, "MP-001");
        CriarProdutoRequest request = new CriarProdutoRequest(
                "P-001", "Bolo", new BigDecimal("50.00"),
                List.of(new ItemComposicaoProdutoRequest(10L, new BigDecimal("2")))
        );
        Produto produtoSalvo = criarProduto(1L, "P-001", "Bolo", new BigDecimal("50.00"));
        ProdutoResponse response = criarResponse(1L, "P-001", "Bolo");

        when(produtoRepository.existsByCodigo("P-001")).thenReturn(false);
        when(materiaPrimaRepository.findAllById(List.of(10L))).thenReturn(List.of(mp));
        when(produtoRepository.save(any(Produto.class))).thenReturn(produtoSalvo);
        when(produtoRepository.buscarPorIdComComposicao(1L)).thenReturn(Optional.of(produtoSalvo));
        when(produtoMapper.paraResponse(produtoSalvo)).thenReturn(response);

        ProdutoResponse resultado = produtoService.criar(request);

        assertThat(resultado).isEqualTo(response);
        verify(produtoRepository).save(any(Produto.class));
    }

    @Test
    @DisplayName("criar: deve lançar RegraDeNegocioException quando código é duplicado")
    void criar_deveLancarExcecao_quandoCodigoDuplicado() {
        CriarProdutoRequest request = new CriarProdutoRequest(
                "P-001", "Bolo", new BigDecimal("50.00"),
                List.of(new ItemComposicaoProdutoRequest(10L, new BigDecimal("2")))
        );

        when(produtoRepository.existsByCodigo("P-001")).thenReturn(true);

        assertThatThrownBy(() -> produtoService.criar(request))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("codigo");

        verify(produtoRepository, never()).save(any());
    }

    @Test
    @DisplayName("criar: deve lançar exceção quando composição tem matéria-prima repetida")
    void criar_deveLancarExcecao_quandoMateriaPrimaRepetida() {
        CriarProdutoRequest request = new CriarProdutoRequest(
                "P-001", "Bolo", new BigDecimal("50.00"),
                List.of(
                        new ItemComposicaoProdutoRequest(10L, new BigDecimal("2")),
                        new ItemComposicaoProdutoRequest(10L, new BigDecimal("3"))
                )
        );

        when(produtoRepository.existsByCodigo("P-001")).thenReturn(false);

        assertThatThrownBy(() -> produtoService.criar(request))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("repetida");
    }

    @Test
    @DisplayName("criar: deve lançar exceção quando matéria-prima da composição não existe")
    void criar_deveLancarExcecao_quandoMateriaPrimaNaoExiste() {
        CriarProdutoRequest request = new CriarProdutoRequest(
                "P-001", "Bolo", new BigDecimal("50.00"),
                List.of(new ItemComposicaoProdutoRequest(999L, new BigDecimal("2")))
        );

        when(produtoRepository.existsByCodigo("P-001")).thenReturn(false);
        when(materiaPrimaRepository.findAllById(List.of(999L))).thenReturn(List.of());

        assertThatThrownBy(() -> produtoService.criar(request))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessageContaining("999");
    }


    @Test
    @DisplayName("atualizar: deve atualizar produto com sucesso")
    void atualizar_deveAtualizarComSucesso() {
        MateriaPrima mp = criarMateriaPrima(10L, "MP-001");
        Produto produtoExistente = criarProduto(1L, "P-001", "Bolo", new BigDecimal("50.00"));
        AtualizarProdutoRequest request = new AtualizarProdutoRequest(
                "P-001-EDIT", "Bolo Premium", new BigDecimal("80.00"),
                List.of(new ItemComposicaoProdutoRequest(10L, new BigDecimal("3")))
        );
        ProdutoResponse response = criarResponse(1L, "P-001-EDIT", "Bolo Premium");

        when(produtoRepository.buscarPorIdComComposicao(1L)).thenReturn(Optional.of(produtoExistente));
        when(produtoRepository.existsByCodigoAndIdNot("P-001-EDIT", 1L)).thenReturn(false);
        when(materiaPrimaRepository.findAllById(List.of(10L))).thenReturn(List.of(mp));
        when(produtoRepository.saveAndFlush(produtoExistente)).thenReturn(produtoExistente);
        when(produtoRepository.save(produtoExistente)).thenReturn(produtoExistente);
        when(produtoMapper.paraResponse(produtoExistente)).thenReturn(response);

        ProdutoResponse resultado = produtoService.atualizar(1L, request);

        assertThat(resultado.codigo()).isEqualTo("P-001-EDIT");
    }


    @Test
    @DisplayName("remover: deve deletar produto existente")
    void remover_deveDeletarComSucesso() {
        Produto produto = criarProduto(1L, "P-001", "Bolo", new BigDecimal("50.00"));
        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));

        produtoService.remover(1L);

        verify(produtoRepository).delete(produto);
    }

    @Test
    @DisplayName("remover: deve lançar exceção quando produto não existe")
    void remover_deveLancarExcecao_quandoNaoExiste() {
        when(produtoRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> produtoService.remover(999L))
                .isInstanceOf(RecursoNaoEncontradoException.class);

        verify(produtoRepository, never()).delete(any());
    }
}
