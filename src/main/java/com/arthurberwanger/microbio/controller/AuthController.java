package com.arthurberwanger.microbio.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    /**
     * Exibe a página de login.
     *
     * @param erro    presente na URL quando a senha estiver errada (?erro=true)
     * @param logout  presente na URL quando o usuário acabou de sair (?logout=true)
     */
    @GetMapping("/login")
    public String loginPage(
            @RequestParam(value = "erro",   required = false) String erro,
            @RequestParam(value = "logout", required = false) String logout,
            Model model) {

        if (erro != null) {
            model.addAttribute("mensagemErro", "Login ou senha inválidos. Tente novamente.");
        }
        if (logout != null) {
            model.addAttribute("mensagemSucesso", "Você saiu com sucesso.");
        }

        return "login"; // → src/main/resources/templates/login.html
    }

    /**
     * Dashboard — página inicial após o login.
     */
    @GetMapping("/dashboard")
    public String dashboard() {
        return "dashboard"; // → src/main/resources/templates/dashboard.html
    }

    /**
     * Redireciona "/" para o dashboard (que o Security vai redirecionar
     * para login se não estiver autenticado).
     */
    @GetMapping("/")
    public String root() {
        return "redirect:/dashboard";
    }
}
