package com.project.inventory.service.optimization;

import com.project.inventory.domain.entity.ProductCompositionItem;
import com.project.inventory.domain.entity.RawMaterial;
import com.project.inventory.domain.entity.Product;
import com.project.inventory.domain.enumtype.UnitOfMeasurement;
import com.project.inventory.domain.repository.RawMaterialRepository;
import com.project.inventory.domain.repository.ProductRepository;
import com.project.inventory.dto.planproduction.ConsumoRawMaterialResponse;
import com.project.inventory.dto.planproduction.ProductionPlanItemResponse;
import com.project.inventory.dto.planproduction.SaldoRawMaterialResponse;
import com.project.inventory.dto.planproduction.ProductionPlanSuggestionResponse;
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
@DisplayName("ProductionPlanService — Lógica de Otimização")
class ProductionPlanServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private RawMaterialRepository rawMaterialRepository;

    @InjectMocks
    private ProductionPlanService productionPlanService;

    private RawMaterial createRawMaterial(long id, String code, String name,
                                          BigDecimal stock, UnitOfMeasurement unit) {
        return RawMaterial.builder()
                .id(id)
                .code(code)
                .name(name)
                .stockQuantity(stock)
                .unitOfMeasurement(unit)
                .build();
    }

    private Product createProduct(Long id, String code, String name, BigDecimal price) {
        return Product.builder()
                .id(id)
                .code(code)
                .name(name)
                .price(price)
                .compositionItems(new ArrayList<>())
                .build();
    }

    private void adicionarComposition(Product product, RawMaterial mp, BigDecimal requiredQtd) {
        ProductCompositionItem item = ProductCompositionItem.builder()
                .rawMaterial(mp)
                .requiredQuantity(requiredQtd)
                .product(product)
                .build();
        product.getCompositionItems().add(item);
    }

    @Test
    @DisplayName("Deve retornar plan vazio quando não há products cadastrados")
    void deveRetornarPlanVazio_quandoNaoHaProducts() {
        RawMaterial mp = createRawMaterial(1L, "MP-001", "Farinha", new BigDecimal("100"), UnitOfMeasurement.KILOGRAM);
        when(productRepository.findAll()).thenReturn(List.of());
        when(rawMaterialRepository.findAll()).thenReturn(List.of(mp));

        ProductionPlanSuggestionResponse response = productionPlanService.suggestOptimalPlan();

        assertThat(response.totalSalesValue()).isEqualByComparingTo(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        assertThat(response.totalProducedQuantity()).isZero();
        assertThat(response.suggestedItems()).isEmpty();
        assertThat(response.rawMaterialConsumptions()).isEmpty();
        assertThat(response.rawMaterialBalances()).hasSize(1);

        SaldoRawMaterialResponse saldo = response.rawMaterialBalances().get(0);
        assertThat(saldo.initialQuantity()).isEqualByComparingTo(new BigDecimal("100"));
        assertThat(saldo.consumedQuantity()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(saldo.balanceQuantity()).isEqualByComparingTo(new BigDecimal("100"));
    }

    @Test
    @DisplayName("Deve ignorar products sem composição válida")
    void deveIgnorarProductsSemComposition() {
        RawMaterial mp = createRawMaterial(1L, "MP-001", "Farinha", new BigDecimal("100"), UnitOfMeasurement.KILOGRAM);

        Product productWithoutComposition = createProduct(1L, "P-001", "Bolo Simples", new BigDecimal("50.00"));

        when(productRepository.findAll()).thenReturn(List.of(productWithoutComposition));
        when(rawMaterialRepository.findAll()).thenReturn(List.of(mp));

        ProductionPlanSuggestionResponse response = productionPlanService.suggestOptimalPlan();

        assertThat(response.totalProducedQuantity()).isZero();
        assertThat(response.suggestedItems()).isEmpty();
    }

    @Test
    @DisplayName("Deve calcular produção de um product simples com uma matéria-prima")
    void deveCalcularProductionProductSimples() {
        RawMaterial farinha = createRawMaterial(1L, "MP-001", "Farinha", new BigDecimal("10"), UnitOfMeasurement.KILOGRAM);

        Product bolo = createProduct(1L, "P-001", "Bolo", new BigDecimal("25.00"));
        adicionarComposition(bolo, farinha, new BigDecimal("2"));

        when(productRepository.findAll()).thenReturn(List.of(bolo));
        when(rawMaterialRepository.findAll()).thenReturn(List.of(farinha));

        ProductionPlanSuggestionResponse response = productionPlanService.suggestOptimalPlan();

        assertThat(response.totalProducedQuantity()).isEqualTo(5);
        assertThat(response.totalSalesValue()).isEqualByComparingTo(new BigDecimal("125.00"));
        assertThat(response.suggestedItems()).hasSize(1);

        ProductionPlanItemResponse item = response.suggestedItems().get(0);
        assertThat(item.productId()).isEqualTo(1L);
        assertThat(item.suggestedQuantity()).isEqualTo(5);
        assertThat(item.totalItemValue()).isEqualByComparingTo(new BigDecimal("125.00"));
    }

    @Test
    @DisplayName("Deve respeitar a matéria-prima limitante (gargalo) quando product tem múltiplas MPs")
    void deveRespeitarRawMaterialLimitante() {
        RawMaterial farinha = createRawMaterial(1L, "MP-001", "Farinha", new BigDecimal("20"), UnitOfMeasurement.KILOGRAM);
        RawMaterial acucar = createRawMaterial(2L, "MP-002", "Açúcar", new BigDecimal("3"), UnitOfMeasurement.KILOGRAM);

        Product bolo = createProduct(1L, "P-001", "Bolo", new BigDecimal("30.00"));
        adicionarComposition(bolo, farinha, new BigDecimal("2"));
        adicionarComposition(bolo, acucar, new BigDecimal("1"));

        when(productRepository.findAll()).thenReturn(List.of(bolo));
        when(rawMaterialRepository.findAll()).thenReturn(List.of(farinha, acucar));

        ProductionPlanSuggestionResponse response = productionPlanService.suggestOptimalPlan();

        assertThat(response.totalProducedQuantity()).isEqualTo(3);
        assertThat(response.totalSalesValue()).isEqualByComparingTo(new BigDecimal("90.00"));
    }

    @Test
    @DisplayName("Deve otimizar dois products que competem pela mesma matéria-prima para maximizar price")
    void deveOtimizarDoisProductsCompetindoPorMP() {
        RawMaterial farinha = createRawMaterial(1L, "MP-001", "Farinha", new BigDecimal("10"), UnitOfMeasurement.KILOGRAM);

        Product bolo = createProduct(1L, "P-001", "Bolo", new BigDecimal("50.00"));
        adicionarComposition(bolo, farinha, new BigDecimal("5"));

        Product pao = createProduct(2L, "P-002", "Pão", new BigDecimal("15.00"));
        adicionarComposition(pao, farinha, new BigDecimal("2"));

        when(productRepository.findAll()).thenReturn(List.of(bolo, pao));
        when(rawMaterialRepository.findAll()).thenReturn(List.of(farinha));

        ProductionPlanSuggestionResponse response = productionPlanService.suggestOptimalPlan();

        assertThat(response.totalSalesValue()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(response.totalProducedQuantity()).isEqualTo(2);
    }

    @Test
    @DisplayName("Deve desempatar por quantity quando price total é igual")
    void shouldTieBreakByQuantity_whenValueIsEqual() {
        RawMaterial farinha = createRawMaterial(1L, "MP-001", "Farinha", new BigDecimal("10"), UnitOfMeasurement.KILOGRAM);

        Product productA = createProduct(1L, "P-001", "Product A", new BigDecimal("50.00"));
        adicionarComposition(productA, farinha, new BigDecimal("5"));

        Product productB = createProduct(2L, "P-002", "Product B", new BigDecimal("20.00"));
        adicionarComposition(productB, farinha, new BigDecimal("2"));

        when(productRepository.findAll()).thenReturn(List.of(productA, productB));
        when(rawMaterialRepository.findAll()).thenReturn(List.of(farinha));

        ProductionPlanSuggestionResponse response = productionPlanService.suggestOptimalPlan();

        assertThat(response.totalSalesValue()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(response.totalProducedQuantity()).isEqualTo(5);
    }

    @Test
    @DisplayName("Deve retornar produção zerada quando stock é zero")
    void deveRetornarProductionZerada_quandoStockZero() {
        RawMaterial farinha = createRawMaterial(1L, "MP-001", "Farinha", BigDecimal.ZERO, UnitOfMeasurement.KILOGRAM);

        Product bolo = createProduct(1L, "P-001", "Bolo", new BigDecimal("25.00"));
        adicionarComposition(bolo, farinha, new BigDecimal("2"));

        when(productRepository.findAll()).thenReturn(List.of(bolo));
        when(rawMaterialRepository.findAll()).thenReturn(List.of(farinha));

        ProductionPlanSuggestionResponse response = productionPlanService.suggestOptimalPlan();

        assertThat(response.totalProducedQuantity()).isZero();
        assertThat(response.totalSalesValue()).isEqualByComparingTo(BigDecimal.ZERO.setScale(2));
        assertThat(response.suggestedItems()).isEmpty();
    }

    @Test
    @DisplayName("Deve calcular consumos e saldos corretamente nos DTOs de response")
    void shouldCalculateConsumptionsAndBalancesCorrectly() {
        RawMaterial farinha = createRawMaterial(1L, "MP-001", "Farinha", new BigDecimal("10"), UnitOfMeasurement.KILOGRAM);
        RawMaterial acucar = createRawMaterial(2L, "MP-002", "Açúcar", new BigDecimal("5"), UnitOfMeasurement.KILOGRAM);

        Product bolo = createProduct(1L, "P-001", "Bolo", new BigDecimal("30.00"));
        adicionarComposition(bolo, farinha, new BigDecimal("2"));
        adicionarComposition(bolo, acucar, new BigDecimal("1"));

        when(productRepository.findAll()).thenReturn(List.of(bolo));
        when(rawMaterialRepository.findAll()).thenReturn(List.of(farinha, acucar));

        ProductionPlanSuggestionResponse response = productionPlanService.suggestOptimalPlan();

        assertThat(response.totalProducedQuantity()).isEqualTo(5);

        assertThat(response.rawMaterialConsumptions()).hasSize(2);

        ConsumoRawMaterialResponse consumoFarinha = response.rawMaterialConsumptions().stream()
                .filter(c -> c.codeRawMaterial().equals("MP-001"))
                .findFirst().orElseThrow();
        assertThat(consumoFarinha.consumedQuantity()).isEqualByComparingTo(new BigDecimal("10"));

        ConsumoRawMaterialResponse consumoAcucar = response.rawMaterialConsumptions().stream()
                .filter(c -> c.codeRawMaterial().equals("MP-002"))
                .findFirst().orElseThrow();
        assertThat(consumoAcucar.consumedQuantity()).isEqualByComparingTo(new BigDecimal("5"));

        assertThat(response.rawMaterialBalances()).hasSize(2);

        SaldoRawMaterialResponse saldoFarinha = response.rawMaterialBalances().stream()
                .filter(s -> s.codeRawMaterial().equals("MP-001"))
                .findFirst().orElseThrow();
        assertThat(saldoFarinha.initialQuantity()).isEqualByComparingTo(new BigDecimal("10"));
        assertThat(saldoFarinha.consumedQuantity()).isEqualByComparingTo(new BigDecimal("10"));
        assertThat(saldoFarinha.balanceQuantity()).isEqualByComparingTo(BigDecimal.ZERO);

        SaldoRawMaterialResponse saldoAcucar = response.rawMaterialBalances().stream()
                .filter(s -> s.codeRawMaterial().equals("MP-002"))
                .findFirst().orElseThrow();
        assertThat(saldoAcucar.initialQuantity()).isEqualByComparingTo(new BigDecimal("5"));
        assertThat(saldoAcucar.consumedQuantity()).isEqualByComparingTo(new BigDecimal("5"));
        assertThat(saldoAcucar.balanceQuantity()).isEqualByComparingTo(BigDecimal.ZERO);
    }
}
