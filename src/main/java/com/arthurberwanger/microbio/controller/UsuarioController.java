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
    public UsuarioController(UsuarioService s) { this.usuarioService = s; }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("usuarios", usuarioService.listarTodos());
        return "usuarios/lista";
    }

    @GetMapping("/novo")
    public String paginaCadastro() { return "usuarios/novo"; }

    @PostMapping("/salvar-simples")
    public String salvarSimples(@RequestParam String login, @RequestParam String senha,
                                @RequestParam String confirmarSenha, RedirectAttributes ra) {
        if (login.isBlank() || senha.isBlank()) { ra.addFlashAttribute("erro","Login e senha obrigatórios."); return "redirect:/usuarios/novo"; }
        if (!senha.equals(confirmarSenha))       { ra.addFlashAttribute("erro","Senhas não coincidem.");       return "redirect:/usuarios/novo"; }
        if (senha.length() < 6)                  { ra.addFlashAttribute("erro","Senha: mínimo 6 caracteres."); return "redirect:/usuarios/novo"; }
        try { usuarioService.criarUsuarioSimples(login, senha); ra.addFlashAttribute("sucesso","Usuário criado!"); return "redirect:/usuarios"; }
        catch (IllegalArgumentException e) { ra.addFlashAttribute("erro", e.getMessage()); return "redirect:/usuarios/novo"; }
    }

    @PostMapping("/salvar-completo")
    public String salvarCompleto(@RequestParam String login, @RequestParam String senha,
                                 @RequestParam String confirmarSenha,
                                 @RequestParam(required=false) String tipoCliente,
                                 @RequestParam(required=false) String cpfCnpj,
                                 @RequestParam(required=false) String rua,
                                 @RequestParam(required=false) String numero,
                                 @RequestParam(required=false) String bairro,
                                 @RequestParam(required=false) String cidade,
                                 @RequestParam(required=false) String estado,
                                 @RequestParam(required=false) String cep,
                                 RedirectAttributes ra) {
        if (login.isBlank() || senha.isBlank()) { ra.addFlashAttribute("erro","Login e senha obrigatórios."); return "redirect:/usuarios/novo"; }
        if (!senha.equals(confirmarSenha))       { ra.addFlashAttribute("erro","Senhas não coincidem.");       return "redirect:/usuarios/novo"; }
        if (senha.length() < 6)                  { ra.addFlashAttribute("erro","Senha: mínimo 6 caracteres."); return "redirect:/usuarios/novo"; }
        try { usuarioService.criarUsuarioCompleto(login,senha,tipoCliente,cpfCnpj,rua,numero,bairro,cidade,estado,cep); ra.addFlashAttribute("sucesso","Cadastrado!"); return "redirect:/usuarios"; }
        catch (IllegalArgumentException e) { ra.addFlashAttribute("erro", e.getMessage()); return "redirect:/usuarios/novo"; }
    }

    @GetMapping("/{id}/visualizar")
    public String visualizar(@PathVariable Long id, Model model, RedirectAttributes ra) {
        try { model.addAttribute("usuario", usuarioService.buscarPorId(id)); return "usuarios/visualizar"; }
        catch (IllegalArgumentException e) { ra.addFlashAttribute("erro", e.getMessage()); return "redirect:/usuarios"; }
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model, RedirectAttributes ra) {
        try { model.addAttribute("usuario", usuarioService.buscarPorId(id)); return "usuarios/editar"; }
        catch (IllegalArgumentException e) { ra.addFlashAttribute("erro", e.getMessage()); return "redirect:/usuarios"; }
    }

    @PostMapping("/{id}/atualizar-acesso")
    public String atualizarAcesso(@PathVariable Long id, @RequestParam String login,
                                  @RequestParam(required=false) String senha,
                                  @RequestParam(required=false) String confirmarSenha,
                                  RedirectAttributes ra) {
        if (login.isBlank()) { ra.addFlashAttribute("erro","Login obrigatório."); return "redirect:/usuarios/"+id+"/editar"; }
        if (senha != null && !senha.isBlank()) {
            if (!senha.equals(confirmarSenha)) { ra.addFlashAttribute("erro","Senhas não coincidem."); return "redirect:/usuarios/"+id+"/editar"; }
            if (senha.length() < 6)            { ra.addFlashAttribute("erro","Senha: mínimo 6 caracteres."); return "redirect:/usuarios/"+id+"/editar"; }
        }
        try { usuarioService.atualizarAcesso(id, login, senha); ra.addFlashAttribute("sucesso","Acesso atualizado!"); return "redirect:/usuarios"; }
        catch (IllegalArgumentException e) { ra.addFlashAttribute("erro", e.getMessage()); return "redirect:/usuarios/"+id+"/editar"; }
    }

    @PostMapping("/{id}/atualizar-completo")
    public String atualizarCompleto(@PathVariable Long id, @RequestParam String login,
                                    @RequestParam(required=false) String senha,
                                    @RequestParam(required=false) String confirmarSenha,
                                    @RequestParam(required=false) String tipoCliente,
                                    @RequestParam(required=false) String cpfCnpj,
                                    @RequestParam(required=false) String rua,
                                    @RequestParam(required=false) String numero,
                                    @RequestParam(required=false) String bairro,
                                    @RequestParam(required=false) String cidade,
                                    @RequestParam(required=false) String estado,
                                    @RequestParam(required=false) String cep,
                                    RedirectAttributes ra) {
        if (login.isBlank()) { ra.addFlashAttribute("erro","Login obrigatório."); return "redirect:/usuarios/"+id+"/editar"; }
        if (senha != null && !senha.isBlank()) {
            if (!senha.equals(confirmarSenha)) { ra.addFlashAttribute("erro","Senhas não coincidem."); return "redirect:/usuarios/"+id+"/editar"; }
            if (senha.length() < 6)            { ra.addFlashAttribute("erro","Senha: mínimo 6 caracteres."); return "redirect:/usuarios/"+id+"/editar"; }
        }
        try { usuarioService.atualizarCompleto(id,login,senha,tipoCliente,cpfCnpj,rua,numero,bairro,cidade,estado,cep); ra.addFlashAttribute("sucesso","Cadastro atualizado!"); return "redirect:/usuarios"; }
        catch (IllegalArgumentException e) { ra.addFlashAttribute("erro", e.getMessage()); return "redirect:/usuarios/"+id+"/editar"; }
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id, RedirectAttributes ra) {
        try { usuarioService.excluir(id); ra.addFlashAttribute("sucesso","Usuário excluído."); }
        catch (IllegalArgumentException e) { ra.addFlashAttribute("erro", e.getMessage()); }
        return "redirect:/usuarios";
    }
}