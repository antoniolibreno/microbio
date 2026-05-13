package com.arthurberwanger.microbio.repository;

import com.arthurberwanger.microbio.model.Analise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnaliseRepository extends JpaRepository<Analise, Long> {
    long countByStatus(String status);
}
