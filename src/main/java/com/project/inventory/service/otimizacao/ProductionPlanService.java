package com.project.inventory.service.optimization;

import com.project.inventory.domain.entity.ProductCompositionItem;
import com.project.inventory.domain.entity.RawMaterial;
import com.project.inventory.domain.entity.Product;
import com.project.inventory.domain.repository.RawMaterialRepository;
import com.project.inventory.domain.repository.ProductRepository;
import com.project.inventory.dto.planproduction.ConsumoRawMaterialResponse;
import com.project.inventory.dto.planproduction.ProductionPlanItemResponse;
import com.project.inventory.dto.planproduction.SaldoRawMaterialResponse;
import com.project.inventory.dto.planproduction.ProductionPlanSuggestionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductionPlanService {

    private static final BigDecimal ZERO = BigDecimal.ZERO;

    private final ProductRepository productRepository;
    private final RawMaterialRepository rawMaterialRepository;

    @Transactional(readOnly = true)
    public ProductionPlanSuggestionResponse suggestOptimalPlan() {
        List<Product> products = productRepository.findAll()
                .stream()
                .filter(this::productTemCompositionValida)
                .sorted(Comparator
                        .comparing(Product::getPrice).reversed()
                        .thenComparing(Product::getCode))
                .toList();

        List<RawMaterial> rawMaterials = rawMaterialRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(RawMaterial::getCode))
                .toList();

        Map<Long, BigDecimal> stockInicial = createMapaStockInicial(rawMaterials);

        if (products.isEmpty()) {
            return montarRespostaSemProduction(rawMaterials, stockInicial);
        }

        SearchState state = new SearchState(products.size());
        Map<Long, BigDecimal> availableStock = new HashMap<>(stockInicial);

        findBestCombination(
                0,
                products,
                availableStock,
                state,
                ZERO,
                0
        );

        return montarResposta(products, rawMaterials, stockInicial, state.bestQuantities);
    }

    private boolean productTemCompositionValida(Product product) {
        return product.getCompositionItems() != null && !product.getCompositionItems().isEmpty();
    }

    private Map<Long, BigDecimal> createMapaStockInicial(List<RawMaterial> rawMaterials) {
        Map<Long, BigDecimal> stockInicial = new HashMap<>();

        for (RawMaterial rawMaterial : rawMaterials) {
            stockInicial.put(
                    rawMaterial.getId(),
                    priceNaoNulo(rawMaterial.getStockQuantity())
            );
        }

        return stockInicial;
    }

    private ProductionPlanSuggestionResponse montarRespostaSemProduction(
            List<RawMaterial> rawMaterials,
            Map<Long, BigDecimal> stockInicial
    ) {
        List<SaldoRawMaterialResponse> saldos = rawMaterials.stream()
                .map(rawMaterial -> new SaldoRawMaterialResponse(
                        rawMaterial.getId(),
                        rawMaterial.getCode(),
                        rawMaterial.getName(),
                        rawMaterial.getUnitOfMeasurement(),
                        stockInicial.getOrDefault(rawMaterial.getId(), ZERO),
                        ZERO,
                        stockInicial.getOrDefault(rawMaterial.getId(), ZERO)
                ))
                .toList();

        return new ProductionPlanSuggestionResponse(
                ZERO.setScale(2, RoundingMode.HALF_UP),
                0,
                List.of(),
                List.of(),
                saldos
        );
    }

    private void findBestCombination(
            int productIndex,
            List<Product> products,
            Map<Long, BigDecimal> availableStock,
            SearchState state,
            BigDecimal priceTotalAtual,
            int totalQuantityAtual
    ) {
        if (productIndex == products.size()) {
            evaluateBestSolution(state, priceTotalAtual, totalQuantityAtual);
            return;
        }

        Product productAtual = products.get(productIndex);
        int maximoUnits = calculateMaxProducibleUnits(productAtual, availableStock);

        for (int quantity = maximoUnits; quantity >= 0; quantity--) {
            state.currentQuantities[productIndex] = quantity;

            if (quantity > 0) {
                consumeRawMaterials(productAtual, quantity, availableStock);
            }

            BigDecimal novoPriceTotal = priceTotalAtual.add(
                    productAtual.getPrice().multiply(BigDecimal.valueOf(quantity))
            );
            int novaQuantityTotal = totalQuantityAtual + quantity;

            findBestCombination(
                    productIndex + 1,
                    products,
                    availableStock,
                    state,
                    novoPriceTotal,
                    novaQuantityTotal
            );

            if (quantity > 0) {
                returnRawMaterials(productAtual, quantity, availableStock);
            }
        }

        state.currentQuantities[productIndex] = 0;
    }

    private int calculateMaxProducibleUnits(Product product, Map<Long, BigDecimal> availableStock) {
        int maximo = Integer.MAX_VALUE;

        for (ProductCompositionItem item : product.getCompositionItems()) {
            Long rawMaterialId = item.getRawMaterial().getId();
            BigDecimal quantityDisponivel = availableStock.getOrDefault(rawMaterialId, ZERO);
            BigDecimal requiredQuantity = item.getRequiredQuantity();

            if (requiredQuantity == null || requiredQuantity.compareTo(ZERO) <= 0) {
                return 0;
            }

            int possivelComEssaRawMaterial = quantityDisponivel
                    .divide(requiredQuantity, 0, RoundingMode.DOWN)
                    .intValue();

            maximo = Math.min(maximo, possivelComEssaRawMaterial);

            if (maximo == 0) {
                return 0;
            }
        }

        return maximo == Integer.MAX_VALUE ? 0 : maximo;
    }

    private void consumeRawMaterials(
            Product product,
            int quantityProduct,
            Map<Long, BigDecimal> availableStock
    ) {
        BigDecimal multiplicador = BigDecimal.valueOf(quantityProduct);

        for (ProductCompositionItem item : product.getCompositionItems()) {
            Long rawMaterialId = item.getRawMaterial().getId();
            BigDecimal consumo = item.getRequiredQuantity().multiply(multiplicador);

            BigDecimal atual = availableStock.getOrDefault(rawMaterialId, ZERO);
            availableStock.put(rawMaterialId, atual.subtract(consumo));
        }
    }

    private void returnRawMaterials(
            Product product,
            int quantityProduct,
            Map<Long, BigDecimal> availableStock
    ) {
        BigDecimal multiplicador = BigDecimal.valueOf(quantityProduct);

        for (ProductCompositionItem item : product.getCompositionItems()) {
            Long rawMaterialId = item.getRawMaterial().getId();
            BigDecimal devolucao = item.getRequiredQuantity().multiply(multiplicador);

            BigDecimal atual = availableStock.getOrDefault(rawMaterialId, ZERO);
            availableStock.put(rawMaterialId, atual.add(devolucao));
        }
    }

    private void evaluateBestSolution(
            SearchState state,
            BigDecimal priceTotalAtual,
            int totalQuantityAtual
    ) {
        int valueComparison = priceTotalAtual.compareTo(state.bestTotalValue);

        boolean shouldReplaceBest = valueComparison > 0
                || (valueComparison == 0 && totalQuantityAtual > state.bestTotalQuantity);

        if (shouldReplaceBest) {
            state.bestTotalValue = priceTotalAtual;
            state.bestTotalQuantity = totalQuantityAtual;
            state.bestQuantities = Arrays.copyOf(state.currentQuantities, state.currentQuantities.length);
        }
    }

    private ProductionPlanSuggestionResponse montarResposta(
            List<Product> products,
            List<RawMaterial> rawMaterials,
            Map<Long, BigDecimal> stockInicial,
            int[] quantitysSugeridas
    ) {
        Map<Long, BigDecimal> stockFinal = new HashMap<>(stockInicial);

        List<ProductionPlanItemResponse> suggestedItems = new ArrayList<>();
        BigDecimal totalSalesValue = ZERO;
        int totalProducedQuantity = 0;

        for (int i = 0; i < products.size(); i++) {
            int quantity = quantitysSugeridas[i];

            if (quantity <= 0) {
                continue;
            }

            Product product = products.get(i);

            BigDecimal totalItemValue = product.getPrice().multiply(BigDecimal.valueOf(quantity));
            totalSalesValue = totalSalesValue.add(totalItemValue);
            totalProducedQuantity += quantity;

            suggestedItems.add(new ProductionPlanItemResponse(
                    product.getId(),
                    product.getCode(),
                    product.getName(),
                    quantity,
                    product.getPrice(),
                    totalItemValue
            ));

            aplicarConsumoNoMapaStockFinal(product, quantity, stockFinal);
        }

        List<ConsumoRawMaterialResponse> consumos = new ArrayList<>();
        List<SaldoRawMaterialResponse> saldos = new ArrayList<>();

        for (RawMaterial rawMaterial : rawMaterials) {
            Long id = rawMaterial.getId();
            BigDecimal initialQuantity = stockInicial.getOrDefault(id, ZERO);
            BigDecimal balanceQuantity = stockFinal.getOrDefault(id, ZERO);
            BigDecimal consumedQuantity = initialQuantity.subtract(balanceQuantity);

            if (consumedQuantity.compareTo(ZERO) > 0) {
                consumos.add(new ConsumoRawMaterialResponse(
                        rawMaterial.getId(),
                        rawMaterial.getCode(),
                        rawMaterial.getName(),
                        rawMaterial.getUnitOfMeasurement(),
                        consumedQuantity
                ));
            }

            saldos.add(new SaldoRawMaterialResponse(
                    rawMaterial.getId(),
                    rawMaterial.getCode(),
                    rawMaterial.getName(),
                    rawMaterial.getUnitOfMeasurement(),
                    initialQuantity,
                    consumedQuantity.max(ZERO),
                    balanceQuantity
            ));
        }

        return new ProductionPlanSuggestionResponse(
                totalSalesValue.setScale(2, RoundingMode.HALF_UP),
                totalProducedQuantity,
                suggestedItems,
                consumos,
                saldos
        );
    }

    private void aplicarConsumoNoMapaStockFinal(
            Product product,
            int quantityProduct,
            Map<Long, BigDecimal> stockFinal
    ) {
        BigDecimal multiplicador = BigDecimal.valueOf(quantityProduct);

        for (ProductCompositionItem item : product.getCompositionItems()) {
            Long rawMaterialId = item.getRawMaterial().getId();
            BigDecimal consumo = item.getRequiredQuantity().multiply(multiplicador);

            BigDecimal atual = stockFinal.getOrDefault(rawMaterialId, ZERO);
            stockFinal.put(rawMaterialId, atual.subtract(consumo));
        }
    }

    private BigDecimal priceNaoNulo(BigDecimal price) {
        return price == null ? ZERO : price;
    }

    private static class SearchState {
        private final int[] currentQuantities;
        private int[] bestQuantities;
        private BigDecimal bestTotalValue;
        private int bestTotalQuantity;

        private SearchState(int quantityProducts) {
            this.currentQuantities = new int[quantityProducts];
            this.bestQuantities = new int[quantityProducts];
            this.bestTotalValue = ZERO;
            this.bestTotalQuantity = 0;
        }
    }
}
