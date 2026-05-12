package com.arthurberwanger.microbio.controller;

import com.arthurberwanger.microbio.model.Pedido;
import com.arthurberwanger.microbio.service.PedidoService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/pedidos")
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("pedidos",         pedidoService.listarTodos());
        model.addAttribute("totalPendentes",  pedidoService.contarPorStatus("PENDENTE"));
        model.addAttribute("totalAndamento",  pedidoService.contarPorStatus("EM_ANDAMENTO"));
        model.addAttribute("totalConcluidos", pedidoService.contarPorStatus("CONCLUIDO"));
        model.addAttribute("totalCancelados", pedidoService.contarPorStatus("CANCELADO"));
        return "pedidos/lista";
    }

    @GetMapping("/{id}")
    public String detalhe(@PathVariable Long id, Model model, RedirectAttributes ra) {
        try {
            model.addAttribute("pedido", pedidoService.buscarPorId(id));
            return "pedidos/detalhe";
        } catch (EntityNotFoundException e) {
            ra.addFlashAttribute("erro", e.getMessage());
            return "redirect:/pedidos";
        }
    }

    @PostMapping("/{id}/promover")
    public String promover(@PathVariable Long id, RedirectAttributes ra) {
        try {
            Pedido p = pedidoService.promoverStatus(id);
            ra.addFlashAttribute("sucesso", "Pedido #" + id + " atualizado para " + labelStatus(p.getStatus()) + ".");
        } catch (Exception e) {
            ra.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/pedidos/" + id;
    }

    @PostMapping("/{id}/cancelar")
    public String cancelar(@PathVariable Long id, RedirectAttributes ra) {
        try {
            pedidoService.cancelar(id);
            ra.addFlashAttribute("sucesso", "Pedido #" + id + " cancelado.");
        } catch (Exception e) {
            ra.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/pedidos/" + id;
    }

    private String labelStatus(String s) {
        return switch (s) {
            case "PENDENTE"     -> "Pendente";
            case "EM_ANDAMENTO" -> "Em andamento";
            case "CONCLUIDO"    -> "Concluído";
            case "CANCELADO"    -> "Cancelado";
            default             -> s;
        };
    }
}