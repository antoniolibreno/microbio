package com.arthurberwanger.microbio.controller;

import com.arthurberwanger.microbio.service.AnaliseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/analises")
@Tag(name = "Análises", description = "Consulta, cadastro, edição e exclusão de análises microbiológicas do catálogo.")
public class AnaliseController {

    private final AnaliseService service;

    public AnaliseController(AnaliseService service) {
        this.service = service;
    }

    @GetMapping
    public String listar(@RequestParam(required = false) String status, Model model) {
        var todas = service.listarTodas();
        var filtradas = (status != null && !status.isBlank())
                ? todas.stream().filter(a -> status.equalsIgnoreCase(a.getStatus())).toList()
                : todas;
        model.addAttribute("analises",      filtradas);
        model.addAttribute("statusFiltro",  status);
        model.addAttribute("totalTodas",    todas.size());
        model.addAttribute("totalAtivas",   service.contarPorStatus("ATIVA"));
        model.addAttribute("totalInativas", service.contarPorStatus("INATIVA"));
        return "analises/lista";
    }

    @GetMapping("/nova")
    public String nova() {
        return "analises/nova";
    }

    @Operation(
            summary = "Cadastra uma nova análise",
            description = "Cria uma análise no catálogo a partir de nome, descrição, valor, tempo de produção, status e listas de tipos, quantidades e formas de conservação suportadas."
    )
    @PostMapping
    public String criar(@RequestParam String nome,
                        @RequestParam(required = false) String descricao,
                        @RequestParam(required = false) BigDecimal valor,
                        @RequestParam(required = false) String tempoProducao,
                        @RequestParam(required = false, defaultValue = "ATIVA") String status,
                        @RequestParam(required = false, name = "tipo") List<String> tipos,
                        @RequestParam(required = false, name = "quantidade") List<String> quantidades,
                        @RequestParam(required = false, name = "conservacao") List<String> conservacoes,
                        RedirectAttributes ra) {
        try {
            var a = service.criar(nome, descricao, valor, tempoProducao, status, tipos, quantidades, conservacoes);
            ra.addFlashAttribute("sucesso", "Análise \"" + a.getNome() + "\" cadastrada.");
            return "redirect:/analises/" + a.getId();
        } catch (Exception e) {
            ra.addFlashAttribute("erro", "Erro ao cadastrar: " + e.getMessage());
            return "redirect:/analises/nova";
        }
    }

    @GetMapping("/{id}")
    public String detalhe(@PathVariable Long id, Model model, RedirectAttributes ra) {
        try {
            model.addAttribute("analise", service.buscarPorId(id));
            return "analises/detalhe";
        } catch (EntityNotFoundException e) {
            ra.addFlashAttribute("erro", e.getMessage());
            return "redirect:/analises";
        }
    }

    @PostMapping("/{id}/atualizar")
    public String atualizar(@PathVariable Long id,
                            @RequestParam String nome,
                            @RequestParam(required = false) String descricao,
                            @RequestParam(required = false) BigDecimal valor,
                            @RequestParam(required = false) String tempoProducao,
                            @RequestParam(required = false) String status,
                            @RequestParam(required = false, name = "tipo") List<String> tipos,
                            @RequestParam(required = false, name = "quantidade") List<String> quantidades,
                            @RequestParam(required = false, name = "conservacao") List<String> conservacoes,
                            RedirectAttributes ra) {
        try {
            service.atualizar(id, nome, descricao, valor, tempoProducao, status, tipos, quantidades, conservacoes);
            ra.addFlashAttribute("sucesso", "Análise atualizada.");
        } catch (Exception e) {
            ra.addFlashAttribute("erro", "Erro ao atualizar: " + e.getMessage());
        }
        return "redirect:/analises/" + id;
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id, RedirectAttributes ra) {
        try {
            service.excluir(id);
            ra.addFlashAttribute("sucesso", "Análise #" + id + " excluída.");
        } catch (Exception e) {
            ra.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/analises";
    }
}
