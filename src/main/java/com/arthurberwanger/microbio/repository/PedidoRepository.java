package com.arthurberwanger.microbio.repository;

import com.arthurberwanger.microbio.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    List<Pedido> findByOrcamentoIdOrderByDataPedidoDesc(Long orcamentoId);

    @Query("""
        SELECT DISTINCT p FROM Pedido p
        LEFT JOIN FETCH p.orcamento o
        LEFT JOIN FETCH o.pessoa pe
        LEFT JOIN FETCH pe.cliente c
        LEFT JOIN FETCH o.usuario
        LEFT JOIN FETCH p.analises pa
        LEFT JOIN FETCH pa.analise
        WHERE p.id = :id
    """)
    Optional<Pedido> findByIdComDetalhes(@Param("id") Long id);
}