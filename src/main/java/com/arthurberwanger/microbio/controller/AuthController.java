package com.arthurberwanger.microbio.controller;

import com.arthurberwanger.microbio.repository.ClienteRepository;
import com.arthurberwanger.microbio.service.UsuarioService;
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

    @GetMapping("/login")
    public String loginPage(
            @RequestParam(value = "erro",   required = false) String erro,
            @RequestParam(value = "logout", required = false) String logout,
            Model model) {

        if (erro != null)   model.addAttribute("mensagemErro",    "Login ou senha inválidos. Tente novamente.");
        if (logout != null) model.addAttribute("mensagemSucesso", "Você saiu com sucesso.");

        return "login";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // Passa os totais para os cards do dashboard
        model.addAttribute("totalUsuarios", usuarioService.contarUsuarios());
        model.addAttribute("totalClientes", clienteRepository.count());
        return "dashboard";
    public String dashboard() {
        return "dashboard";
    }

    @GetMapping("/indicadores")
    public String indicadores() {
        return "indicadores";
    }

    @GetMapping("/")
    public String root() {
        return "redirect:/dashboard";
    }
}