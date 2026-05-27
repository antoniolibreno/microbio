package com.arthurberwanger.microbio.controller;

import com.arthurberwanger.microbio.dto.OrcamentoDTO;
import com.arthurberwanger.microbio.model.Cliente;
import com.arthurberwanger.microbio.model.Orcamento;
import com.arthurberwanger.microbio.model.Pedido;
import com.arthurberwanger.microbio.model.Pessoa;
import com.arthurberwanger.microbio.model.Usuario;
import com.arthurberwanger.microbio.repository.OrcamentoRepository;
import com.arthurberwanger.microbio.repository.PedidoRepository;
import com.arthurberwanger.microbio.repository.PessoaRepository;
import com.arthurberwanger.microbio.repository.UsuarioRepository;
import com.arthurberwanger.microbio.service.OrcamentoService;
import com.arthurberwanger.microbio.service.PedidoService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/painel")
public class PainelClienteController {

    private final UsuarioRepository    usuarioRepository;
    private final OrcamentoRepository  orcamentoRepository;
    private final PedidoRepository     pedidoRepository;
    private final PessoaRepository     pessoaRepository;
    private final OrcamentoService     orcamentoService;
    private final PedidoService        pedidoService;

    public PainelClienteController(UsuarioRepository usuarioRepository,
                                   OrcamentoRepository orcamentoRepository,
                                   PedidoRepository pedidoRepository,
                                   PessoaRepository pessoaRepository,
                                   OrcamentoService orcamentoService,
                                   PedidoService pedidoService) {
        this.usuarioRepository   = usuarioRepository;
        this.orcamentoRepository = orcamentoRepository;
        this.pedidoRepository    = pedidoRepository;
        this.pessoaRepository    = pessoaRepository;
        this.orcamentoService    = orcamentoService;
        this.pedidoService       = pedidoService;
    }

    // ── helpers ──────────────────────────────────────────────────────────

