package com.project.inventory.mapper;

import com.project.inventory.domain.entity.RawMaterial;
import com.project.inventory.dto.materiaprima.CreateRawMaterialRequest;
import com.project.inventory.dto.materiaprima.RawMaterialResponse;
import org.springframework.stereotype.Component;

@Component
public class RawMaterialMapper {

    public RawMaterial toEntity(CreateRawMaterialRequest request){
        return RawMaterial.builder()
                .code(request.code())
                .name(request.name())
                .stockQuantity(request.stockQuantity())
                .unitOfMeasurement(request.unitOfMeasurement())
                .build();

    }

    public RawMaterialResponse toResponse(RawMaterial rawMaterial){
        return new RawMaterialResponse(
                rawMaterial.getId(),
                rawMaterial.getCode(),
                rawMaterial.getName(),
                rawMaterial.getStockQuantity(),
                rawMaterial.getUnitOfMeasurement()
        );
    }


}
