package com.arthurberwanger.microbio.repository;

import com.arthurberwanger.microbio.model.Pessoa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PessoaRepository extends JpaRepository<Pessoa, Long> {

    @Query("SELECT p FROM Pessoa p LEFT JOIN FETCH p.cliente ORDER BY p.dataSol DESC")
    List<Pessoa> findAllComCliente();

    Optional<Pessoa> findFirstByClienteOrderByDataSolAsc(com.arthurberwanger.microbio.model.Cliente cliente);

}