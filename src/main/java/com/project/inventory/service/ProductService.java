package com.project.inventory.service;

import com.project.inventory.domain.entity.ProductCompositionItem;
import com.project.inventory.domain.entity.RawMaterial;
import com.project.inventory.domain.entity.Product;
import com.project.inventory.domain.repository.RawMaterialRepository;
import com.project.inventory.domain.repository.ProductRepository;
import com.project.inventory.dto.product.UpdateProductRequest;
import com.project.inventory.dto.product.CreateProductRequest;
import com.project.inventory.dto.product.ProductCompositionItemRequest;
import com.project.inventory.dto.product.ProductResponse;
import com.project.inventory.exception.ResourceNotFoundException;
import com.project.inventory.exception.BusinessRuleException;
import com.project.inventory.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final RawMaterialRepository rawMaterialRepository;
    private final ProductMapper productMapper;

    @Transactional(readOnly = true)
    public List<ProductResponse> findAll() {
        return productRepository.findAll()
                .stream()
                .map(productMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductResponse findById(Long id) {
        Product product = findEntityWithCompositionById(id);
        return productMapper.toResponse(product);
    }

    @Transactional
    public ProductResponse create(CreateProductRequest request) {
        validateDuplicateCodeOnCreation(request.code());
        validateCompositionItems(request.compositionItems());

        Product product = Product.builder()
                .code(request.code())
                .name(request.name())
                .price(request.price())
                .build();

        addCompositionItems(product, request.compositionItems());

        Product saved = productRepository.save(product);
        Product completeProduct = findEntityWithCompositionById(saved.getId());

        return productMapper.toResponse(completeProduct);
    }

    @Transactional
    public ProductResponse update(Long id, UpdateProductRequest request) {
        Product product = findEntityWithCompositionById(id);

        validateDuplicateCodeOnUpdate(request.code(), id);
        validateCompositionItems(request.compositionItems());

        product.setCode(request.code());
        product.setName(request.name());
        product.setPrice(request.price());

        product.limparItensComposition();
        productRepository.saveAndFlush(product); // garante remoção no banco antes de re-adicionar

        addCompositionItems(product, request.compositionItems());

        Product updated = productRepository.save(product);
        Product completeProduct = findEntityWithCompositionById(updated.getId());

        return productMapper.toResponse(completeProduct);
    }

    @Transactional
    public void delete(Long id) {
        Product product = findEntityById(id);
        productRepository.delete(product);
    }

    private Product findEntityById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found for id: " + id));
    }

    private Product findEntityWithCompositionById(Long id) {
        return productRepository.findByIdWithComposition(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found for id: " + id));
    }

    private void validateDuplicateCodeOnCreation(String code) {
        if (productRepository.existsByCode(code)) {
            throw new BusinessRuleException("Product with the given code already exists.");
        }
    }

    private void validateDuplicateCodeOnUpdate(String code, Long id) {
        if (productRepository.existsByCodeAndIdNot(code, id)) {
            throw new BusinessRuleException("Another product with the given code already exists.");
        }
    }

    private void validateCompositionItems(List<ProductCompositionItemRequest> compositionItems) {
        Set<Long> idsMateriasPrimas = new HashSet<>();

        for (ProductCompositionItemRequest item : compositionItems) {
            if (!idsMateriasPrimas.add(item.rawMaterialId())) {
                throw new BusinessRuleException(
                        "A composition nao pode conter materia-prima repetida. Id repetido: " + item.rawMaterialId()
                );
            }
        }
    }

    private void addCompositionItems(Product product, List<ProductCompositionItemRequest> itensRequest) {
        List<Long> idsMateriasPrimas = itensRequest.stream()
                .map(ProductCompositionItemRequest::rawMaterialId)
                .toList();

        List<RawMaterial> rawMaterials = rawMaterialRepository.findAllById(idsMateriasPrimas);

        if (rawMaterials.size() != idsMateriasPrimas.size()) {
            Set<Long> idsEncontrados = rawMaterials.stream()
                    .map(RawMaterial::getId)
                    .collect(Collectors.toSet());

            List<Long> idsNaoEncontrados = idsMateriasPrimas.stream()
                    .filter(id -> !idsEncontrados.contains(id))
                    .distinct()
                    .toList();

            throw new ResourceNotFoundException(
                    "Materia-prima(s) nao encontrada(s) para os ids: " + idsNaoEncontrados
            );
        }

        Map<Long, RawMaterial> rawMaterialsPorId = rawMaterials.stream()
                .collect(Collectors.toMap(RawMaterial::getId, Function.identity()));

        for (ProductCompositionItemRequest itemRequest : itensRequest) {
            RawMaterial rawMaterial = rawMaterialsPorId.get(itemRequest.rawMaterialId());

            ProductCompositionItem item = ProductCompositionItem.builder()
                    .rawMaterial(rawMaterial)
                    .requiredQuantity(itemRequest.requiredQuantity())
                    .build();

            product.adicionarItemComposition(item);
        }
    }
}
