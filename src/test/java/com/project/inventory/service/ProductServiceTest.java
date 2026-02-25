package com.project.inventory.service;

import com.project.inventory.domain.entity.RawMaterial;
import com.project.inventory.domain.entity.Product;
import com.project.inventory.domain.enumtype.UnitOfMeasurement;
import com.project.inventory.domain.repository.RawMaterialRepository;
import com.project.inventory.domain.repository.ProductRepository;
import com.project.inventory.dto.product.*;
import com.project.inventory.exception.ResourceNotFoundException;
import com.project.inventory.exception.BusinessRuleException;
import com.project.inventory.mapper.ProductMapper;
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
@DisplayName("ProductService — CRUD de Product")
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private RawMaterialRepository rawMaterialRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductService productService;


    private Product createProduct(Long id, String code, String name, BigDecimal price) {
        return Product.builder()
                .id(id)
                .code(code)
                .name(name)
                .price(price)
                .compositionItems(new ArrayList<>())
                .build();
    }

    private RawMaterial createRawMaterial(long id, String code) {
        return RawMaterial.builder()
                .id(id)
                .code(code)
                .name("MP " + code)
                .stockQuantity(new BigDecimal("100"))
                .unitOfMeasurement(UnitOfMeasurement.KILOGRAM)
                .build();
    }

    private ProductResponse createResponse(Long id, String code, String name) {
        return new ProductResponse(id, code, name, new BigDecimal("50.00"), List.of());
    }


    @Test
    @DisplayName("findAll: deve retornar lista de products mapeados")
    void findAll_deveRetornarListaMapeada() {
        Product product = createProduct(1L, "P-001", "Bolo", new BigDecimal("50.00"));
        ProductResponse response = createResponse(1L, "P-001", "Bolo");

        when(productRepository.findAll()).thenReturn(List.of(product));
        when(productMapper.toResponse(product)).thenReturn(response);

        List<ProductResponse> result = productService.findAll();

        assertThat(result).hasSize(1).containsExactly(response);
    }


    @Test
    @DisplayName("findById: deve retornar response quando encontra")
    void findById_deveRetornarResponse() {
        Product product = createProduct(1L, "P-001", "Bolo", new BigDecimal("50.00"));
        ProductResponse response = createResponse(1L, "P-001", "Bolo");

        when(productRepository.findByIdWithComposition(1L)).thenReturn(Optional.of(product));
        when(productMapper.toResponse(product)).thenReturn(response);

        ProductResponse result = productService.findById(1L);

        assertThat(result).isEqualTo(response);
    }

    @Test
    @DisplayName("findById: deve lançar ResourceNotFoundException quando não encontra")
    void findById_deveLancarExcecao() {
        when(productRepository.findByIdWithComposition(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.findById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }


    @Test
    @DisplayName("create: deve savedr product com composição e retornar response")
    void create_deveSalvarComSucesso() {
        RawMaterial mp = createRawMaterial(10L, "MP-001");
        CreateProductRequest request = new CreateProductRequest(
                "P-001", "Bolo", new BigDecimal("50.00"),
                List.of(new ProductCompositionItemRequest(10L, new BigDecimal("2")))
        );
        Product productSalvo = createProduct(1L, "P-001", "Bolo", new BigDecimal("50.00"));
        ProductResponse response = createResponse(1L, "P-001", "Bolo");

        when(productRepository.existsByCode("P-001")).thenReturn(false);
        when(rawMaterialRepository.findAllById(List.of(10L))).thenReturn(List.of(mp));
        when(productRepository.save(any(Product.class))).thenReturn(productSalvo);
        when(productRepository.findByIdWithComposition(1L)).thenReturn(Optional.of(productSalvo));
        when(productMapper.toResponse(productSalvo)).thenReturn(response);

        ProductResponse result = productService.create(request);

        assertThat(result).isEqualTo(response);
        verify(productRepository).save(any(Product.class));
    }

    @Test
    @DisplayName("create: deve lançar BusinessRuleException quando código é duplicado")
    void create_deveLancarExcecao_quandoCodigoDuplicado() {
        CreateProductRequest request = new CreateProductRequest(
                "P-001", "Bolo", new BigDecimal("50.00"),
                List.of(new ProductCompositionItemRequest(10L, new BigDecimal("2")))
        );

        when(productRepository.existsByCode("P-001")).thenReturn(true);

        assertThatThrownBy(() -> productService.create(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("code");

        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("create: deve lançar exceção quando composição tem matéria-prima repetida")
    void create_deveLancarExcecao_quandoRawMaterialRepetida() {
        CreateProductRequest request = new CreateProductRequest(
                "P-001", "Bolo", new BigDecimal("50.00"),
                List.of(
                        new ProductCompositionItemRequest(10L, new BigDecimal("2")),
                        new ProductCompositionItemRequest(10L, new BigDecimal("3"))
                )
        );

        when(productRepository.existsByCode("P-001")).thenReturn(false);

        assertThatThrownBy(() -> productService.create(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("repetida");
    }

    @Test
    @DisplayName("create: deve lançar exceção quando matéria-prima da composição não existe")
    void create_deveLancarExcecao_quandoRawMaterialNaoExiste() {
        CreateProductRequest request = new CreateProductRequest(
                "P-001", "Bolo", new BigDecimal("50.00"),
                List.of(new ProductCompositionItemRequest(999L, new BigDecimal("2")))
        );

        when(productRepository.existsByCode("P-001")).thenReturn(false);
        when(rawMaterialRepository.findAllById(List.of(999L))).thenReturn(List.of());

        assertThatThrownBy(() -> productService.create(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }


    @Test
    @DisplayName("update: deve update product com sucesso")
    void update_deveAtualizarComSucesso() {
        RawMaterial mp = createRawMaterial(10L, "MP-001");
        Product productExistente = createProduct(1L, "P-001", "Bolo", new BigDecimal("50.00"));
        UpdateProductRequest request = new UpdateProductRequest(
                "P-001-EDIT", "Bolo Premium", new BigDecimal("80.00"),
                List.of(new ProductCompositionItemRequest(10L, new BigDecimal("3")))
        );
        ProductResponse response = createResponse(1L, "P-001-EDIT", "Bolo Premium");

        when(productRepository.findByIdWithComposition(1L)).thenReturn(Optional.of(productExistente));
        when(productRepository.existsByCodeAndIdNot("P-001-EDIT", 1L)).thenReturn(false);
        when(rawMaterialRepository.findAllById(List.of(10L))).thenReturn(List.of(mp));
        when(productRepository.saveAndFlush(productExistente)).thenReturn(productExistente);
        when(productRepository.save(productExistente)).thenReturn(productExistente);
        when(productMapper.toResponse(productExistente)).thenReturn(response);

        ProductResponse result = productService.update(1L, request);

        assertThat(result.code()).isEqualTo("P-001-EDIT");
    }


    @Test
    @DisplayName("delete: deve deletar product existente")
    void delete_deveDeletarComSucesso() {
        Product product = createProduct(1L, "P-001", "Bolo", new BigDecimal("50.00"));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        productService.delete(1L);

        verify(productRepository).delete(product);
    }

    @Test
    @DisplayName("delete: deve lançar exceção quando product não existe")
    void delete_deveLancarExcecao_quandoNaoExiste() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.delete(999L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(productRepository, never()).delete(any());
    }
}
