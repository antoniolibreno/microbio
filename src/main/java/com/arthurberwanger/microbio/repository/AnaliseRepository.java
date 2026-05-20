package com.arthurberwanger.microbio.repository;

import com.arthurberwanger.microbio.model.Analise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AnaliseRepository extends JpaRepository<Analise, Long> {
    long countByStatus(String status);

    Optional<Analise> findFirstByNomeIgnoreCase(String nome);
}
