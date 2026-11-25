package com.umc.javacamus.service;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.umc.javacamus.entity.Role;
import com.umc.javacamus.entity.Usuario;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.stereotype.Service;

// gera o jwt que o microservico de chamados espera, assinado com o mesmo JWT_SECRET
@Service
public class TokenService {

    private static final Duration VALIDADE_TOKEN = Duration.ofMinutes(5);

    private final JwtEncoder encoder;

    public TokenService(@Value("${app.jwt.secret}") String jwtSecret) {
        SecretKeySpec chave = new SecretKeySpec(jwtSecret.getBytes(), "HmacSHA256");
        this.encoder = new NimbusJwtEncoder(new ImmutableSecret<>(chave));
    }

    public String gerar(Usuario usuario) {
        Instant agora = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(usuario.getId())
                .claim("nome", usuario.getUsername())
                .claim("permissoes", permissoes(usuario.getRole()))
                .issuedAt(agora)
                .expiresAt(agora.plus(VALIDADE_TOKEN))
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    // o microservico so conhece permissao, aqui que cada role vira as dela
    public List<String> permissoes(Role role) {
        if (role == Role.USER) {
            return List.of("chamados:abrir");
        }
        return List.of("chamados:abrir", "chamados:resolver");
    }
}
