package com.project.inventory.dto.product;

import java.math.BigDecimal;
import java.util.List;

public record ProductResponse(
        Long id,
        String code,
        String name,
        BigDecimal price,
        List<ProductCompositionItemResponse> compositionItems
) {
}
