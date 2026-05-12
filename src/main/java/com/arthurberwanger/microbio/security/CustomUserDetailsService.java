package com.arthurberwanger.microbio.security;

import com.arthurberwanger.microbio.model.Usuario;
import com.arthurberwanger.microbio.repository.UsuarioRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Ponte entre o Spring Security e o banco de dados.
 * Atribui ROLE_ADMIN para usuários com is_admin = true,
 * e ROLE_USER para os demais (clientes).
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public CustomUserDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByLogin(login)
                .orElseThrow(() ->
                        new UsernameNotFoundException("Usuário não encontrado: " + login));

        String role = usuario.isAdmin() ? "ADMIN" : "USER";

        return User.builder()
                .username(usuario.getLogin())
                .password(usuario.getSenha())
                .roles(role)   // Spring adiciona o prefixo ROLE_ automaticamente
                .build();
    }
}