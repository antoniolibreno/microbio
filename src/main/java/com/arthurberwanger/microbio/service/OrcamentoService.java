package com.arthurberwanger.microbio.service;

import com.arthurberwanger.microbio.dto.OrcamentoDTO;
import com.arthurberwanger.microbio.model.Analise;
import com.arthurberwanger.microbio.model.Cliente;
import com.arthurberwanger.microbio.model.Orcamento;
import com.arthurberwanger.microbio.model.Orcamento.StatusOrcamento;
import com.arthurberwanger.microbio.model.OrcamentoAnalise;
import com.arthurberwanger.microbio.model.Pessoa;
import com.arthurberwanger.microbio.model.Usuario;
import com.arthurberwanger.microbio.repository.AnaliseRepository;
import com.arthurberwanger.microbio.repository.OrcamentoAnaliseRepository;
import com.arthurberwanger.microbio.repository.OrcamentoRepository;
import com.arthurberwanger.microbio.repository.PessoaRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Service
public class OrcamentoService {

    private final OrcamentoRepository        orcamentoRepository;
    private final PessoaRepository           pessoaRepository;
    private final OrcamentoAnaliseRepository orcamentoAnaliseRepository;
    private final AnaliseRepository          analiseRepository;

    public OrcamentoService(OrcamentoRepository orcamentoRepository,
                            PessoaRepository pessoaRepository,
                            OrcamentoAnaliseRepository orcamentoAnaliseRepository,
                            AnaliseRepository analiseRepository) {
        this.orcamentoRepository        = orcamentoRepository;
        this.pessoaRepository           = pessoaRepository;
        this.orcamentoAnaliseRepository = orcamentoAnaliseRepository;
        this.analiseRepository          = analiseRepository;
    }

    // ── Solicitações públicas (site institucional) ─────────────────────────

    /** Recebe uma solicitação do site, cria a Pessoa e já gera o Orçamento PENDENTE vinculado.
     *  valorTotal fica null e será calculado quando o admin agregar as análises. */
    @Transactional
    public Orcamento registrarSolicitacao(OrcamentoDTO dto) {
        Pessoa pessoa = new Pessoa();
        pessoa.setNome(dto.getNome().trim());
        pessoa.setEmail(dto.getEmail() != null ? dto.getEmail().trim() : null);
        pessoa.setTelefone(dto.getTelefone() != null ? dto.getTelefone().trim() : null);
        pessoa.setTipoServico(dto.getTipoServico());
        pessoaRepository.save(pessoa);

        Orcamento orc = new Orcamento();
        orc.setPessoa(pessoa);
        return orcamentoRepository.save(orc);
    }

    /** Lista todas as solicitações de orçamento (tabela pessoa). */
    public List<Pessoa> listarSolicitacoes() {
        return pessoaRepository.findAllComCliente();
    }

    // ── Orçamentos formais (tabela orcamento) ──────────────────────────────

    public List<Orcamento> listarTodos() {
        return orcamentoRepository.findAllComDetalhes();
    }

    public Orcamento buscarPorId(Long id) {
        return orcamentoRepository.findByIdComDetalhes(id)
                .orElseThrow(() -> new EntityNotFoundException("Orçamento #" + id + " não encontrado"));
    }

    /** Avança o status do orçamento seguindo o fluxo:
     *  PENDENTE → EM_ANDAMENTO → CONCLUIDO  */
    @Transactional
    public Orcamento promoverStatus(Long id) {
        Orcamento orc = buscarPorId(id);
        StatusOrcamento atual = orc.getStatus();
        StatusOrcamento proximo = switch (atual) {
            case PENDENTE     -> StatusOrcamento.EM_ANDAMENTO;
            case EM_ANDAMENTO -> StatusOrcamento.CONCLUIDO;
            default           -> throw new IllegalStateException(
                    "Orçamento com status '" + atual.getLabel() + "' não pode ser promovido.");
        };
        orc.setStatus(proximo);
        return orcamentoRepository.save(orc);
    }

