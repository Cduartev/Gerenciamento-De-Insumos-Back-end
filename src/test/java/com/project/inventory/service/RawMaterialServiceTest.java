package com.project.inventory.service;

import com.project.inventory.domain.entity.RawMaterial;
import com.project.inventory.domain.enumtype.UnitOfMeasurement;
import com.project.inventory.domain.repository.RawMaterialRepository;
import com.project.inventory.dto.rawmaterial.UpdateRawMaterialRequest;
import com.project.inventory.dto.rawmaterial.CreateRawMaterialRequest;
import com.project.inventory.dto.rawmaterial.RawMaterialResponse;
import com.project.inventory.exception.ResourceNotFoundException;
import com.project.inventory.exception.BusinessRuleException;
import com.project.inventory.mapper.RawMaterialMapper;
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
@DisplayName("RawMaterialService — CRUD de Matéria-Prima")
class RawMaterialServiceTest {

    @Mock
    private RawMaterialRepository rawMaterialRepository;

    @Mock
    private RawMaterialMapper rawMaterialMapper;

    @InjectMocks
    private RawMaterialService rawMaterialService;


    private RawMaterial createEntity(long id, String code, String name) {
        return RawMaterial.builder()
                .id(id)
                .code(code)
                .name(name)
                .stockQuantity(new BigDecimal("100"))
                .unitOfMeasurement(UnitOfMeasurement.KILOGRAM)
                .build();
    }

    private RawMaterialResponse createResponse(long id, String code, String name) {
        return new RawMaterialResponse(id, code, name, new BigDecimal("100"), UnitOfMeasurement.KILOGRAM);
    }


    @Test
    @DisplayName("findAll: deve retornar lista mapeada de matérias-primas")
    void findAll_deveRetornarListaMapeada() {
        RawMaterial entidade = createEntity(1L, "MP-001", "Farinha");
        RawMaterialResponse response = createResponse(1L, "MP-001", "Farinha");

        when(rawMaterialRepository.findAll()).thenReturn(List.of(entidade));
        when(rawMaterialMapper.toResponse(entidade)).thenReturn(response);

        List<RawMaterialResponse> result = rawMaterialService.findAll();

        assertThat(result).hasSize(1).containsExactly(response);
        verify(rawMaterialRepository).findAll();
    }


    @Test
    @DisplayName("findById: deve retornar response quando encontra")
    void findById_deveRetornarResponse_quandoEncontra() {
        RawMaterial entidade = createEntity(1L, "MP-001", "Farinha");
        RawMaterialResponse response = createResponse(1L, "MP-001", "Farinha");

        when(rawMaterialRepository.findById(1L)).thenReturn(Optional.of(entidade));
        when(rawMaterialMapper.toResponse(entidade)).thenReturn(response);

        RawMaterialResponse result = rawMaterialService.findById(1L);

        assertThat(result).isEqualTo(response);
    }

    @Test
    @DisplayName("findById: deve lançar ResourceNotFoundException quando não encontra")
    void findById_deveLancarExcecao_quandoNaoEncontra() {
        when(rawMaterialRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> rawMaterialService.findById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }


    @Test
    @DisplayName("create: deve savedr e retornar response quando código é válido")
    void create_deveSalvarERetornar_quandoCodigoValido() {
        CreateRawMaterialRequest request = new CreateRawMaterialRequest(
                "MP-001", "Farinha", new BigDecimal("100"), UnitOfMeasurement.KILOGRAM
        );
        RawMaterial entidade = createEntity(1L, "MP-001", "Farinha");
        RawMaterialResponse response = createResponse(1L, "MP-001", "Farinha");

        when(rawMaterialRepository.existsByCode("MP-001")).thenReturn(false);
        when(rawMaterialMapper.toEntity(request)).thenReturn(entidade);
        when(rawMaterialRepository.save(entidade)).thenReturn(entidade);
        when(rawMaterialMapper.toResponse(entidade)).thenReturn(response);

        RawMaterialResponse result = rawMaterialService.create(request);

        assertThat(result).isEqualTo(response);
        verify(rawMaterialRepository).save(entidade);
    }

    @Test
    @DisplayName("create: deve lançar BusinessRuleException quando código é duplicado")
    void create_deveLancarExcecao_quandoCodigoDuplicado() {
        CreateRawMaterialRequest request = new CreateRawMaterialRequest(
                "MP-001", "Farinha", new BigDecimal("100"), UnitOfMeasurement.KILOGRAM
        );

        when(rawMaterialRepository.existsByCode("MP-001")).thenReturn(true);

        assertThatThrownBy(() -> rawMaterialService.create(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("code");

        verify(rawMaterialRepository, never()).save(any());
    }


    @Test
    @DisplayName("update: deve update e retornar response com sucesso")
    void update_deveAtualizarERetornar() {
        RawMaterial entidadeExistente = createEntity(1L, "MP-001", "Farinha");
        UpdateRawMaterialRequest request = new UpdateRawMaterialRequest(
                "MP-001-EDIT", "Farinha Especial", new BigDecimal("200"), UnitOfMeasurement.KILOGRAM
        );
        RawMaterialResponse response = createResponse(1L, "MP-001-EDIT", "Farinha Especial");

        when(rawMaterialRepository.findById(1L)).thenReturn(Optional.of(entidadeExistente));
        when(rawMaterialRepository.existsAllByCodeAndIdNot("MP-001-EDIT", 1L)).thenReturn(false);
        when(rawMaterialRepository.save(entidadeExistente)).thenReturn(entidadeExistente);
        when(rawMaterialMapper.toResponse(entidadeExistente)).thenReturn(response);

        RawMaterialResponse result = rawMaterialService.update(1L, request);

        assertThat(result.code()).isEqualTo("MP-001-EDIT");
        verify(rawMaterialRepository).save(entidadeExistente);
    }

    @Test
    @DisplayName("update: deve lançar exceção quando código já pertence a outra matéria-prima")
    void update_deveLancarExcecao_quandoCodigoDuplicado() {
        RawMaterial entidadeExistente = createEntity(1L, "MP-001", "Farinha");
        UpdateRawMaterialRequest request = new UpdateRawMaterialRequest(
                "MP-002", "Farinha", new BigDecimal("100"), UnitOfMeasurement.KILOGRAM
        );

        when(rawMaterialRepository.findById(1L)).thenReturn(Optional.of(entidadeExistente));
        when(rawMaterialRepository.existsAllByCodeAndIdNot("MP-002", 1L)).thenReturn(true);

        assertThatThrownBy(() -> rawMaterialService.update(1L, request))
                .isInstanceOf(BusinessRuleException.class);

        verify(rawMaterialRepository, never()).save(any());
    }


    @Test
    @DisplayName("delete: deve deletar matéria-prima existente com sucesso")
    void delete_deveDeletar_quandoExiste() {
        RawMaterial entidade = createEntity(1L, "MP-001", "Farinha");
        when(rawMaterialRepository.findById(1L)).thenReturn(Optional.of(entidade));

        rawMaterialService.delete(1L);

        verify(rawMaterialRepository).delete(entidade);
    }

    @Test
    @DisplayName("delete: deve lançar exceção quando matéria-prima não existe")
    void delete_deveLancarExcecao_quandoNaoExiste() {
        when(rawMaterialRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> rawMaterialService.delete(999L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(rawMaterialRepository, never()).delete(any());
    }
}
