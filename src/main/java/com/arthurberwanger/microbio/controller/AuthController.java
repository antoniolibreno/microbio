package com.arthurberwanger.microbio.controller;

import com.arthurberwanger.microbio.model.Orcamento.StatusOrcamento;
import com.arthurberwanger.microbio.repository.ClienteRepository;
import com.arthurberwanger.microbio.service.OrcamentoService;
import com.arthurberwanger.microbio.service.PedidoService;
import com.arthurberwanger.microbio.service.UsuarioService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    private final UsuarioService    usuarioService;
    private final ClienteRepository clienteRepository;
    private final OrcamentoService  orcamentoService;
    private final PedidoService     pedidoService;

    public AuthController(UsuarioService usuarioService,
                          ClienteRepository clienteRepository,
                          OrcamentoService orcamentoService,
                          PedidoService pedidoService) {
        this.usuarioService    = usuarioService;
        this.clienteRepository = clienteRepository;
        this.orcamentoService  = orcamentoService;
        this.pedidoService     = pedidoService;
    }

    @GetMapping("/")
    public String home() { return "index"; }

    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String erro,
                            @RequestParam(required = false) String logout,
                            Model model) {
        if (erro   != null) model.addAttribute("mensagemErro",    "Login ou senha inválidos. Tente novamente.");
        if (logout != null) model.addAttribute("mensagemSucesso", "Você saiu com sucesso.");
        return "login";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("totalUsuarios",   usuarioService.contarUsuarios());
        model.addAttribute("totalClientes",   clienteRepository.count());
        model.addAttribute("totalOrcamentos", orcamentoService.contarTodos());
        model.addAttribute("totalPendentes",  orcamentoService.contarPorStatus(StatusOrcamento.PENDENTE));
        model.addAttribute("totalAndamento",  orcamentoService.contarPorStatus(StatusOrcamento.EM_ANDAMENTO));
        model.addAttribute("totalConcluidos", orcamentoService.contarPorStatus(StatusOrcamento.CONCLUIDO));
        model.addAttribute("totalPedidos",    pedidoService.contarTodos());
        return "dashboard";
    }

    @GetMapping("/painel")
    public String painelCliente() { return "painel/index"; }

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