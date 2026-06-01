package com.example.NEO_ENERGY.service;

import com.example.NEO_ENERGY.exception.RecursoNaoEncontradoException;
import com.example.NEO_ENERGY.objects.model.RefreshTokenEntity;
import com.example.NEO_ENERGY.objects.model.UsuarioEntity;
import com.example.NEO_ENERGY.objects.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository repository;

    // Duração configurada em application.yaml em ms. Default 7 dias caso a chave esteja ausente.
    @Value("${app.jwt.refresh-expiration-ms:604800000}")
    private long refreshExpirationMs;

    private static final SecureRandom RNG = new SecureRandom();

    // Cria um novo refresh token para o usuário. Token é uma string aleatória de 64 bytes em Base64.
    public RefreshTokenEntity criar(UsuarioEntity usuario) {
        RefreshTokenEntity token = new RefreshTokenEntity();
        token.setUsuario(usuario);
        token.setToken(gerarTokenAleatorio());
        token.setCriadoEm(LocalDateTime.now());
        token.setExpiraEm(LocalDateTime.now().plusNanos(refreshExpirationMs * 1_000_000L));
        token.setRevogado(false);
        return repository.save(token);
    }

    // Valida um refresh token recebido do cliente. Lança se inválido, revogado ou expirado.
    public RefreshTokenEntity validar(String tokenString) {
        RefreshTokenEntity token = repository.findByToken(tokenString)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Refresh token inválido."));

        if (token.isRevogado()) {
            throw new IllegalStateException("Refresh token já foi revogado.");
        }
        if (token.getExpiraEm().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Refresh token expirado.");
        }
        return token;
    }

    // Rotação: revoga o token antigo e emite um novo para o mesmo usuário.
    // Boas práticas modernas exigem rotação a cada refresh para limitar dano se vazar.
    public RefreshTokenEntity rotacionar(RefreshTokenEntity antigo) {
        antigo.setRevogado(true);
        repository.save(antigo);
        return criar(antigo.getUsuario());
    }

    // Logout: revoga TODOS os refresh tokens ativos do usuário (mata todas as sessões).
    public void revogarTodosDe(UsuarioEntity usuario) {
        List<RefreshTokenEntity> ativos = repository.findAllByUsuarioAndRevogadoFalse(usuario);
        ativos.forEach(t -> t.setRevogado(true));
        repository.saveAll(ativos);
    }

    private String gerarTokenAleatorio() {
        byte[] bytes = new byte[64];
        RNG.nextBytes(bytes);
        // URL-safe Base64 sem padding — fica seguro pra enviar em JSON/header/URL.
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
