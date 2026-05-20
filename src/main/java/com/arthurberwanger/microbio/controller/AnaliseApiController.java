package com.arthurberwanger.microbio.controller;

import com.arthurberwanger.microbio.service.AnaliseService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analises")
public class AnaliseApiController {

    private final AnaliseService service;

    public AnaliseApiController(AnaliseService service) {
        this.service = service;
    }

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
