package com.arthurberwanger.microbio.controller;

import com.arthurberwanger.microbio.model.Orcamento;
import com.arthurberwanger.microbio.model.Orcamento.StatusOrcamento;
import com.arthurberwanger.microbio.model.Pedido;
import com.arthurberwanger.microbio.repository.AnaliseRepository;
import com.arthurberwanger.microbio.service.OrcamentoService;
import com.arthurberwanger.microbio.service.PdfService;
import com.arthurberwanger.microbio.service.PedidoService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/indicadores")
public class IndicadoresController {

    private final OrcamentoService orcamentoService;
    private final PedidoService    pedidoService;
    private final AnaliseRepository analiseRepository;
    private final PdfService pdfService;

    public IndicadoresController(OrcamentoService orcamentoService,
                                 PedidoService pedidoService,
                                 AnaliseRepository analiseRepository,
                                 PdfService pdfService) {
        this.orcamentoService = orcamentoService;
        this.pedidoService    = pedidoService;
        this.analiseRepository = analiseRepository;
        this.pdfService = pdfService;
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
        return "indicadores/lista";
    }

    /** Detalhe de um orçamento específico. */
    @GetMapping("/{id}")
    public String detalhe(@PathVariable Long id, Model model,
                          RedirectAttributes ra) {
        try {
            Orcamento orc = orcamentoService.buscarPorId(id);

            var vinculadas = orcamentoService.listarAnalises(id);
            var idsVinculados = vinculadas.stream()
                    .map(oa -> oa.getAnalise() != null ? oa.getAnalise().getId() : null)
                    .filter(java.util.Objects::nonNull)
                    .collect(java.util.stream.Collectors.toSet());
            var disponiveis = analiseRepository.findAll().stream()
                    .filter(a -> "ATIVA".equals(a.getStatus()) && !idsVinculados.contains(a.getId()))
                    .toList();

            model.addAttribute("orcamento", orc);
            model.addAttribute("analisesVinculadas", vinculadas);
            model.addAttribute("analisesDisponiveis", disponiveis);
            model.addAttribute("statusValues", StatusOrcamento.values());
            return "indicadores/editar";
        } catch (EntityNotFoundException e) {
            ra.addFlashAttribute("erro", e.getMessage());
            return "redirect:/indicadores";
        }
    }

    /** Adiciona uma análise ao orçamento (recalcula o total automaticamente). */
    @PostMapping("/{id}/analises/adicionar")
    public String adicionarAnalise(@PathVariable Long id,
                                   @RequestParam Long analiseId,
                                   RedirectAttributes ra) {
        try {
            orcamentoService.adicionarAnalise(id, analiseId);
            ra.addFlashAttribute("sucesso", "Análise adicionada ao orçamento.");
        } catch (Exception e) {
            ra.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/indicadores/" + id;
    }

    /** Remove uma análise do orçamento (recalcula o total automaticamente). */
    @PostMapping("/{id}/analises/{oaId}/remover")
    public String removerAnalise(@PathVariable Long id,
                                 @PathVariable Long oaId,
                                 RedirectAttributes ra) {
        try {
            orcamentoService.removerAnalise(oaId);
            ra.addFlashAttribute("sucesso", "Análise removida do orçamento.");
        } catch (Exception e) {
            ra.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/indicadores/" + id;
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

    /** Salva status + observações (botão "Salvar" do detalhe). O valor total é derivado das análises. */
    @PostMapping("/{id}/atualizar")
    public String atualizar(@PathVariable Long id,
                            @RequestParam(required = false) String status,
                            @RequestParam(required = false) String observacoes,
                            RedirectAttributes ra) {
        try {
            StatusOrcamento s = (status != null && !status.isBlank())
                    ? StatusOrcamento.valueOf(status) : null;
            orcamentoService.atualizar(id, s, observacoes);
            ra.addFlashAttribute("sucesso", "Orçamento atualizado.");
        } catch (Exception e) {
            ra.addFlashAttribute("erro", "Erro ao atualizar: " + e.getMessage());
        }
        return "redirect:/indicadores/" + id;
    }

    /** "Ganhar" o orçamento: cria o Pedido vinculado e marca CONCLUIDO, atomicamente. */
    @PostMapping("/{id}/ganhar")
    public String ganhar(@PathVariable Long id,
                         @RequestParam(required = false) String observacoes,
                         RedirectAttributes ra) {
        try {
            Pedido novo = pedidoService.ganharOrcamento(id, observacoes);
            ra.addFlashAttribute("sucesso",
                    "Orçamento ganho! Pedido #" + novo.getId() + " criado.");
            return "redirect:/indicadores";
        } catch (Exception e) {
            ra.addFlashAttribute("erro", "Não foi possível ganhar o orçamento: " + e.getMessage());
            return "redirect:/indicadores/" + id;
        }
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

    /** Gera o PDF do orçamento para download. */
    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> gerarPdf(@PathVariable Long id) {
        Orcamento orc = orcamentoService.buscarPorId(id);
        byte[] pdf = pdfService.render("pdf/orcamento", Map.of("orcamento", orc));
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.inline()
                .filename("orcamento-" + id + ".pdf").build());
        return new ResponseEntity<>(pdf, headers, 200);
    }

    private long contarStatus(List<Orcamento> lista, StatusOrcamento s) {
        return lista.stream().filter(o -> o.getStatus() == s).count();
    }
}