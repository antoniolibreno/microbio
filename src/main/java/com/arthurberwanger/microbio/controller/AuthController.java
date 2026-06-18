package com.arthurberwanger.microbio.controller;

import com.arthurberwanger.microbio.model.Orcamento.StatusOrcamento;
import com.arthurberwanger.microbio.repository.ClienteRepository;
import com.arthurberwanger.microbio.service.AnaliseService;
import com.arthurberwanger.microbio.service.OrcamentoService;
import com.arthurberwanger.microbio.service.PedidoService;
import com.arthurberwanger.microbio.service.UsuarioService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Controller
public class AuthController {

    private final UsuarioService    usuarioService;
    private final ClienteRepository clienteRepository;
    private final OrcamentoService  orcamentoService;
    private final PedidoService     pedidoService;
    private final AnaliseService    analiseService;

    public AuthController(UsuarioService usuarioService,
                          ClienteRepository clienteRepository,
                          OrcamentoService orcamentoService,
                          PedidoService pedidoService,
                          AnaliseService analiseService) {
        this.usuarioService    = usuarioService;
        this.clienteRepository = clienteRepository;
        this.orcamentoService  = orcamentoService;
        this.pedidoService     = pedidoService;
        this.analiseService    = analiseService;
    }

    @GetMapping("/")
    public String home() { return "home/index"; }

    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String erro,
                            @RequestParam(required = false) String logout,
                            Model model) {
        if (erro   != null) model.addAttribute("mensagemErro",    "Login ou senha inválidos. Tente novamente.");
        if (logout != null) model.addAttribute("mensagemSucesso", "Você saiu com sucesso.");
        return "auth/login";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        long total      = orcamentoService.contarTodos();
        long pendentes  = orcamentoService.contarPorStatus(StatusOrcamento.PENDENTE);
        long andamento  = orcamentoService.contarPorStatus(StatusOrcamento.EM_ANDAMENTO);
        long concluidos = orcamentoService.contarPorStatus(StatusOrcamento.CONCLUIDO);
        long cancelados = orcamentoService.contarPorStatus(StatusOrcamento.CANCELADO);

        model.addAttribute("totalUsuarios",   usuarioService.contarUsuarios());
        model.addAttribute("totalClientes",   clienteRepository.count());
        model.addAttribute("totalOrcamentos", total);
        model.addAttribute("totalPendentes",  pendentes);
        model.addAttribute("totalAndamento",  andamento);
        model.addAttribute("totalConcluidos", concluidos);
        model.addAttribute("totalCancelados", cancelados);
        model.addAttribute("totalPedidos",    pedidoService.contarTodos());

        long ativos = pendentes + andamento;
        long taxaConclusao = (total - cancelados) > 0
                ? (concluidos * 100L / (total - cancelados)) : 0L;
        model.addAttribute("taxaConclusao", taxaConclusao);
        model.addAttribute("totalAtivos",   ativos);

        // Métricas financeiras e de catálogo
        BigDecimal faturamento = orcamentoService.faturamentoConcluido();
        BigDecimal ticketMedio = concluidos > 0
                ? faturamento.divide(BigDecimal.valueOf(concluidos), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        model.addAttribute("faturamentoConcluido", faturamento);
        model.addAttribute("ticketMedio",          ticketMedio);
        model.addAttribute("totalAnalisesAtivas",  analiseService.contarPorStatus("ATIVA"));

        model.addAttribute("orcamentosRecentes", orcamentoService.listarRecentes(6));
        model.addAttribute("pedidosRecentes",   pedidoService.listarRecentes(5));

        long pedidosPendentes  = pedidoService.contarPorStatus("PENDENTE");
        long pedidosAndamento  = pedidoService.contarPorStatus("EM_ANDAMENTO");
        long pedidosConcluidos = pedidoService.contarPorStatus("CONCLUIDO");
        model.addAttribute("pedidosPendentes",  pedidosPendentes);
        model.addAttribute("pedidosAndamento",  pedidosAndamento);
        model.addAttribute("pedidosConcluidos", pedidosConcluidos);
        var contagem = orcamentoService.contagemPorMes(6);
        var labels   = orcamentoService.labelsMeses(6);
        model.addAttribute("contagemPorMes",     contagem);
        model.addAttribute("labelsMeses",        labels);
        try {
            ObjectMapper mapper = new ObjectMapper();
            model.addAttribute("contagemPorMesJson", mapper.writeValueAsString(contagem));
            model.addAttribute("labelsMesesJson",    mapper.writeValueAsString(labels));
        } catch (JsonProcessingException e) {
            model.addAttribute("contagemPorMesJson", "[]");
            model.addAttribute("labelsMesesJson",    "[]");
        }
        return "auth/dashboard";
    }

    @GetMapping("/meu-painel")
    public String meuPainel() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            boolean isAdmin = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            return "redirect:" + (isAdmin ? "/dashboard" : "/painel");
        }
        return "redirect:/login";
    }
}