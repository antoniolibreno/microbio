package com.arthurberwanger.microbio.security;

import com.arthurberwanger.microbio.security.handler.LoginSuccessHandler;
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

    private final LoginSuccessHandler loginSuccessHandler;

    public SecurityConfig(LoginSuccessHandler loginSuccessHandler) {
        this.loginSuccessHandler = loginSuccessHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"))

                .authorizeHttpRequests(auth -> auth
                        // Públicos
                        .requestMatchers(
                                "/", "/login",
                                "/css/**", "/js/**", "/img/**", "/images/**", "/fonts/**",
                                "/api/solicitacoes"
                        ).permitAll()

                        // Admin
                        .requestMatchers(
                                "/dashboard", "/dashboard/**",
                                "/usuarios", "/usuarios/**",
                                "/indicadores", "/indicadores/**",
                                "/solicitacoes", "/solicitacoes/**",
                                "/pedidos", "/pedidos/**",
                                "/analises", "/analises/**",
                                "/clientes", "/clientes/**"
                        ).hasRole("ADMIN")

                        // Cliente
                        .requestMatchers("/painel", "/painel/**").hasRole("USER")

                        .anyRequest().authenticated()
                )

                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .usernameParameter("username")
                        .passwordParameter("password")
                        .successHandler(loginSuccessHandler)
                        .failureUrl("/login?erro=true")
                        .permitAll()
                )

                // logoutUrl() aceita apenas POST por padrão no Spring Security 6 — sem precisar de AntPathRequestMatcher
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )

                .exceptionHandling(ex -> ex
                        .accessDeniedPage("/painel")
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}