package com.arthurberwanger.microbio.repository;

import com.arthurberwanger.microbio.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByLogin(String login);

    /*
     * Busca um usuário já com cliente e endereço carregados (JOIN FETCH).
     * Isso evita LazyInitializationException ao acessar usuario.getCliente().getEndereco()
     * nos templates Thymeleaf fora do contexto transacional.
     */
    @Query("""
            SELECT u FROM Usuario u
            LEFT JOIN FETCH u.cliente c
            LEFT JOIN FETCH c.endereco
            WHERE u.id = :id
            """)
    Optional<Usuario> findByIdComCliente(@Param("id") Long id);

    /*
     * Lista todos os usuários já com cliente e endereço carregados.
     * Usado na página de lista para exibir o badge de "Completo" / "Só acesso".
     */
    @Query("""
            SELECT u FROM Usuario u
            LEFT JOIN FETCH u.cliente c
            LEFT JOIN FETCH c.endereco
            ORDER BY u.id
            """)
    List<Usuario> findAllComCliente();
}