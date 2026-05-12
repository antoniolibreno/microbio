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

import java.util.List;

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

    // ── Contadores para o dashboard ───────────────────────────────────────

    public long contarTodos()         { return orcamentoRepository.count(); }
    public long contarPorStatus(StatusOrcamento s) {
        return orcamentoRepository.findAllComDetalhes().stream()
                .filter(o -> o.getStatus() == s).count();
    }
}