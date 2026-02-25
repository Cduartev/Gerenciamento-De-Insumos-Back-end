package com.project.inventory.mapper;

import com.project.inventory.domain.entity.ProductCompositionItem;
import com.project.inventory.domain.entity.Product;
import com.project.inventory.dto.product.ProductCompositionItemResponse;
import com.project.inventory.dto.product.ProductResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProductMapper {

    public ProductResponse toResponse(Product product) {
        List<ProductCompositionItemResponse> itens = product.getCompositionItems()
                .stream()
                .map(this::paraItemResponse)
                .toList();

        return new ProductResponse(
                product.getId(),
                product.getCode(),
                product.getName(),
                product.getPrice(),
                itens
        );
    }

    private ProductCompositionItemResponse paraItemResponse(ProductCompositionItem item) {
        return new ProductCompositionItemResponse(
                item.getId(),
                item.getRawMaterial().getId(),
                item.getRawMaterial().getCode(),
                item.getRawMaterial().getName(),
                item.getRawMaterial().getUnitOfMeasurement(),
                item.getRequiredQuantity()
        );
    }
}
