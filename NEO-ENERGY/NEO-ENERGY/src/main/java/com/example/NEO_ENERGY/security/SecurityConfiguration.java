package com.example.NEO_ENERGY.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfiguration {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // rotas publicas
                        .requestMatchers(HttpMethod.POST, "/usuarios").permitAll()
                        .requestMatchers("/login/**").permitAll()
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers("/error").permitAll()

                        // somente ADMIN gerencia usuarios
                        .requestMatchers("/usuarios/**").hasRole("ADMIN")

                        // CRUD dos objetos: ADMIN ou PRO
                        .requestMatchers(HttpMethod.POST, "/casa/**", "/irrigador/**", "/painel_solar/**", "/solo/**")
                            .hasAnyRole("ADMIN", "PRO")
                        .requestMatchers(HttpMethod.PUT, "/casa/**", "/irrigador/**", "/painel_solar/**", "/solo/**")
                            .hasAnyRole("ADMIN", "PRO")
                        .requestMatchers(HttpMethod.PATCH, "/casa/**", "/irrigador/**", "/painel_solar/**", "/solo/**")
                            .hasAnyRole("ADMIN", "PRO")
                        .requestMatchers(HttpMethod.DELETE, "/casa/**", "/irrigador/**", "/painel_solar/**", "/solo/**")
                            .hasRole("ADMIN")

                        // leitura: qualquer usuario autenticado
                        .anyRequest().authenticated()
                )
                .httpBasic(httpBasic -> {})
                .headers(headers -> headers.frameOptions(frame -> frame.disable()));

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
