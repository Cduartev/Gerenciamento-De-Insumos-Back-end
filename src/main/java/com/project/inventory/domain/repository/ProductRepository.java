package com.project.inventory.domain.repository;

import com.project.inventory.domain.entity.Product;
import jakarta.persistence.Entity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Boolean existsByCode(String code);

    Boolean existsByCodeAndIdNot(String code, Long id);

    @Override
    @EntityGraph(attributePaths = {"compositionItems", "compositionItems.rawMaterial"})
    List<Product> findAll();

    @EntityGraph(attributePaths = {"compositionItems", "compositionItems.rawMaterial"})
    @Query("select p from Product p where p.id = :id")
    Optional<Product> findByIdWithComposition(Long id);
}
