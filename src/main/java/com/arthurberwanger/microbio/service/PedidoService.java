package com.arthurberwanger.microbio.service;

import com.arthurberwanger.microbio.model.Analise;
import com.arthurberwanger.microbio.model.Orcamento;
import com.arthurberwanger.microbio.model.OrcamentoAnalise;
import com.arthurberwanger.microbio.model.Pedido;
import com.arthurberwanger.microbio.model.PedidoAnalise;
import com.arthurberwanger.microbio.repository.AnaliseRepository;
import com.arthurberwanger.microbio.repository.OrcamentoAnaliseRepository;
import com.arthurberwanger.microbio.repository.OrcamentoRepository;
import com.arthurberwanger.microbio.repository.PedidoAnaliseRepository;
import com.arthurberwanger.microbio.repository.PedidoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Service
public class PedidoService {

    private final PedidoRepository          pedidoRepository;
    private final OrcamentoRepository       orcamentoRepository;
    private final AnaliseRepository         analiseRepository;
    private final PedidoAnaliseRepository   pedidoAnaliseRepository;
    private final OrcamentoAnaliseRepository orcamentoAnaliseRepository;

    public PedidoService(PedidoRepository pedidoRepository,
                         OrcamentoRepository orcamentoRepository,
                         AnaliseRepository analiseRepository,
                         PedidoAnaliseRepository pedidoAnaliseRepository,
                         OrcamentoAnaliseRepository orcamentoAnaliseRepository) {
        this.pedidoRepository           = pedidoRepository;
        this.orcamentoRepository        = orcamentoRepository;
        this.analiseRepository          = analiseRepository;
        this.pedidoAnaliseRepository    = pedidoAnaliseRepository;
        this.orcamentoAnaliseRepository = orcamentoAnaliseRepository;
    }

    public List<Pedido> listarTodos() {
        return pedidoRepository.findAll();
    }

    public List<Pedido> listarPorOrcamento(Long orcamentoId) {
        return pedidoRepository.findByOrcamentoIdOrderByDataPedidoDesc(orcamentoId);
    }

    public Pedido buscarPorId(Long id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Pedido #" + id + " não encontrado"));
    }

    public Pedido buscarComDetalhes(Long id) {
        return pedidoRepository.findByIdComDetalhes(id)
                .orElseThrow(() -> new EntityNotFoundException("Pedido #" + id + " não encontrado"));
    }

