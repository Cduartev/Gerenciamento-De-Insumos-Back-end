package com.projeto.gerenciamento.de.insumos.service;

import com.projeto.gerenciamento.de.insumos.domain.entity.MateriaPrima;
import com.projeto.gerenciamento.de.insumos.domain.enumtype.UnidadeMedida;
import com.projeto.gerenciamento.de.insumos.domain.repository.MateriaPrimaRepository;
import com.projeto.gerenciamento.de.insumos.dto.materiaprima.AtualizarMateriaPrimaRequest;
import com.projeto.gerenciamento.de.insumos.dto.materiaprima.CriarMateriaPrimaRequest;
import com.projeto.gerenciamento.de.insumos.dto.materiaprima.MateriaPrimaResponse;
import com.projeto.gerenciamento.de.insumos.exception.RecursoNaoEncontradoException;
import com.projeto.gerenciamento.de.insumos.exception.RegraDeNegocioException;
import com.projeto.gerenciamento.de.insumos.mapper.MateriaPrimaMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MateriaPrimaService — CRUD de Matéria-Prima")
class MateriaPrimaServiceTest {

    @Mock
    private MateriaPrimaRepository materiaPrimaRepository;

    @Mock
    private MateriaPrimaMapper materiaPrimaMapper;

    @InjectMocks
    private MateriaPrimaService materiaPrimaService;


    private MateriaPrima criarEntidade(long id, String codigo, String nome) {
        return MateriaPrima.builder()
                .id(id)
                .codigo(codigo)
                .nome(nome)
                .quantidadeEstoque(new BigDecimal("100"))
                .unidadeMedida(UnidadeMedida.QUILOGRAMA)
                .build();
    }

    private MateriaPrimaResponse criarResponse(long id, String codigo, String nome) {
        return new MateriaPrimaResponse(id, codigo, nome, new BigDecimal("100"), UnidadeMedida.QUILOGRAMA);
    }


    @Test
    @DisplayName("listarTodas: deve retornar lista mapeada de matérias-primas")
    void listarTodas_deveRetornarListaMapeada() {
        MateriaPrima entidade = criarEntidade(1L, "MP-001", "Farinha");
        MateriaPrimaResponse response = criarResponse(1L, "MP-001", "Farinha");

        when(materiaPrimaRepository.findAll()).thenReturn(List.of(entidade));
        when(materiaPrimaMapper.paraResponse(entidade)).thenReturn(response);

        List<MateriaPrimaResponse> resultado = materiaPrimaService.listarTodas();

        assertThat(resultado).hasSize(1).containsExactly(response);
        verify(materiaPrimaRepository).findAll();
    }


    @Test
    @DisplayName("buscarPorId: deve retornar response quando encontra")
    void buscarPorId_deveRetornarResponse_quandoEncontra() {
        MateriaPrima entidade = criarEntidade(1L, "MP-001", "Farinha");
        MateriaPrimaResponse response = criarResponse(1L, "MP-001", "Farinha");

        when(materiaPrimaRepository.findById(1L)).thenReturn(Optional.of(entidade));
        when(materiaPrimaMapper.paraResponse(entidade)).thenReturn(response);

        MateriaPrimaResponse resultado = materiaPrimaService.buscarPorId(1L);

        assertThat(resultado).isEqualTo(response);
    }

    @Test
    @DisplayName("buscarPorId: deve lançar RecursoNaoEncontradoException quando não encontra")
    void buscarPorId_deveLancarExcecao_quandoNaoEncontra() {
        when(materiaPrimaRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> materiaPrimaService.buscarPorId(999L))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessageContaining("999");
    }


    @Test
    @DisplayName("criar: deve salvar e retornar response quando código é válido")
    void criar_deveSalvarERetornar_quandoCodigoValido() {
        CriarMateriaPrimaRequest request = new CriarMateriaPrimaRequest(
                "MP-001", "Farinha", new BigDecimal("100"), UnidadeMedida.QUILOGRAMA
        );
        MateriaPrima entidade = criarEntidade(1L, "MP-001", "Farinha");
        MateriaPrimaResponse response = criarResponse(1L, "MP-001", "Farinha");

        when(materiaPrimaRepository.existsByCodigo("MP-001")).thenReturn(false);
        when(materiaPrimaMapper.paraEntidade(request)).thenReturn(entidade);
        when(materiaPrimaRepository.save(entidade)).thenReturn(entidade);
        when(materiaPrimaMapper.paraResponse(entidade)).thenReturn(response);

        MateriaPrimaResponse resultado = materiaPrimaService.criar(request);

        assertThat(resultado).isEqualTo(response);
        verify(materiaPrimaRepository).save(entidade);
    }

