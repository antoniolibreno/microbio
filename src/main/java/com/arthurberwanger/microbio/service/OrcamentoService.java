package com.arthurberwanger.microbio.service;

import com.arthurberwanger.microbio.dto.OrcamentoDTO;
import com.arthurberwanger.microbio.model.Orcamento;
import com.arthurberwanger.microbio.model.Orcamento.StatusOrcamento;
import com.arthurberwanger.microbio.model.Pessoa;
import com.arthurberwanger.microbio.repository.OrcamentoRepository;
import com.arthurberwanger.microbio.repository.PessoaRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class OrcamentoService {

    private final OrcamentoRepository orcamentoRepository;
    private final PessoaRepository    pessoaRepository;

    public OrcamentoService(OrcamentoRepository orcamentoRepository,
                            PessoaRepository pessoaRepository) {
        this.orcamentoRepository = orcamentoRepository;
        this.pessoaRepository    = pessoaRepository;
    }

    // ── Solicitações públicas (site institucional) ─────────────────────────

    /** Recebe uma solicitação do site, cria a Pessoa e já gera o Orçamento PENDENTE vinculado. */
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

    /** Cria um orçamento formal a partir de uma solicitação (Pessoa). */
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

    /** Atualiza status, valor total e observações em uma única chamada. */
    @Transactional
    public Orcamento atualizar(Long id, StatusOrcamento status, BigDecimal valorTotal, String observacoes) {
        Orcamento orc = buscarPorId(id);
        if (status != null)      orc.setStatus(status);
        if (valorTotal != null)  orc.setValorTotal(valorTotal);
        orc.setObservacoes(observacoes);
        return orcamentoRepository.save(orc);
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