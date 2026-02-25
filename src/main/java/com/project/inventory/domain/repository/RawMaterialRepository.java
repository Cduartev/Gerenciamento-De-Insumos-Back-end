package com.project.inventory.domain.repository;

import com.project.inventory.domain.entity.RawMaterial;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RawMaterialRepository extends JpaRepository<RawMaterial, Long> {

    boolean existsByCode(String code);

    boolean existsAllByCodeAndIdNot(String code, long id);

    Optional<RawMaterial> findByCode(String code);
}
