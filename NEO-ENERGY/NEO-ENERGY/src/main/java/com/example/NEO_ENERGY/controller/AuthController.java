package com.example.NEO_ENERGY.controller;

import com.example.NEO_ENERGY.objects.model.RefreshTokenEntity;
import com.example.NEO_ENERGY.objects.model.UsuarioEntity;
import com.example.NEO_ENERGY.objects.repository.UsuarioRepository;
import com.example.NEO_ENERGY.security.JwtService;
import com.example.NEO_ENERGY.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    public record LoginRequest(String username, String senha) {}
    public record RefreshRequest(String refreshToken) {}
    public record LogoutRequest(String refreshToken) {}
    public record TokenResponse(
            String token,
            String refreshToken,
            String tipo,
            String username,
            String role
    ) {}

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody LoginRequest req) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.username(), req.senha()));
        } catch (BadCredentialsException ex) {
            return ResponseEntity.status(401).build();
        }

        UsuarioEntity usuario = usuarioRepository.findByLogin(req.username());
        String accessToken = jwtService.generateToken(usuario);
        RefreshTokenEntity refresh = refreshTokenService.criar(usuario);

        return ResponseEntity.ok(new TokenResponse(
                accessToken,
                refresh.getToken(),
                "Bearer",
                usuario.getLogin(),
                usuario.getRole().name()
        ));
    }

    // Troca o refresh por um novo par (access + refresh). O refresh antigo é revogado (rotação).
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@RequestBody RefreshRequest req) {
        RefreshTokenEntity antigo = refreshTokenService.validar(req.refreshToken());
        RefreshTokenEntity novo = refreshTokenService.rotacionar(antigo);

        UsuarioEntity usuario = novo.getUsuario();
        String accessToken = jwtService.generateToken(usuario);

        return ResponseEntity.ok(new TokenResponse(
                accessToken,
                novo.getToken(),
                "Bearer",
                usuario.getLogin(),
                usuario.getRole().name()
        ));
    }

    // Revoga TODAS as sessões ativas do dono do refresh token enviado.
    // Front deve descartar o access token também (não há revogação JWT — ele expira sozinho em 24h).
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody LogoutRequest req) {
        RefreshTokenEntity token = refreshTokenService.validar(req.refreshToken());
        refreshTokenService.revogarTodosDe(token.getUsuario());
        return ResponseEntity.noContent().build();
    }
}
