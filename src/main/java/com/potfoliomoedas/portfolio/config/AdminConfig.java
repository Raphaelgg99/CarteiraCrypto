package com.potfoliomoedas.portfolio.config;

import com.potfoliomoedas.portfolio.model.Usuario;
import com.potfoliomoedas.portfolio.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
public class AdminConfig {

    @Bean
    CommandLineRunner initDatabase(UsuarioRepository repo) {
        return args -> {
            if (repo.findByEmail("admin@email.com").isEmpty()) {
                Usuario admin = new Usuario();
                admin.setNome("Administrador");
                admin.setEmail("admin@email.com");
                admin.setSenha(new BCryptPasswordEncoder().encode("123456"));
                admin.getRoles().add("ADMIN");
                repo.save(admin);
            }
        };
    }
}
