package com.arthurberwanger.microbio.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {
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

        return "login";
    }

    /**
     * Dashboard — página inicial após o login.
     */
    @GetMapping("/dashboard")
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
