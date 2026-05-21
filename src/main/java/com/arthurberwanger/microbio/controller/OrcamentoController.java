package com.arthurberwanger.microbio.controller;

import com.arthurberwanger.microbio.dto.OrcamentoDTO;
import com.arthurberwanger.microbio.model.Orcamento;
import com.arthurberwanger.microbio.service.OrcamentoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Endpoint público — acessível sem login pelo site institucional.
 * Recebe solicitações de orçamento de novos clientes em potencial.
 */
@RestController
@RequestMapping("/api/solicitacoes")
@Tag(name = "Solicitações de Orçamento", description = "Recebe pedidos de orçamento enviados pelo site institucional (endpoint público).")
public class OrcamentoController {

    private final OrcamentoService service;

    public OrcamentoController(OrcamentoService service) {
        this.service = service;
    }

    @Operation(
            summary = "Registra uma nova solicitação de orçamento",
            description = "Cria uma solicitação a partir dos dados de contato do interessado (nome, email, telefone, tipo de serviço). Retorna o id gerado em caso de sucesso."
    )
    @PostMapping
    public ResponseEntity<?> criar(@Valid @RequestBody OrcamentoDTO dto) {
        try {
            Orcamento orc = service.registrarSolicitacao(dto);
            return ResponseEntity.ok(Map.of(
                    "sucesso", true,
                    "mensagem", "Solicitação recebida com sucesso! Entraremos em contato em breve.",
                    "id", orc.getId()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "sucesso", false,
                    "mensagem", "Erro ao registrar solicitação. Tente novamente."
            ));
        }
    }
}