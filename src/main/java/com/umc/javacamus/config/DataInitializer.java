package com.umc.javacamus.config;

import com.umc.javacamus.service.UsuarioService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// cria o admin padrao quando sobe o app, usando os valores do .env
@Configuration
public class DataInitializer {

    @Value("${app.admin.username}")
    private String adminUsername;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Value("${app.admin.email}")
    private String adminEmail;

    // so roda uma vez no boot, se o admin ja existir nao faz nada
    @Bean
    CommandLineRunner initDatabase(UsuarioService usuarioService) {
        return args -> usuarioService.garantirAdminPadrao(adminUsername, adminPassword, adminEmail);
    }
}
