package com.projeto.gerenciamento.de.insumos.domain.entity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(
        name = "itens_composicao_produto",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_produto_materia_prima",
                        columnNames = {"produto_id", "materia_prima_id"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemComposicaoProduto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "materia_prima_id", nullable = false)
    private MateriaPrima materiaPrima;

    @Column(name = "quantidade_necessaria", nullable = false, precision = 19, scale = 4)
    private BigDecimal quantidadeNecessaria;

}
