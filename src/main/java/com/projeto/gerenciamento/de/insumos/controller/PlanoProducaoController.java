package com.projeto.gerenciamento.de.insumos.controller;

import com.projeto.gerenciamento.de.insumos.dto.planoproducao.SugestaoPlanoProducaoResponse;
import com.projeto.gerenciamento.de.insumos.service.otimizacao.PlanoProducaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/planos-producao")
@RequiredArgsConstructor
public class PlanoProducaoController {

    private final PlanoProducaoService planoProducaoService;

    @PostMapping("/sugerir")
    public ResponseEntity<SugestaoPlanoProducaoResponse> sugerirPlanoOtimo() {
        return ResponseEntity.ok(planoProducaoService.sugerirPlanoOtimo());
    }
}
