package com.projeto.gerenciamento.de.insumos.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "produtos",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_produto_codigo", columnNames = "codigo")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Produto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo", nullable = false, length = 50)
    private String codigo;

    @Column(name = "nome", nullable = false, length = 50)
    private String nome;

    @Column(name = "valor", nullable = false, precision = 19, scale = 2)
    private BigDecimal valor;

    @OneToMany(mappedBy = "produto", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ItemComposicaoProduto> itensComposicao = new ArrayList<>();

    public void adicionarItemComposicao(ItemComposicaoProduto item) {
        item.setProduto(this);
        this.itensComposicao.add(item);
    }

    public void limparItensComposicao() {
        this.itensComposicao.clear();
    }
}
