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

import java.util.List;

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

        // Copia as análises do orçamento para o pedido
        List<OrcamentoAnalise> orcAnalises = orcamentoAnaliseRepository.findByOrcamentoId(orcamentoId);
        for (OrcamentoAnalise oa : orcAnalises) {
            PedidoAnalise pa = new PedidoAnalise();
            pa.setPedido(pedido);
            pa.setAnalise(oa.getAnalise());
            pedidoAnaliseRepository.save(pa);
        }

        return pedido;
    }

    /** Avança o status: PENDENTE → EM_ANDAMENTO → CONCLUIDO */
    @Transactional
    public Pedido promoverStatus(Long id) {
        Pedido p = buscarPorId(id);
        String proximo = switch (p.getStatus()) {
            case "PENDENTE"     -> "EM_ANDAMENTO";
            case "EM_ANDAMENTO" -> "CONCLUIDO";
            default -> throw new IllegalStateException("Pedido com status '" + p.getStatus() + "' não pode ser promovido.");
        };
        p.setStatus(proximo);
        return pedidoRepository.save(p);
    }

    /** Atualiza status + observações em uma única chamada. */
    @Transactional
    public Pedido atualizar(Long id, String status, String observacoes) {
        Pedido p = buscarPorId(id);
        if (status != null && !status.isBlank()) p.setStatus(status);
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

        PedidoAnalise pa = new PedidoAnalise();
        pa.setPedido(pedido);
        pa.setAnalise(analise);
        return pedidoAnaliseRepository.save(pa);
    }

    @Transactional
    public PedidoAnalise atualizarResultado(Long paId, String resultado, String valorReferencia,
                                            String conformidade, java.time.LocalDate dataRealizacao,
                                            String observacoes) {
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
        pedidoAnaliseRepository.delete(pa);
    }

    public long contarTodos() { return pedidoRepository.count(); }

    public long contarPorStatus(String status) {
        return pedidoRepository.findAll().stream()
                .filter(p -> status.equals(p.getStatus())).count();
    }
}