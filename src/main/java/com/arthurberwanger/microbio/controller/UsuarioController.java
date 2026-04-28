package com.arthurberwanger.microbio.controller;

import com.arthurberwanger.microbio.model.Usuario;
import com.arthurberwanger.microbio.service.UsuarioService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    // ── GET /usuarios → lista todos ──────────────────────────────────
    @GetMapping
    public String listar(Model model, RedirectAttributes redirectAttributes) {
        List<Usuario> usuarios = usuarioService.listarTodos();
        model.addAttribute("usuarios", usuarios);
        return "usuarios/lista"; // templates/usuarios/lista.html
    }

    // ── GET /usuarios/novo → exibe formulário de cadastro ────────────
    @GetMapping("/novo")
    public String paginaCadastro() {
        return "usuarios/novo"; // templates/usuarios/novo.html
    }

    // ── POST /usuarios/salvar-simples → só login + senha ─────────────
    @PostMapping("/salvar-simples")
    public String salvarSimples(
            @RequestParam String login,
            @RequestParam String senha,
            @RequestParam String confirmarSenha,
            RedirectAttributes ra) {

        if (login.isBlank() || senha.isBlank()) {
            ra.addFlashAttribute("erro", "Login e senha são obrigatórios.");
            return "redirect:/usuarios/novo";
        }
        if (!senha.equals(confirmarSenha)) {
            ra.addFlashAttribute("erro", "As senhas não coincidem.");
            return "redirect:/usuarios/novo";
        }
        if (senha.length() < 6) {
            ra.addFlashAttribute("erro", "A senha deve ter pelo menos 6 caracteres.");
            return "redirect:/usuarios/novo";
        }

        try {
            usuarioService.criarUsuarioSimples(login, senha);
            ra.addFlashAttribute("sucesso", "Usuário '" + login + "' criado com sucesso!");
            return "redirect:/usuarios";
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("erro", e.getMessage());
            return "redirect:/usuarios/novo";
        }
    }

    // ── POST /usuarios/salvar-completo → usuário + cliente + endereço ─
    @PostMapping("/salvar-completo")
    public String salvarCompleto(
            @RequestParam String login,
            @RequestParam String senha,
            @RequestParam String confirmarSenha,
            @RequestParam(required = false) String tipoCliente,
            @RequestParam(required = false) String cpfCnpj,
            @RequestParam(required = false) String rua,
            @RequestParam(required = false) String numero,
            @RequestParam(required = false) String bairro,
            @RequestParam(required = false) String cidade,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String cep,
            RedirectAttributes ra) {

        if (login.isBlank() || senha.isBlank()) {
            ra.addFlashAttribute("erro", "Login e senha são obrigatórios.");
            return "redirect:/usuarios/novo";
        }
        if (!senha.equals(confirmarSenha)) {
            ra.addFlashAttribute("erro", "As senhas não coincidem.");
            return "redirect:/usuarios/novo";
        }
        if (senha.length() < 6) {
            ra.addFlashAttribute("erro", "A senha deve ter pelo menos 6 caracteres.");
            return "redirect:/usuarios/novo";
        }

        try {
            usuarioService.criarUsuarioCompleto(login, senha, tipoCliente, cpfCnpj,
                    rua, numero, bairro, cidade, estado, cep);
            ra.addFlashAttribute("sucesso", "Usuário '" + login + "' cadastrado com sucesso!");
            return "redirect:/usuarios";
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("erro", e.getMessage());
            return "redirect:/usuarios/novo";
        }
    }

    // ── GET /usuarios/{id}/editar → exibe formulário de edição ───────
    @GetMapping("/{id}/editar")
    public String paginaEditar(@PathVariable Long id, Model model, RedirectAttributes ra) {
        try {
            Usuario usuario = usuarioService.buscarPorId(id);
            model.addAttribute("usuario", usuario);
            return "usuarios/editar"; // templates/usuarios/editar.html
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("erro", e.getMessage());
            return "redirect:/usuarios";
        }
    }

    // ── POST /usuarios/{id}/atualizar → salva edição ─────────────────
    @PostMapping("/{id}/atualizar")
    public String atualizar(
            @PathVariable Long id,
            @RequestParam String login,
            @RequestParam(required = false) String senha,
            @RequestParam(required = false) String confirmarSenha,
            RedirectAttributes ra) {

        if (login.isBlank()) {
            ra.addFlashAttribute("erro", "Login não pode ser vazio.");
            return "redirect:/usuarios/" + id + "/editar";
        }

        // Só valida senha se o campo foi preenchido
        if (senha != null && !senha.isBlank()) {
            if (!senha.equals(confirmarSenha)) {
                ra.addFlashAttribute("erro", "As senhas não coincidem.");
                return "redirect:/usuarios/" + id + "/editar";
            }
            if (senha.length() < 6) {
                ra.addFlashAttribute("erro", "A senha deve ter pelo menos 6 caracteres.");
                return "redirect:/usuarios/" + id + "/editar";
            }
        }

        try {
            usuarioService.atualizar(id, login, senha);
            ra.addFlashAttribute("sucesso", "Usuário atualizado com sucesso!");
            return "redirect:/usuarios";
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("erro", e.getMessage());
            return "redirect:/usuarios/" + id + "/editar";
        }
    }

    // ── POST /usuarios/{id}/excluir → exclui usuário ─────────────────
    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id, RedirectAttributes ra) {
        try {
            usuarioService.excluir(id);
            ra.addFlashAttribute("sucesso", "Usuário excluído com sucesso.");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/usuarios";
    }
}