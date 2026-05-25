package com.arthurberwanger.microbio.service;

import com.arthurberwanger.microbio.model.Orcamento;
import com.arthurberwanger.microbio.model.Pedido;
import com.arthurberwanger.microbio.repository.OrcamentoRepository;
import com.arthurberwanger.microbio.repository.PedidoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PedidoService {

    private final PedidoRepository    pedidoRepository;
    private final OrcamentoRepository orcamentoRepository;

    public PedidoService(PedidoRepository pedidoRepository,
                         OrcamentoRepository orcamentoRepository) {
        this.pedidoRepository    = pedidoRepository;
        this.orcamentoRepository = orcamentoRepository;
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

    /** Cria um pedido a partir de um orçamento existente. */
    @Transactional
    public Pedido criarDe(Long orcamentoId, String observacoes) {
        Orcamento orc = orcamentoRepository.findById(orcamentoId)
                .orElseThrow(() -> new EntityNotFoundException("Orçamento #" + orcamentoId + " não encontrado"));
        if (observacoes != null && !observacoes.isBlank()) {
            orc.setObservacoes(observacoes);
            orcamentoRepository.save(orc);
        }
        Pedido pedido = new Pedido();
        pedido.setOrcamento(orc);
        pedido.setStatus("PENDENTE");
        pedido.setObservacoes(observacoes != null && !observacoes.isBlank() ? observacoes : orc.getObservacoes());
        return pedidoRepository.save(pedido);
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

    public long contarTodos() { return pedidoRepository.count(); }

    public long contarPorStatus(String status) {
        return pedidoRepository.findAll().stream()
                .filter(p -> status.equals(p.getStatus())).count();
    }
}