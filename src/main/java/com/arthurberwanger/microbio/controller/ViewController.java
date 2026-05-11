package com.arthurberwanger.microbio.controller;

import com.arthurberwanger.microbio.service.OrcamentoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/solicitacoes")
public class ViewController {

    private final OrcamentoService service;

    public ViewController(OrcamentoService service) {
        this.service = service;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("solicitacoes", service.listarTodas());
        return "solicitacoes/lista";
    }
}