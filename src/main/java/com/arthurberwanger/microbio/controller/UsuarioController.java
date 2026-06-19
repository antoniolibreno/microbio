package com.arthurberwanger.microbio.controller;

import com.arthurberwanger.microbio.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/usuarios")
@Tag(name = "Usuários", description = "Cadastro, edição e exclusão de usuários do sistema.")
public class UsuarioController {

    private final UsuarioService usuarioService;
    public UsuarioController(UsuarioService s) { this.usuarioService = s; }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("usuarios", usuarioService.listarTodos());
        return "usuarios/lista";
    }

    @Operation(
            summary = "Cadastra um usuário (modo simples)",
            description = "Cria um novo usuário apenas com login, senha e flag de administrador. Valida confirmação de senha e tamanho mínimo de 6 caracteres."
    )
    @PostMapping("/salvar-simples")
    public String salvarSimples(@RequestParam String login,
                                @RequestParam String senha,
                                @RequestParam String confirmarSenha,
                                @RequestParam(defaultValue = "false") boolean admin,
                                RedirectAttributes ra) {
        if (login.isBlank() || senha.isBlank()) { ra.addFlashAttribute("erro","Login e senha obrigatórios."); return "redirect:/usuarios"; }
        if (!senha.equals(confirmarSenha))       { ra.addFlashAttribute("erro","Senhas não coincidem.");       return "redirect:/usuarios"; }
        if (senha.length() < 6)                  { ra.addFlashAttribute("erro","Senha: mínimo 6 caracteres."); return "redirect:/usuarios"; }
        try {
            usuarioService.criarUsuarioSimples(login, senha, admin);
            ra.addFlashAttribute("sucesso", "Usuário criado com sucesso!");
            return "redirect:/usuarios";
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("erro", e.getMessage());
            return "redirect:/usuarios";
        }
    }

    @Operation(
            summary = "Cadastra um usuário (modo completo)",
            description = "Cria um usuário com dados de acesso e dados cadastrais (tipo de cliente, CPF/CNPJ e endereço completo)."
    )
    @PostMapping("/salvar-completo")
    public String salvarCompleto(@RequestParam String login,
                                 @RequestParam String senha,
                                 @RequestParam String confirmarSenha,
                                 @RequestParam(defaultValue = "false") boolean admin,
                                 @RequestParam(required=false) String tipoCliente,
                                 @RequestParam(required=false) String cpfCnpj,
                                 @RequestParam(required=false) String rua,
                                 @RequestParam(required=false) String numero,
                                 @RequestParam(required=false) String bairro,
                                 @RequestParam(required=false) String cidade,
                                 @RequestParam(required=false) String estado,
                                 @RequestParam(required=false) String cep,
                                 RedirectAttributes ra) {
        if (login.isBlank() || senha.isBlank()) { ra.addFlashAttribute("erro","Login e senha obrigatórios."); return "redirect:/usuarios"; }
        if (!senha.equals(confirmarSenha))       { ra.addFlashAttribute("erro","Senhas não coincidem.");       return "redirect:/usuarios"; }
        if (senha.length() < 6)                  { ra.addFlashAttribute("erro","Senha: mínimo 6 caracteres."); return "redirect:/usuarios"; }
        try {
            usuarioService.criarUsuarioCompleto(login, senha, admin, tipoCliente, cpfCnpj, rua, numero, bairro, cidade, estado, cep);
            ra.addFlashAttribute("sucesso", "Usuário cadastrado com sucesso!");
            return "redirect:/usuarios";
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("erro", e.getMessage());
            return "redirect:/usuarios";
        }
    }

    @PostMapping("/{id}/atualizar-acesso")
    public String atualizarAcesso(@PathVariable Long id,
                                  @RequestParam String login,
                                  @RequestParam(required=false) String senha,
                                  @RequestParam(required=false) String confirmarSenha,
                                  @RequestParam(defaultValue = "false") boolean admin,
                                  RedirectAttributes ra) {
        if (login.isBlank()) { ra.addFlashAttribute("erro","Login obrigatório."); return "redirect:/usuarios"; }
        if (senha != null && !senha.isBlank()) {
            if (!senha.equals(confirmarSenha)) { ra.addFlashAttribute("erro","Senhas não coincidem."); return "redirect:/usuarios"; }
            if (senha.length() < 6)            { ra.addFlashAttribute("erro","Senha: mínimo 6 caracteres."); return "redirect:/usuarios"; }
        }
        try {
            usuarioService.atualizarAcesso(id, login, senha, admin);
            ra.addFlashAttribute("sucesso", "Usuário atualizado!");
            return "redirect:/usuarios";
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("erro", e.getMessage());
            return "redirect:/usuarios";
        }
    }

    @PostMapping("/{id}/atualizar-completo")
    public String atualizarCompleto(@PathVariable Long id,
                                    @RequestParam String login,
                                    @RequestParam(required=false) String senha,
                                    @RequestParam(required=false) String confirmarSenha,
                                    @RequestParam(defaultValue = "false") boolean admin,
                                    @RequestParam(required=false) String tipoCliente,
                                    @RequestParam(required=false) String cpfCnpj,
                                    @RequestParam(required=false) String rua,
                                    @RequestParam(required=false) String numero,
                                    @RequestParam(required=false) String bairro,
                                    @RequestParam(required=false) String cidade,
                                    @RequestParam(required=false) String estado,
                                    @RequestParam(required=false) String cep,
                                    RedirectAttributes ra) {
        if (login.isBlank()) { ra.addFlashAttribute("erro","Login obrigatório."); return "redirect:/usuarios"; }
        if (senha != null && !senha.isBlank()) {
            if (!senha.equals(confirmarSenha)) { ra.addFlashAttribute("erro","Senhas não coincidem."); return "redirect:/usuarios"; }
            if (senha.length() < 6)            { ra.addFlashAttribute("erro","Senha: mínimo 6 caracteres."); return "redirect:/usuarios"; }
        }
        try {
            usuarioService.atualizarCompleto(id, login, senha, admin, tipoCliente, cpfCnpj, rua, numero, bairro, cidade, estado, cep);
            ra.addFlashAttribute("sucesso", "Cadastro atualizado!");
            return "redirect:/usuarios";
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("erro", e.getMessage());
            return "redirect:/usuarios";
        }
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id, RedirectAttributes ra) {
        try { usuarioService.excluir(id); ra.addFlashAttribute("sucesso","Usuário excluído."); }
        catch (IllegalArgumentException e) { ra.addFlashAttribute("erro", e.getMessage()); }
        return "redirect:/usuarios";
    }
}