    public List<Pedido> listarRecentes(int limit) {
        return pedidoRepository.findAll(
                PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "dataPedido"))
        ).getContent();
    }

    /** Cria um pedido a partir de um orçamento existente, copiando as análises selecionadas. */
    @Transactional
    public Pedido criarDe(Long orcamentoId, String observacoes) {
        Orcamento orc = orcamentoRepository.findById(orcamentoId)
                .orElseThrow(() -> new EntityNotFoundException("Orçamento #" + orcamentoId + " não encontrado"));

        Pedido pedido = new Pedido();
        pedido.setOrcamento(orc);
        pedido.setStatus("PENDENTE");
        pedido.setObservacoes(observacoes != null && !observacoes.isBlank() ? observacoes : orc.getObservacoes());
        pedidoRepository.save(pedido);

        List<OrcamentoAnalise> orcAnalises = orcamentoAnaliseRepository.findByOrcamentoId(orcamentoId);
        for (OrcamentoAnalise oa : orcAnalises) {
            PedidoAnalise pa = new PedidoAnalise();
            pa.setPedido(pedido);
            pa.setAnalise(oa.getAnalise());
            // Herda o snapshot de preço do orçamento (não relê do catálogo) → totais idênticos.
            pa.setValorUnitario(oa.getValorUnitario() != null
                    ? oa.getValorUnitario()
                    : (oa.getAnalise() != null ? oa.getAnalise().getValor() : null));
            pedidoAnaliseRepository.save(pa);
        }

        Pedido recarregado = pedidoRepository.findByIdComDetalhes(pedido.getId()).orElse(pedido);
        recalcularValorTotal(recarregado);
        pedidoRepository.save(recarregado);
        return recarregado;
    }

    /**
     * "Ganha" o orçamento de forma atômica: cria o pedido (copiando as análises e seus
     * snapshots de preço) e marca o orçamento como CONCLUIDO. Tudo em uma transação —
     * se qualquer passo falhar, nada é persistido.
     */
    @Transactional
    public Pedido ganharOrcamento(Long orcamentoId, String observacoes) {
        Orcamento orc = orcamentoRepository.findById(orcamentoId)
                .orElseThrow(() -> new EntityNotFoundException("Orçamento #" + orcamentoId + " não encontrado"));
        if (orc.getStatus() == Orcamento.StatusOrcamento.CANCELADO)
            throw new IllegalStateException("Um orçamento cancelado não pode ser ganho.");
        if (orc.getStatus() == Orcamento.StatusOrcamento.CONCLUIDO)
            throw new IllegalStateException("Este orçamento já foi ganho.");
        if (orcamentoAnaliseRepository.findByOrcamentoId(orcamentoId).isEmpty())
            throw new IllegalStateException("Adicione ao menos uma análise ao orçamento antes de ganhá-lo.");

        Pedido pedido = criarDe(orcamentoId, observacoes);
        orc.setStatus(Orcamento.StatusOrcamento.CONCLUIDO);
        orcamentoRepository.save(orc);
        return pedido;
    }

    /** Avança o status: PENDENTE → EM_ANDAMENTO → CONCLUIDO */
    @Transactional
    public Pedido promoverStatus(Long id) {
        Pedido p = buscarComDetalhes(id);
        String proximo = switch (p.getStatus()) {
            case "PENDENTE"     -> "EM_ANDAMENTO";
            case "EM_ANDAMENTO" -> "CONCLUIDO";
            default -> throw new IllegalStateException("Pedido com status '" + p.getStatus() + "' não pode ser promovido.");
        };
        if ("CONCLUIDO".equals(proximo)) {
            assertPodeConcluir(p);
        }
        p.setStatus(proximo);
        return pedidoRepository.save(p);
    }

    /** Atualiza status + observações em uma única chamada. */
    @Transactional
    public Pedido atualizar(Long id, String status, String observacoes) {
        Pedido p = buscarComDetalhes(id);
        if (status != null && !status.isBlank()) {
            if ("CONCLUIDO".equals(status) && !"CONCLUIDO".equals(p.getStatus())) {
                assertPodeConcluir(p);
            }
            p.setStatus(status);
        }
        p.setObservacoes(observacoes);
        return pedidoRepository.save(p);
    }

    /** Cancela o pedido. */
    @Transactional
    public Pedido cancelar(Long id) {
        Pedido p = buscarPorId(id);
        if ("CONCLUIDO".equals(p.getStatus()))
            throw new IllegalStateException("Pedidos concluídos não podem ser cancelados.");
        p.setStatus("CANCELADO");
        return pedidoRepository.save(p);
    }

    @Transactional
    public PedidoAnalise adicionarAnalise(Long pedidoId, Long analiseId) {
        Pedido pedido = buscarPorId(pedidoId);
        if ("CONCLUIDO".equals(pedido.getStatus()) || "CANCELADO".equals(pedido.getStatus()))
            throw new IllegalStateException("Não é possível modificar análises de um pedido com status " + pedido.getStatus() + ".");

        boolean jaVinculada = pedido.getAnalises().stream()
                .anyMatch(pa -> pa.getAnalise().getId().equals(analiseId));
        if (jaVinculada)
            throw new IllegalStateException("Essa análise já está vinculada ao pedido.");

        Analise analise = analiseRepository.findById(analiseId)
                .orElseThrow(() -> new EntityNotFoundException("Análise #" + analiseId + " não encontrada"));

        if (!"ATIVA".equals(analise.getStatus()))
            throw new IllegalStateException("Análise '" + analise.getNome() + "' está inativa e não pode ser vinculada.");

        PedidoAnalise pa = new PedidoAnalise();
        pa.setPedido(pedido);
        pa.setAnalise(analise);
        pa.setValorUnitario(analise.getValor()); // congela o preço do catálogo neste instante
        PedidoAnalise salvo = pedidoAnaliseRepository.save(pa);

        atualizarTotaisAposMudancaAnalise(pedidoId);
        return salvo;
    }

    @Transactional
    public PedidoAnalise atualizarResultado(Long paId, String resultado, String valorReferencia,
                                            String conformidade, LocalDate dataRealizacao,
                                            String observacoes) {
        if (dataRealizacao != null && dataRealizacao.isAfter(LocalDate.now()))
            throw new IllegalArgumentException("Data de realização não pode ser futura.");

        PedidoAnalise pa = pedidoAnaliseRepository.findById(paId)
                .orElseThrow(() -> new EntityNotFoundException("Vínculo de análise não encontrado."));
        pa.setResultado(resultado != null && !resultado.isBlank() ? resultado : null);
        pa.setValorReferencia(valorReferencia != null && !valorReferencia.isBlank() ? valorReferencia : null);
        if (conformidade != null && !conformidade.isBlank()) pa.setConformidade(conformidade);
        pa.setDataRealizacao(dataRealizacao);
        pa.setObservacoes(observacoes != null && !observacoes.isBlank() ? observacoes : null);
        return pedidoAnaliseRepository.save(pa);
    }

    @Transactional
    public void removerAnalise(Long paId) {
        PedidoAnalise pa = pedidoAnaliseRepository.findById(paId)
                .orElseThrow(() -> new EntityNotFoundException("Vínculo de análise não encontrado."));
        Pedido pedido = pa.getPedido();
        if ("CONCLUIDO".equals(pedido.getStatus()) || "CANCELADO".equals(pedido.getStatus()))
            throw new IllegalStateException("Não é possível modificar análises de um pedido com status " + pedido.getStatus() + ".");
        Long pedidoId = pedido.getId();
        pedidoAnaliseRepository.delete(pa);
        atualizarTotaisAposMudancaAnalise(pedidoId);
    }

    public long contarTodos() { return pedidoRepository.count(); }

    public long contarPorStatus(String status) {
        return pedidoRepository.findAll().stream()
                .filter(p -> status.equals(p.getStatus())).count();
    }

    /** Recalcula valor_total do pedido somando os snapshots de preço das análises vinculadas.
     *  Consulta a tabela de junção diretamente (fonte da verdade), evitando coleções
     *  potencialmente desatualizadas na sessão do Hibernate. */
    private void recalcularValorTotal(Pedido pedido) {
        BigDecimal total = pedidoAnaliseRepository.findByPedidoId(pedido.getId()).stream()
                .map(PedidoAnalise::getValorUnitario)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
        pedido.setValorTotal(total);
    }

    /** Bloqueia conclusão de pedido com análises de conformidade PENDENTE. */
    private void assertPodeConcluir(Pedido p) {
        if (p.getAnalises() == null || p.getAnalises().isEmpty())
            throw new IllegalStateException("Não é possível concluir um pedido sem análises vinculadas.");
        boolean temPendente = p.getAnalises().stream()
                .anyMatch(pa -> "PENDENTE".equals(pa.getConformidade()));
        if (temPendente)
            throw new IllegalStateException("Não é possível concluir: existem análises com conformidade PENDENTE.");
    }

    /** Recarrega pedido (com análises) e recalcula seu total a partir dos snapshots. */
    private void atualizarTotaisAposMudancaAnalise(Long pedidoId) {
        Pedido recarregado = pedidoRepository.findByIdComDetalhes(pedidoId)
                .orElseThrow(() -> new EntityNotFoundException("Pedido #" + pedidoId + " não encontrado"));
        recalcularValorTotal(recarregado);
        pedidoRepository.save(recarregado);
    }
}
