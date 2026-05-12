package com.arthurberwanger.microbio.controller;

import com.arthurberwanger.microbio.model.Orcamento;
import com.arthurberwanger.microbio.model.Orcamento.StatusOrcamento;
import com.arthurberwanger.microbio.service.OrcamentoService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/indicadores")
public class IndicadoresController {

    private final OrcamentoService orcamentoService;

    public IndicadoresController(OrcamentoService orcamentoService) {
        this.orcamentoService = orcamentoService;
    }

    /** Lista todos os orçamentos com filtro opcional por status. */
    @GetMapping
    public String listar(@RequestParam(required = false) String status, Model model) {
        List<Orcamento> todos = orcamentoService.listarTodos();

        List<Orcamento> filtrados = (status != null && !status.isBlank())
                ? todos.stream()
                .filter(o -> o.getStatus().name().equalsIgnoreCase(status))
                .toList()
                : todos;

        model.addAttribute("orcamentos",    filtrados);
        model.addAttribute("statusFiltro",  status);
        model.addAttribute("totalTodos",    todos.size());
        model.addAttribute("totalPendente",    contarStatus(todos, StatusOrcamento.PENDENTE));
        model.addAttribute("totalAndamento",   contarStatus(todos, StatusOrcamento.EM_ANDAMENTO));
        model.addAttribute("totalConcluido",   contarStatus(todos, StatusOrcamento.CONCLUIDO));
        model.addAttribute("totalCancelado",   contarStatus(todos, StatusOrcamento.CANCELADO));
        model.addAttribute("statusValues",     StatusOrcamento.values());
        return "orcamentopainel";
    }

    /** Detalhe de um orçamento específico. */
    @GetMapping("/{id}")
    public String detalhe(@PathVariable Long id, Model model,
                          RedirectAttributes ra) {
        try {
            Orcamento orc = orcamentoService.buscarPorId(id);
            model.addAttribute("orcamento", orc);
            model.addAttribute("statusValues", StatusOrcamento.values());
            return "editarorcamentopainel";
        } catch (EntityNotFoundException e) {
            ra.addFlashAttribute("erro", e.getMessage());
            return "redirect:/indicadores";
        }
    }

    /** Promove o status: PENDENTE→EM_ANDAMENTO→CONCLUIDO. */
    @PostMapping("/{id}/promover")
    public String promover(@PathVariable Long id, RedirectAttributes ra) {
        try {
            Orcamento atualizado = orcamentoService.promoverStatus(id);
            ra.addFlashAttribute("sucesso",
                    "Orçamento #" + id + " promovido para " + atualizado.getStatus().getLabel() + ".");
        } catch (Exception e) {
            ra.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/indicadores/" + id;
    }

    /** Atualiza o status livremente via select do formulário de edição. */
    @PostMapping("/{id}/status")
    public String atualizarStatus(@PathVariable Long id,
                                  @RequestParam String status,
                                  RedirectAttributes ra) {
        try {
            StatusOrcamento novoStatus = StatusOrcamento.valueOf(status);
            orcamentoService.atualizarStatus(id, novoStatus);
            ra.addFlashAttribute("sucesso", "Status atualizado para \"" + novoStatus.getLabel() + "\".");
        } catch (Exception e) {
            ra.addFlashAttribute("erro", "Não foi possível atualizar o status: " + e.getMessage());
        }
        return "redirect:/indicadores/" + id;
    }

    /** Cancela o orçamento. */
    @PostMapping("/{id}/cancelar")
    public String cancelar(@PathVariable Long id, RedirectAttributes ra) {
        try {
            orcamentoService.cancelar(id);
            ra.addFlashAttribute("sucesso", "Orçamento #" + id + " cancelado.");
        } catch (Exception e) {
            ra.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/indicadores/" + id;
    }

    private long contarStatus(List<Orcamento> lista, StatusOrcamento s) {
        return lista.stream().filter(o -> o.getStatus() == s).count();
    }
}