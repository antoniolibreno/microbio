package com.arthurberwanger.microbio.controller;

import com.arthurberwanger.microbio.service.AnaliseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analises")
@Tag(name = "Análises", description = "Consulta, cadastro, edição e exclusão de análises microbiológicas do catálogo.")
public class AnaliseApiController {

    private final AnaliseService service;

    public AnaliseApiController(AnaliseService service) {
        this.service = service;
    }

    @Operation(
            summary = "Lista análises ativas",
            description = "Retorna id e nome de todas as análises com status ATIVA. Endpoint público, usado pelo site institucional para popular o formulário de solicitação de orçamento."
    )
    @GetMapping("/ativas")
    public List<Map<String, Object>> listarAtivas() {
        return service.listarTodas().stream()
                .filter(a -> "ATIVA".equalsIgnoreCase(a.getStatus()))
                .map(a -> Map.<String, Object>of(
                        "id", a.getId(),
                        "nome", a.getNome()
                ))
                .toList();
    }
}