    @Test
    @DisplayName("criar: deve lançar RegraDeNegocioException quando código é duplicado")
    void criar_deveLancarExcecao_quandoCodigoDuplicado() {
        CriarMateriaPrimaRequest request = new CriarMateriaPrimaRequest(
                "MP-001", "Farinha", new BigDecimal("100"), UnidadeMedida.QUILOGRAMA
        );

        when(materiaPrimaRepository.existsByCodigo("MP-001")).thenReturn(true);

        assertThatThrownBy(() -> materiaPrimaService.criar(request))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("codigo");

        verify(materiaPrimaRepository, never()).save(any());
    }


    @Test
    @DisplayName("atualizar: deve atualizar e retornar response com sucesso")
    void atualizar_deveAtualizarERetornar() {
        MateriaPrima entidadeExistente = criarEntidade(1L, "MP-001", "Farinha");
        AtualizarMateriaPrimaRequest request = new AtualizarMateriaPrimaRequest(
                "MP-001-EDIT", "Farinha Especial", new BigDecimal("200"), UnidadeMedida.QUILOGRAMA
        );
        MateriaPrimaResponse response = criarResponse(1L, "MP-001-EDIT", "Farinha Especial");

        when(materiaPrimaRepository.findById(1L)).thenReturn(Optional.of(entidadeExistente));
        when(materiaPrimaRepository.existsAllByCodigoAndIdNot("MP-001-EDIT", 1L)).thenReturn(false);
        when(materiaPrimaRepository.save(entidadeExistente)).thenReturn(entidadeExistente);
        when(materiaPrimaMapper.paraResponse(entidadeExistente)).thenReturn(response);

        MateriaPrimaResponse resultado = materiaPrimaService.atualizar(1L, request);

        assertThat(resultado.codigo()).isEqualTo("MP-001-EDIT");
        verify(materiaPrimaRepository).save(entidadeExistente);
    }

    @Test
    @DisplayName("atualizar: deve lançar exceção quando código já pertence a outra matéria-prima")
    void atualizar_deveLancarExcecao_quandoCodigoDuplicado() {
        MateriaPrima entidadeExistente = criarEntidade(1L, "MP-001", "Farinha");
        AtualizarMateriaPrimaRequest request = new AtualizarMateriaPrimaRequest(
                "MP-002", "Farinha", new BigDecimal("100"), UnidadeMedida.QUILOGRAMA
        );

        when(materiaPrimaRepository.findById(1L)).thenReturn(Optional.of(entidadeExistente));
        when(materiaPrimaRepository.existsAllByCodigoAndIdNot("MP-002", 1L)).thenReturn(true);

        assertThatThrownBy(() -> materiaPrimaService.atualizar(1L, request))
                .isInstanceOf(RegraDeNegocioException.class);

        verify(materiaPrimaRepository, never()).save(any());
    }


    @Test
    @DisplayName("remover: deve deletar matéria-prima existente com sucesso")
    void remover_deveDeletar_quandoExiste() {
        MateriaPrima entidade = criarEntidade(1L, "MP-001", "Farinha");
        when(materiaPrimaRepository.findById(1L)).thenReturn(Optional.of(entidade));

        materiaPrimaService.remover(1L);

        verify(materiaPrimaRepository).delete(entidade);
    }

    @Test
    @DisplayName("remover: deve lançar exceção quando matéria-prima não existe")
    void remover_deveLancarExcecao_quandoNaoExiste() {
        when(materiaPrimaRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> materiaPrimaService.remover(999L))
                .isInstanceOf(RecursoNaoEncontradoException.class);

        verify(materiaPrimaRepository, never()).delete(any());
    }
}
