package com.arthurberwanger.microbio.repository;

import com.arthurberwanger.microbio.model.PedidoAnalise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PedidoAnaliseRepository extends JpaRepository<PedidoAnalise, Long> {
}