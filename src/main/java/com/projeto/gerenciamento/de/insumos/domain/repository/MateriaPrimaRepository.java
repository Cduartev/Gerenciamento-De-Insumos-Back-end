package com.projeto.gerenciamento.de.insumos.domain.repository;

import com.projeto.gerenciamento.de.insumos.domain.entity.MateriaPrima;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MateriaPrimaRepository extends JpaRepository<MateriaPrima, Long> {

    boolean existsbyCodidgo(String codigo);

    boolean existsAllByCodigoAndIdNot(String codigo, long id);

    Optional<MateriaPrima> findByCodigo(String codigo);
}
