package com.umc.javacamus.service;

import com.umc.javacamus.entity.Usuario;
import com.umc.javacamus.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;

// gera e confere o codigo de 5 digitos usado na recuperacao de senha
@Service
public class PasswordResetService {

    private static final Duration VALIDADE_CODIGO = Duration.ofMinutes(10);

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final SecureRandom random = new SecureRandom();

    public PasswordResetService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, EmailService emailService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    // cria
    public void solicitarCodigo(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email).orElse(null);
        if (usuario == null) {
            return;
        }

        // codigo aleatorio de 5 digitos, de 00000 a 99999
        String codigo = String.format("%05d", random.nextInt(100000));

        // seta no user
        usuario.setResetCode(codigo);
        usuario.setResetCodeExpiresAt(Instant.now().plus(VALIDADE_CODIGO));
        usuarioRepository.save(usuario);


        // enviao email
        emailService.enviarCodigoRecuperacao(usuario.getEmail(), codigo);
    }

    // confere
    public void redefinirSenha(String email, String codigo, String novaSenha) {
        Usuario usuario = usuarioRepository.findByEmail(email).orElse(null);
        if (usuario == null) {
            throw new IllegalArgumentException("Solicitação inválida.");
        }

        if (usuario.getResetCode() == null || usuario.getResetCodeExpiresAt() == null) {
            throw new IllegalArgumentException("Solicite um novo código de recuperação.");
        }
        if (Instant.now().isAfter(usuario.getResetCodeExpiresAt())) {
            throw new IllegalArgumentException("Código expirado. Solicite um novo.");
        }
        if (!usuario.getResetCode().equals(codigo)) {
            throw new IllegalArgumentException("Código inválido.");
        }
        if (novaSenha == null || novaSenha.length() < 6) {
            throw new IllegalArgumentException("A nova senha deve ter pelo menos 6 caracteres.");
        }

        usuario.setPassword(passwordEncoder.encode(novaSenha));
        usuario.setResetCode(null);
        usuario.setResetCodeExpiresAt(null);
        usuarioRepository.save(usuario);
    }
}
