package com.umc.javacamus.service;

import com.umc.javacamus.entity.Role;
import com.umc.javacamus.entity.Usuario;
import com.umc.javacamus.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Optional<Usuario> buscarPorId(String id) {
        return usuarioRepository.findById(id);
    }

    public Optional<Usuario> buscarPorUsername(String username) {
        return usuarioRepository.findByUsername(username);
    }

    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    // valida os campos e dps cria
    public void criarUsuario(Usuario usuario) {
        if (usuario.getUsername() == null || usuario.getUsername().isBlank()) {
            throw new IllegalArgumentException("Informe um nome de usuário.");
        }
        if (usuario.getPassword() == null || usuario.getPassword().isBlank()) {
            throw new IllegalArgumentException("Informe uma senha.");
        }
        if (usuario.getPassword().length() < 6) {
            throw new IllegalArgumentException("A senha deve ter pelo menos 6 caracteres.");
        }
        if (usuario.getRole() == null) {
            throw new IllegalArgumentException("Selecione um perfil de acesso.");
        }
        if (usuario.getEmail() == null || usuario.getEmail().isBlank()) {
            throw new IllegalArgumentException("Informe um e-mail.");
        }
        if (usuarioRepository.existsByUsername(usuario.getUsername())) {
            throw new IllegalArgumentException("Já existe um usuário com esse nome.");
        }

        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        usuarioRepository.save(usuario);
    }

    public void atualizarUsuario(String id, Usuario usuarioAtualizado) {
        Usuario usuarioExistente = usuarioRepository.findById(id).orElse(null);
        if (usuarioExistente == null) {
            throw new IllegalArgumentException("Usuário não encontrado.");
        }

        if (usuarioAtualizado.getUsername() == null || usuarioAtualizado.getUsername().isBlank()) {
            throw new IllegalArgumentException("Informe um nome de usuário.");
        }
        if (usuarioAtualizado.getRole() == null) {
            throw new IllegalArgumentException("Selecione um perfil de acesso.");
        }
        if (!usuarioExistente.getUsername().equals(usuarioAtualizado.getUsername())
                && usuarioRepository.existsByUsername(usuarioAtualizado.getUsername())) {
            throw new IllegalArgumentException("Já existe um usuário com esse nome.");
        }

        if (usuarioAtualizado.getEmail() == null || usuarioAtualizado.getEmail().isBlank()) {
            throw new IllegalArgumentException("Informe um e-mail.");
        }

        usuarioExistente.setUsername(usuarioAtualizado.getUsername());
        usuarioExistente.setEmail(usuarioAtualizado.getEmail());
        usuarioExistente.setRole(usuarioAtualizado.getRole());

        // so troca a senha se veio uma nova, se vier em branco mantem a atual
        if (usuarioAtualizado.getPassword() != null && !usuarioAtualizado.getPassword().isBlank()) {
            if (usuarioAtualizado.getPassword().length() < 6) {
                throw new IllegalArgumentException("A senha deve ter pelo menos 6 caracteres.");
            }
            usuarioExistente.setPassword(passwordEncoder.encode(usuarioAtualizado.getPassword()));
        }

        usuarioRepository.save(usuarioExistente);
    }

    public void deletarUsuario(String id) {
        usuarioRepository.deleteById(id);
    }

    // roda no boot do app, se o admin ja existe nao mexe em nada
    public void garantirAdminPadrao(String username, String password, String email) {
        if (usuarioRepository.existsByUsername(username)) {
            return;
        }
        Usuario admin = new Usuario();
        admin.setUsername(username);
        admin.setEmail(email);
        admin.setPassword(passwordEncoder.encode(password));
        admin.setRole(Role.ADMIN);
        usuarioRepository.save(admin);
    }
}
