package com.arthurberwanger.microbio.repository;

import com.arthurberwanger.microbio.model.PedidoAnalise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PedidoAnaliseRepository extends JpaRepository<PedidoAnalise, Long> {

    List<PedidoAnalise> findByPedidoId(Long pedidoId);
}