    /** Cancela um orçamento (PENDENTE ou EM_ANDAMENTO). */
    @Transactional
    public Orcamento cancelar(Long id) {
        Orcamento orc = buscarPorId(id);
        if (orc.getStatus() == StatusOrcamento.CONCLUIDO) {
            throw new IllegalStateException("Orçamentos concluídos não podem ser cancelados.");
        }
        orc.setStatus(StatusOrcamento.CANCELADO);
        return orcamentoRepository.save(orc);
    }

    /** Cria, de forma atômica, a solicitação de orçamento feita pelo cliente logado:
     *  Pessoa + Orçamento + vínculos de análise (com snapshot de preço) + total derivado. */
    @Transactional
    public Orcamento criarSolicitacaoCliente(Usuario usuario, List<Long> analiseIds, String observacoes) {
        Cliente cliente = usuario.getCliente();

        List<Analise> selecionadas = (analiseIds != null && !analiseIds.isEmpty())
                ? analiseRepository.findAllById(analiseIds)
                : List.of();

        Pessoa pessoa = new Pessoa();
        String tipoServico = selecionadas.stream()
                .map(Analise::getNome)
                .collect(java.util.stream.Collectors.joining(", "));
        pessoa.setTipoServico(tipoServico.isBlank() ? "Análise laboratorial" : tipoServico);

        if (cliente != null) {
            pessoaRepository.findFirstByClienteOrderByDataSolAsc(cliente).ifPresentOrElse(
                    p -> { pessoa.setNome(p.getNome()); pessoa.setEmail(p.getEmail()); pessoa.setTelefone(p.getTelefone()); },
                    () -> pessoa.setNome(usuario.getLogin())
            );
            pessoa.setCliente(cliente);
        } else {
            pessoa.setNome(usuario.getLogin());
        }
        pessoaRepository.save(pessoa);

        Orcamento orc = new Orcamento();
        orc.setPessoa(pessoa);
        orc.setUsuario(usuario);
        if (observacoes != null && !observacoes.isBlank()) orc.setObservacoes(observacoes);
        orcamentoRepository.save(orc);

        for (Analise a : selecionadas) {
            OrcamentoAnalise oa = new OrcamentoAnalise();
            oa.setOrcamento(orc);
            oa.setAnalise(a);
            oa.setValorUnitario(a.getValor()); // congela o preço do catálogo neste instante
            orcamentoAnaliseRepository.save(oa);
        }
        recalcularValorTotal(orc.getId());
        return orc;
    }

    /** Cria um orçamento formal a partir de uma solicitação (Pessoa).
     *  valorTotal fica null até que análises sejam adicionadas. */
    @Transactional
    public Orcamento criarDesolicitacao(Long pessoaId) {
        Pessoa pessoa = pessoaRepository.findById(pessoaId)
                .orElseThrow(() -> new EntityNotFoundException("Solicitação #" + pessoaId + " não encontrada"));
        Orcamento orc = new Orcamento();
        orc.setPessoa(pessoa);
        return orcamentoRepository.save(orc);
    }

    /** Atualiza o status do orçamento diretamente (qualquer transição). */
    @Transactional
    public Orcamento atualizarStatus(Long id, StatusOrcamento novoStatus) {
        Orcamento orc = buscarPorId(id);
        orc.setStatus(novoStatus);
        return orcamentoRepository.save(orc);
    }

    /** Atualiza status e observações. O valor total é sempre derivado das análises (não editável). */
    @Transactional
    public Orcamento atualizar(Long id, StatusOrcamento status, String observacoes) {
        Orcamento orc = buscarPorId(id);
        if (status != null) orc.setStatus(status);
        orc.setObservacoes(observacoes);
        return orcamentoRepository.save(orc);
    }

    // ── Análises do orçamento (montagem pelo admin) ────────────────────────

    public List<OrcamentoAnalise> listarAnalises(Long orcamentoId) {
        return orcamentoAnaliseRepository.findByOrcamentoId(orcamentoId);
    }

    /** Recalcula valor_total do orçamento somando os snapshots de preço das análises. */
    @Transactional
    public void recalcularValorTotal(Long orcamentoId) {
        Orcamento orc = orcamentoRepository.findById(orcamentoId)
                .orElseThrow(() -> new EntityNotFoundException("Orçamento #" + orcamentoId + " não encontrado"));
        BigDecimal total = orcamentoAnaliseRepository.findByOrcamentoId(orcamentoId).stream()
                .map(OrcamentoAnalise::getValorUnitario)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
        orc.setValorTotal(total);
        orcamentoRepository.save(orc);
    }