    private Usuario usuarioLogado(Authentication auth) {
        return usuarioRepository.findByLogin(auth.getName())
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));
    }

    /** Retorna todos os orçamentos vinculados ao usuário logado. */
    private List<Orcamento> orcamentosDoUsuario(Usuario usuario) {
        return orcamentoRepository.findAllComDetalhes().stream()
                .filter(o -> o.getUsuario() != null
                        && o.getUsuario().getId().equals(usuario.getId()))
                .toList();
    }

    // ── Início do painel ─────────────────────────────────────────────────

    @GetMapping
    public String inicio(Model model, Authentication auth) {
        Usuario usuario = usuarioLogado(auth);
        List<Orcamento> orcamentos = orcamentosDoUsuario(usuario);

        long totalOrcamentos = orcamentos.size();
        long totalPedidos    = orcamentos.stream()
                .mapToLong(o -> o.getPedidos().size()).sum();
        long pendentes       = orcamentos.stream()
                .filter(o -> o.getStatus() == Orcamento.StatusOrcamento.PENDENTE
                        || o.getStatus() == Orcamento.StatusOrcamento.EM_ANDAMENTO)
                .count();

        model.addAttribute("totalOrcamentos", totalOrcamentos);
        model.addAttribute("totalPedidos",    totalPedidos);
        model.addAttribute("pendentes",       pendentes);
        model.addAttribute("recentesOrcamentos", orcamentos.stream().limit(3).toList());
        return "painel/index";
    }

    // ── Orçamentos ───────────────────────────────────────────────────────

    @GetMapping("/orcamentos")
    public String listarOrcamentos(Model model, Authentication auth) {
        Usuario usuario = usuarioLogado(auth);
        model.addAttribute("orcamentos", orcamentosDoUsuario(usuario));
        return "painel/orcamentos";
    }

    @GetMapping("/orcamentos/{id}")
    public String verOrcamento(@PathVariable Long id, Model model, Authentication auth,
                               RedirectAttributes ra) {
        Usuario usuario = usuarioLogado(auth);
        Orcamento orc = orcamentoRepository.findByIdComDetalhes(id)
                .orElseThrow(() -> new EntityNotFoundException("Orçamento não encontrado"));

        // Garante que o orçamento pertence ao usuário logado
        if (orc.getUsuario() == null || !orc.getUsuario().getId().equals(usuario.getId())) {
            ra.addFlashAttribute("erro", "Orçamento não encontrado.");
            return "redirect:/painel/orcamentos";
        }

        model.addAttribute("orcamento", orc);
        return "painel/orcamento-detalhe";
    }

    /** Solicitar novo orçamento — GET mostra formulário */
    @GetMapping("/orcamentos/novo")
    public String novoOrcamentoForm(Model model, Authentication auth) {
        Usuario usuario = usuarioLogado(auth);
        Cliente cliente = usuario.getCliente();
        if (cliente != null) {
            pessoaRepository.findFirstByClienteOrderByDataSolAsc(cliente)
                    .ifPresent(p -> model.addAttribute("pessoaCliente", p));
        }
        model.addAttribute("dto", new OrcamentoDTO());
        return "painel/orcamento-novo";
    }

    /** Solicitar novo orçamento — POST cria a solicitação */
    @PostMapping("/orcamentos/novo")
    public String novoOrcamentoSubmit(@ModelAttribute OrcamentoDTO dto,
                                      Authentication auth,
                                      RedirectAttributes ra) {
        try {
            Usuario usuario = usuarioLogado(auth);
            Cliente cliente = usuario.getCliente();

            Pessoa pessoa = new Pessoa();
            pessoa.setTipoServico(dto.getTipoServico());

            if (cliente != null) {
                // Preenche com dados do cadastro já existente
                pessoaRepository.findFirstByClienteOrderByDataSolAsc(cliente).ifPresentOrElse(
                        p -> {
                            pessoa.setNome(p.getNome());
                            pessoa.setEmail(p.getEmail());
                            pessoa.setTelefone(p.getTelefone());
                        },
                        () -> pessoa.setNome(usuario.getLogin())
                );
                pessoa.setCliente(cliente);
            } else {
                pessoa.setNome(dto.getNome() != null ? dto.getNome().trim() : usuario.getLogin());
                pessoa.setEmail(dto.getEmail() != null ? dto.getEmail().trim() : null);
                pessoa.setTelefone(dto.getTelefone() != null ? dto.getTelefone().trim() : null);
            }
            pessoaRepository.save(pessoa);

            Orcamento orc = new Orcamento();
            orc.setPessoa(pessoa);
            orc.setUsuario(usuario);
            orcamentoRepository.save(orc);

            ra.addFlashAttribute("sucesso", "Solicitação enviada com sucesso! Em breve entraremos em contato.");
            return "redirect:/painel/orcamentos";
        } catch (Exception e) {
            ra.addFlashAttribute("erro", "Erro ao registrar solicitação. Tente novamente.");
            return "redirect:/painel/orcamentos/novo";
        }
    }

    // ── Pedidos ──────────────────────────────────────────────────────────

    @GetMapping("/pedidos")
    public String listarPedidos(Model model, Authentication auth) {
        Usuario usuario = usuarioLogado(auth);
        List<Orcamento> orcamentos = orcamentosDoUsuario(usuario);

        List<Pedido> pedidos = orcamentos.stream()
                .flatMap(o -> o.getPedidos().stream())
                .sorted((a, b) -> b.getDataPedido().compareTo(a.getDataPedido()))
                .toList();

        model.addAttribute("pedidos", pedidos);
        return "painel/pedidos";
    }

    @GetMapping("/pedidos/{id}")
    public String verPedido(@PathVariable Long id, Model model, Authentication auth,
                            RedirectAttributes ra) {
        Usuario usuario = usuarioLogado(auth);
        Pedido pedido;
        try {
            pedido = pedidoService.buscarComDetalhes(id);
        } catch (EntityNotFoundException e) {
            ra.addFlashAttribute("erro", "Pedido não encontrado.");
            return "redirect:/painel/pedidos";
        }

        Orcamento orc = pedido.getOrcamento();
        if (orc == null || orc.getUsuario() == null
                || !orc.getUsuario().getId().equals(usuario.getId())) {
            ra.addFlashAttribute("erro", "Pedido não encontrado.");
            return "redirect:/painel/pedidos";
        }

        model.addAttribute("pedido", pedido);
        return "painel/pedido-detalhe";
    }

    @GetMapping("/meus-dados")
    public String meusDados(Model model, Authentication auth) {
        Usuario usuario = usuarioLogado(auth);
        Cliente cliente = usuario.getCliente();
        Pessoa pessoaCliente = null;
        if (cliente != null) {
            pessoaCliente = pessoaRepository
                    .findFirstByClienteOrderByDataSolAsc(cliente).orElse(null);
        }
        model.addAttribute("usuario", usuario);
        model.addAttribute("cliente", cliente);
        model.addAttribute("pessoaCliente", pessoaCliente);
        return "painel/meus-dados";
    }
}