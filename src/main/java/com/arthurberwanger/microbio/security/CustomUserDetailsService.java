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
 * O Security chama loadUserByUsername passando o que o usuário digitou no campo "username".
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

        // Retorna um UserDetails com login, senha (hash) e role padrão
        return User.builder()
                .username(usuario.getLogin())
                .password(usuario.getSenha())   // já está em BCrypt no banco
                .roles("USER")                  // pode evoluir para roles no banco depois
                .build();
    }
}