    @Transactional
    public OrcamentoAnalise adicionarAnalise(Long orcamentoId, Long analiseId) {
        Orcamento orc = buscarPorId(orcamentoId);
        if (orc.getStatus() == StatusOrcamento.CONCLUIDO || orc.getStatus() == StatusOrcamento.CANCELADO)
            throw new IllegalStateException("Não é possível modificar análises de um orçamento "
                    + orc.getStatus().getLabel().toLowerCase() + ".");

        boolean jaVinculada = orcamentoAnaliseRepository.findByOrcamentoId(orcamentoId).stream()
                .anyMatch(oa -> oa.getAnalise() != null && oa.getAnalise().getId().equals(analiseId));
        if (jaVinculada)
            throw new IllegalStateException("Essa análise já está no orçamento.");

        Analise analise = analiseRepository.findById(analiseId)
                .orElseThrow(() -> new EntityNotFoundException("Análise #" + analiseId + " não encontrada"));
        if (!"ATIVA".equals(analise.getStatus()))
            throw new IllegalStateException("Análise '" + analise.getNome() + "' está inativa e não pode ser vinculada.");

        OrcamentoAnalise oa = new OrcamentoAnalise();
        oa.setOrcamento(orc);
        oa.setAnalise(analise);
        oa.setValorUnitario(analise.getValor()); // congela o preço do catálogo neste instante
        OrcamentoAnalise salvo = orcamentoAnaliseRepository.save(oa);

        recalcularValorTotal(orcamentoId);
        return salvo;
    }

    @Transactional
    public void removerAnalise(Long orcamentoAnaliseId) {
        OrcamentoAnalise oa = orcamentoAnaliseRepository.findById(orcamentoAnaliseId)
                .orElseThrow(() -> new EntityNotFoundException("Vínculo de análise não encontrado."));
        Orcamento orc = oa.getOrcamento();
        if (orc.getStatus() == StatusOrcamento.CONCLUIDO || orc.getStatus() == StatusOrcamento.CANCELADO)
            throw new IllegalStateException("Não é possível modificar análises de um orçamento "
                    + orc.getStatus().getLabel().toLowerCase() + ".");
        Long orcamentoId = orc.getId();
        orcamentoAnaliseRepository.delete(oa);
        recalcularValorTotal(orcamentoId);
    }

    // ── Contadores para o dashboard ───────────────────────────────────────

    public long contarTodos()         { return orcamentoRepository.count(); }
    public long contarPorStatus(StatusOrcamento s) {
        return orcamentoRepository.findAllComDetalhes().stream()
                .filter(o -> o.getStatus() == s).count();
    }

    /** Últimos N orçamentos ordenados por data desc. */
    public List<Orcamento> listarRecentes(int limite) {
        return orcamentoRepository.findAllComDetalhes().stream()
                .limit(limite)
                .toList();
    }

    /** Contagem de orçamentos por mês nos últimos numMeses meses. */
    public List<Long> contagemPorMes(int numMeses) {
        List<Orcamento> todos = orcamentoRepository.findAllComDetalhes();
        List<Long> resultado = new ArrayList<>();
        LocalDateTime agora = LocalDateTime.now();
        for (int i = numMeses - 1; i >= 0; i--) {
            LocalDateTime inicio = agora.minusMonths(i)
                    .withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
            LocalDateTime fim = inicio.plusMonths(1);
            long count = todos.stream()
                    .filter(o -> !o.getDataOrcamento().isBefore(inicio) && o.getDataOrcamento().isBefore(fim))
                    .count();
            resultado.add(count);
        }
        return resultado;
    }

    /** Labels de mês/ano para os últimos numMeses meses (ex: "jan/25"). */
    public List<String> labelsMeses(int numMeses) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM/yy", new Locale("pt", "BR"));
        List<String> labels = new ArrayList<>();
        LocalDateTime agora = LocalDateTime.now();
        for (int i = numMeses - 1; i >= 0; i--) {
            labels.add(agora.minusMonths(i).format(fmt));
        }
        return labels;
    }
}