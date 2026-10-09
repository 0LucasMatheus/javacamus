package com.umc.javacamus.repository;

import com.umc.javacamus.entity.Usuario;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

// spring data gera a query sozinho so pelo nome do metodo, sem precisar escrever nada
public interface UsuarioRepository extends MongoRepository<Usuario, String> {
    Optional<Usuario> findByUsername(String username);
    Optional<Usuario> findByEmail(String email);
    boolean existsByUsername(String username);
}
