package com.projeto.gerenciamento.de.insumos.mapper;

import com.projeto.gerenciamento.de.insumos.domain.entity.MateriaPrima;
import com.projeto.gerenciamento.de.insumos.dto.materiaprima.CriarMateriaPrimaRequest;
import com.projeto.gerenciamento.de.insumos.dto.materiaprima.MateriaPrimaResponse;
import org.springframework.stereotype.Component;

@Component
public class MateriaPrimaMapper {

    public MateriaPrima paraEntidade(CriarMateriaPrimaRequest request){
        return MateriaPrima.builder()
                .codigo(request.codigo())
                .nome(request.nome())
                .quantidadeEstoque(request.quantidadeEstoque())
                .unidadeMedida(request.unidadeMedida())
                .build();

    }

    public MateriaPrimaResponse paraResponse(MateriaPrima materiaPrima){
        return new MateriaPrimaResponse(
                materiaPrima.getId(),
                materiaPrima.getCodigo(),
                materiaPrima.getNome(),
                materiaPrima.getQuantidadeEstoque(),
                materiaPrima.getUnidadeMedida()
        );
    }


}
