package com.arthurberwanger.microbio.repository;

import com.arthurberwanger.microbio.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // Spring Data gera o SQL automaticamente pelo nome do método
    Optional<Usuario> findByLogin(String login);
}
