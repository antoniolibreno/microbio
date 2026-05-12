package com.arthurberwanger.microbio.repository;

import com.arthurberwanger.microbio.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    List<Pedido> findByOrcamentoIdOrderByDataPedidoDesc(Long orcamentoId);
}