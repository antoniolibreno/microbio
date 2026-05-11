package com.arthurberwanger.microbio.controller;

import com.arthurberwanger.microbio.repository.ClienteRepository;
import com.arthurberwanger.microbio.service.UsuarioService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    private final UsuarioService usuarioService;
    private final ClienteRepository clienteRepository;

    public AuthController(UsuarioService usuarioService, ClienteRepository clienteRepository) {
        this.usuarioService = usuarioService;
        this.clienteRepository = clienteRepository;
    }

    /** Site institucional — público, sem autenticação */
    @GetMapping("/")
    public String home() {
        return "index";
    }

    /**
     * Exibe o formulário de login.
     * NÃO faz nenhum redirect aqui — isso causava o loop.
     * O LoginSuccessHandler cuida de onde ir APÓS o login.
     */
    @GetMapping("/login")
    public String loginPage(
            @RequestParam(value = "erro",   required = false) String erro,
            @RequestParam(value = "logout", required = false) String logout,
            Model model) {

        if (erro != null)
            model.addAttribute("mensagemErro", "Login ou senha inválidos. Tente novamente.");
        if (logout != null)
            model.addAttribute("mensagemSucesso", "Você saiu com sucesso.");

        return "login";
    }

    /** Dashboard admin — protegido por ROLE_ADMIN no SecurityConfig */
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("totalUsuarios", usuarioService.contarUsuarios());
        model.addAttribute("totalClientes", clienteRepository.count());
        return "dashboard";
    }

    /** Painel do cliente — protegido por ROLE_USER no SecurityConfig */
    @GetMapping("/painel")
    public String painelCliente() {
        return "painel/index";
    }

    /**
     * Rota genérica de painel — redireciona conforme a role.
     * Usada pelo botão "Meu Painel" no site institucional.
     */
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

    @GetMapping("/indicadores")
    public String indicadores() {
        return "indicadores";
    }
}