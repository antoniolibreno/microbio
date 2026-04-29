package com.arthurberwanger.microbio.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Define as regras de acesso de toda a aplicação.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        // Rotas públicas (não precisam de login)
                        .requestMatchers("/login", "/css/**", "/js/**", "/images/**").permitAll()
                        // Todo o resto exige autenticação
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")               // URL da nossa página de login customizada
                        .loginProcessingUrl("/login")      // URL que recebe o POST (Spring Security intercepta)
                        .usernameParameter("username")     // nome do campo no HTML
                        .passwordParameter("password")     // nome do campo no HTML
                        .defaultSuccessUrl("/dashboard", true) // para onde vai após login ok
                        .failureUrl("/login?erro=true")    // para onde vai se errar a senha
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")              // URL do POST de logout
                        .logoutSuccessUrl("/login?logout=true") // após logout, vai para o login com msg
                        .invalidateHttpSession(true)       // invalida a sessão
                        .deleteCookies("JSESSIONID")       // limpa o cookie
                        .permitAll()
                );

        return http.build();
    }

    /**
     * BCrypt é o algoritmo de hash recomendado para senhas.
     * Nunca armazene senha em texto puro!
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * AuthenticationManager exposto como Bean — necessário caso queiramos
     * autenticar programaticamente (ex: endpoint REST de login futuramente).
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
