package com.umc.javacamus.config;

import com.umc.javacamus.entity.Role;
import com.umc.javacamus.service.UsuarioService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// cria o admin, o tecnico e o usuario padrao quando sobe o app, usando os valores do .env
@Configuration
public class DataInitializer {

    @Value("${app.admin.username}")
    private String adminUsername;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.tecnico.username}")
    private String tecnicoUsername;

    @Value("${app.tecnico.password}")
    private String tecnicoPassword;

    @Value("${app.tecnico.email}")
    private String tecnicoEmail;

    @Value("${app.usuario.username}")
    private String usuarioUsername;

    @Value("${app.usuario.password}")
    private String usuarioPassword;

    @Value("${app.usuario.email}")
    private String usuarioEmail;

    // roda no boot, se a conta ja existir nao faz nada
    @Bean
    CommandLineRunner initDatabase(UsuarioService usuarioService) {
        return args -> {
            usuarioService.garantirUsuarioPadrao(adminUsername, adminPassword, adminEmail, Role.ADMIN);
            usuarioService.garantirUsuarioPadrao(tecnicoUsername, tecnicoPassword, tecnicoEmail, Role.MODERATOR);
            usuarioService.garantirUsuarioPadrao(usuarioUsername, usuarioPassword, usuarioEmail, Role.USER);
        };
    }
}
