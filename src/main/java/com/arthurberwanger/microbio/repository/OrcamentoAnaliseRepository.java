package com.arthurberwanger.microbio.repository;

import com.arthurberwanger.microbio.model.OrcamentoAnalise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrcamentoAnaliseRepository extends JpaRepository<OrcamentoAnalise, Long> {

    @Query("SELECT oa FROM OrcamentoAnalise oa LEFT JOIN FETCH oa.analise WHERE oa.orcamento.id = :orcamentoId")
    List<OrcamentoAnalise> findByOrcamentoId(Long orcamentoId);
}
