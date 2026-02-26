package com.project.inventory.service;

import com.project.inventory.domain.entity.RawMaterial;
import com.project.inventory.domain.repository.RawMaterialRepository;
import com.project.inventory.dto.rawmaterial.UpdateRawMaterialRequest;
import com.project.inventory.dto.rawmaterial.CreateRawMaterialRequest;
import com.project.inventory.dto.rawmaterial.RawMaterialResponse;
import com.project.inventory.exception.ResourceNotFoundException;
import com.project.inventory.exception.BusinessRuleException;
import com.project.inventory.mapper.RawMaterialMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RawMaterialService {

    private final RawMaterialRepository rawMaterialRepository;
    private final RawMaterialMapper rawMaterialMapper;

    @Transactional(readOnly = true)
    public List<RawMaterialResponse> findAll() {
        return rawMaterialRepository.findAll()
                .stream()
                .map(rawMaterialMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public RawMaterialResponse findById(Long id) {
        RawMaterial rawMaterial = findEntityById(id);
        return rawMaterialMapper.toResponse(rawMaterial);
    }

    @Transactional
    public RawMaterialResponse create(CreateRawMaterialRequest request) {
        validateDuplicateCodeOnCreation(request.code());

        RawMaterial rawMaterial = rawMaterialMapper.toEntity(request);
        RawMaterial saved = rawMaterialRepository.save(rawMaterial);

        return rawMaterialMapper.toResponse(saved);
    }

    @Transactional
    public RawMaterialResponse update(Long id, UpdateRawMaterialRequest request) {
        RawMaterial rawMaterial = findEntityById(id);

        validateDuplicateCodeOnUpdate(request.code(), id);

        rawMaterial.setCode(request.code());
        rawMaterial.setName(request.name());
        rawMaterial.setStockQuantity(request.stockQuantity());
        rawMaterial.setUnitOfMeasurement(request.unitOfMeasurement());

        RawMaterial updated = rawMaterialRepository.save(rawMaterial);
        return rawMaterialMapper.toResponse(updated);
    }

    @Transactional
    public void delete(Long id) {
        RawMaterial rawMaterial = findEntityById(id);
        rawMaterialRepository.delete(rawMaterial);
    }

    private RawMaterial findEntityById(Long id) {
        return rawMaterialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Raw material not found for id: " + id));
    }

    private void validateDuplicateCodeOnCreation(String code) {
        if (rawMaterialRepository.existsByCode(code)) {
            throw new BusinessRuleException("Raw material with the given code already exists.");
        }
    }

    private void validateDuplicateCodeOnUpdate(String code, Long id) {
        if (rawMaterialRepository.existsAllByCodeAndIdNot(code, id)) {
            throw new BusinessRuleException("Another raw material with the given code already exists.");
        }
    }
}
