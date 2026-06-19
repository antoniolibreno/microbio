package com.arthurberwanger.microbio.repository;

import com.arthurberwanger.microbio.model.Orcamento;
import com.arthurberwanger.microbio.model.Orcamento.StatusOrcamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrcamentoRepository extends JpaRepository<Orcamento, Long> {

    @Query("SELECT COALESCE(SUM(o.valorTotal), 0) FROM Orcamento o WHERE o.status = :status")
    BigDecimal somarValorTotalPorStatus(@Param("status") StatusOrcamento status);

    @Query("""
        SELECT o FROM Orcamento o
        LEFT JOIN FETCH o.pessoa p
        LEFT JOIN FETCH o.usuario u
        ORDER BY o.dataOrcamento DESC
    """)
    List<Orcamento> findAllComDetalhes();

    @Query("""
        SELECT DISTINCT o FROM Orcamento o
        LEFT JOIN FETCH o.pessoa p
        LEFT JOIN FETCH p.cliente c
        LEFT JOIN FETCH c.endereco
        LEFT JOIN FETCH o.usuario
        LEFT JOIN FETCH o.pedidos
        WHERE o.id = :id
    """)
    Optional<Orcamento> findByIdComDetalhes(Long id);
}