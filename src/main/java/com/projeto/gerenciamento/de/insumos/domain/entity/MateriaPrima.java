package com.projeto.gerenciamento.de.insumos.domain.entity;

import com.projeto.gerenciamento.de.insumos.domain.enumtype.UnidadeMedida;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "materias_primas", uniqueConstraints ={@UniqueConstraint(name = "uk_materia_prima_codigo", columnNames = "codigo")})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MateriaPrima {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "codigo", nullable = false, length = 50)
    private String codigo;

    @Column(name = "nome", nullable = false, length = 120)
    private String nome;

    @Column(name = "quantidade_estoque", nullable = false, precision = 19, scale = 4)
    private BigDecimal quantidadeEstoque;

    @Enumerated(EnumType.STRING)
    @Column(name = "unidade_medida", nullable = false, length = 20)
    private UnidadeMedida unidadeMedida;
}
