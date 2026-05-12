package com.arthurberwanger.microbio.controller;

import com.arthurberwanger.microbio.dto.OrcamentoDTO;
import com.arthurberwanger.microbio.model.Orcamento;
import com.arthurberwanger.microbio.service.OrcamentoService;
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
public class OrcamentoController {

    private final OrcamentoService service;

    public OrcamentoController(OrcamentoService service) {
        this.service = service;
    }

    /**
     * POST /api/solicitacoes
     * Body JSON: { nome, email, telefone, tipoServico }
     */
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