package com.arthurberwanger.microbio.controller;

import com.arthurberwanger.microbio.service.OrcamentoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/solicitacoes")
public class ViewController {

    private final OrcamentoService service;

    public ViewController(OrcamentoService service) {
        this.service = service;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("solicitacoes", service.listarSolicitacoes());
        return "solicitacoes/lista";
    }

    /**
     * Cria um orçamento formal a partir de uma solicitação (Pessoa).
     * Redireciona para o detalhe do orçamento recém-criado.
     */
    @PostMapping("/{pessoaId}/criar-orcamento")
    public String criarOrcamento(@PathVariable Long pessoaId, RedirectAttributes ra) {
        try {
            var orc = service.criarDesolicitacao(pessoaId);
            ra.addFlashAttribute("sucesso", "Orçamento #" + orc.getId() + " criado com sucesso!");
            return "redirect:/indicadores/" + orc.getId();
        } catch (Exception e) {
            ra.addFlashAttribute("erro", e.getMessage());
            return "redirect:/solicitacoes";
        }
    }
}