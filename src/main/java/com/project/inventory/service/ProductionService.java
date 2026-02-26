package com.project.inventory.service;

import com.project.inventory.domain.entity.Product;
import com.project.inventory.domain.entity.ProductCompositionItem;
import com.project.inventory.domain.entity.RawMaterial;
import com.project.inventory.domain.repository.ProductRepository;
import com.project.inventory.domain.repository.RawMaterialRepository;
import com.project.inventory.dto.production.ProductionRequest;
import com.project.inventory.exception.BusinessRuleException;
import com.project.inventory.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductionService {

    private final ProductRepository productRepository;
    private final RawMaterialRepository rawMaterialRepository;

    @Transactional
    public void produce(ProductionRequest request) {
        if (request.quantity() <= 0) {
            throw new BusinessRuleException("A quantidade a ser produzida deve ser maior que zero.");
        }

        Product product = productRepository.findByIdWithComposition(request.productId())
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado para o id: " + request.productId()));

        if (product.getCompositionItems() == null || product.getCompositionItems().isEmpty()) {
            throw new BusinessRuleException("O produto não possui uma composição definida para produção.");
        }

        BigDecimal multiplier = BigDecimal.valueOf(request.quantity());

        // Pass 1: Validate stock for all items
        for (ProductCompositionItem item : product.getCompositionItems()) {
            RawMaterial rawMaterial = item.getRawMaterial();
            BigDecimal requiredConsumo = item.getRequiredQuantity().multiply(multiplier);

            if (rawMaterial.getStockQuantity().compareTo(requiredConsumo) < 0) {
                throw new BusinessRuleException(
                        String.format("Estoque insuficiente para a matéria-prima '%s' (%s). Necessário: %s, Disponível: %s",
                                rawMaterial.getName(), rawMaterial.getCode(), requiredConsumo, rawMaterial.getStockQuantity())
                );
            }
        }

        // Pass 2: Deduct stock
        List<RawMaterial> updatedRawMaterials = new ArrayList<>();
        for (ProductCompositionItem item : product.getCompositionItems()) {
            RawMaterial rawMaterial = item.getRawMaterial();
            BigDecimal requiredConsumo = item.getRequiredQuantity().multiply(multiplier);

            rawMaterial.setStockQuantity(rawMaterial.getStockQuantity().subtract(requiredConsumo));
            updatedRawMaterials.add(rawMaterial);
        }

        rawMaterialRepository.saveAll(updatedRawMaterials);
    }
}
