package com.projeto.gerenciamento.de.insumos.domain.repository;

import com.projeto.gerenciamento.de.insumos.domain.entity.Produto;
import jakarta.persistence.Entity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    Boolean existsByCodigo(String codigo);

    Boolean existsByCodigoAndIdNot(String codigo, Long id);

    @Override
    @EntityGraph(attributePaths = {"itensComposicao", "itensComposicao.materiaPrima"})
    List<Produto> findAll();

    @EntityGraph(attributePaths = {"itensComposicao", "itensComposicao.materiaPrima"})
    @Query("select p from Produto p where p.id = :id")
    Optional<Produto> buscarPorIdComComposicao(Long id);
}
