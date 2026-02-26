package com.project.inventory.service;

import com.project.inventory.domain.entity.Product;
import com.project.inventory.domain.entity.ProductCompositionItem;
import com.project.inventory.domain.entity.RawMaterial;
import com.project.inventory.domain.repository.ProductRepository;
import com.project.inventory.domain.repository.RawMaterialRepository;
import com.project.inventory.dto.production.ProductionRequest;
import com.project.inventory.exception.BusinessRuleException;
import com.project.inventory.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductionServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private RawMaterialRepository rawMaterialRepository;

    @InjectMocks
    private ProductionService productionService;

    private Product product;
    private RawMaterial roloMassa;
    private RawMaterial queijo;

    @BeforeEach
    void setUp() {
        roloMassa = RawMaterial.builder()
                .id(1L)
                .code("RM01")
                .name("Rolo de Massa")
                .stockQuantity(new BigDecimal("100.00"))
                .build();

        queijo = RawMaterial.builder()
                .id(2L)
                .code("RM02")
                .name("Queijo")
                .stockQuantity(new BigDecimal("50.00"))
                .build();

        product = Product.builder()
                .id(10L)
                .code("PROD01")
                .name("Pastel de Queijo")
                .build();

        ProductCompositionItem comp1 = ProductCompositionItem.builder()
                .id(100L)
                .rawMaterial(roloMassa)
                .requiredQuantity(new BigDecimal("1.50"))
                .build();

        ProductCompositionItem comp2 = ProductCompositionItem.builder()
                .id(101L)
                .rawMaterial(queijo)
                .requiredQuantity(new BigDecimal("2.00"))
                .build();

        product.adicionarItemComposition(comp1);
        product.adicionarItemComposition(comp2);
    }

    @Test
    void shouldProduceAndDeductRawMaterialsSuccessfully() {
        // Arrange
        ProductionRequest request = new ProductionRequest(10L, 10); // Produce 10 units
        
        when(productRepository.findByIdWithComposition(10L)).thenReturn(Optional.of(product));
        when(rawMaterialRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        assertDoesNotThrow(() -> productionService.produce(request));

        // Assert
        verify(productRepository, times(1)).findByIdWithComposition(10L);
        verify(rawMaterialRepository, times(1)).saveAll(any());
        
        // Initial stock roloMassa: 100.00, required: 1.50 * 10 = 15.00 -> Remaining: 85.00
        assertEquals(new BigDecimal("85.00"), roloMassa.getStockQuantity());
        
        // Initial stock queijo: 50.00, required: 2.00 * 10 = 20.00 -> Remaining: 30.00
        assertEquals(new BigDecimal("30.00"), queijo.getStockQuantity());
    }

    @Test
    void shouldThrowExceptionWhenStockIsInsufficient() {
        // Arrange
        // Producing 40 units of Pastel de Queijo
        // roloMassa required = 40 * 1.50 = 60.00 -> (Available 100) OK
        // queijo required = 40 * 2.00 = 80.00 -> (Available 50) INSUFICIENT!
        ProductionRequest request = new ProductionRequest(10L, 40); 
        
        when(productRepository.findByIdWithComposition(10L)).thenReturn(Optional.of(product));

        // Act & Assert
        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () -> {
            productionService.produce(request);
        });

        assertTrue(exception.getMessage().contains("Estoque insuficiente para a matéria-prima"));
        
        verify(productRepository, times(1)).findByIdWithComposition(10L);
        verify(rawMaterialRepository, never()).saveAll(any());
        
        // Stock should remain unchanged
        assertEquals(new BigDecimal("100.00"), roloMassa.getStockQuantity());
        assertEquals(new BigDecimal("50.00"), queijo.getStockQuantity());
    }

    @Test
    void shouldThrowExceptionWhenQuantityIsZeroOrLess() {
        ProductionRequest request = new ProductionRequest(10L, 0);

        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () -> {
            productionService.produce(request);
        });

        assertEquals("A quantidade a ser produzida deve ser maior que zero.", exception.getMessage());
        verify(productRepository, never()).findByIdWithComposition(any());
    }

    @Test
    void shouldThrowExceptionWhenProductNotFound() {
        ProductionRequest request = new ProductionRequest(99L, 10);
        
        when(productRepository.findByIdWithComposition(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            productionService.produce(request);
        });

        assertTrue(exception.getMessage().contains("Produto não encontrado"));
        verify(rawMaterialRepository, never()).saveAll(any());
    }

    @Test
    void shouldThrowExceptionWhenProductHasNoComposition() {
        Product emptyProduct = Product.builder().id(20L).build();
        ProductionRequest request = new ProductionRequest(20L, 10);

        when(productRepository.findByIdWithComposition(20L)).thenReturn(Optional.of(emptyProduct));

        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () -> {
            productionService.produce(request);
        });

        assertEquals("O produto não possui uma composição definida para produção.", exception.getMessage());
        verify(rawMaterialRepository, never()).saveAll(any());
    }
}
