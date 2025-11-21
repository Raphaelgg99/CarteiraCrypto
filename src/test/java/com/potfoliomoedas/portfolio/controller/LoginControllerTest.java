package com.potfoliomoedas.portfolio.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.potfoliomoedas.portfolio.dto.Login;
import com.potfoliomoedas.portfolio.model.Usuario;
import com.potfoliomoedas.portfolio.repository.UsuarioRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class LoginControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private BCryptPasswordEncoder encoder;

    @BeforeEach
    void setUp() {
        usuarioRepository.deleteAll();

        // Cria um usuário no banco para testarmos o login
        Usuario usuario = new Usuario();
        usuario.setNome("Usuario Teste");
        usuario.setEmail("teste@email.com");
        // Importante: Salvar a senha CRIPTOGRAFADA
        usuario.setSenha(encoder.encode("123456"));
        usuario.setRoles(new ArrayList<>(List.of("ROLE_USER")));

        usuarioRepository.save(usuario);
    }

    @Test
    @DisplayName("Deve realizar login com sucesso e retornar token (POST /login)")
    void deveLogarComSucesso() throws Exception {
        // --- Cenário ---
        Login loginRequest = new Login("teste@email.com", "123456");
        String jsonRequest = objectMapper.writeValueAsString(loginRequest);

        // --- Ação ---
        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))

                // --- Verificação ---
                .andExpect(status().isOk()) // Espera 200 OK
                .andExpect(jsonPath("$.login").value("teste@email.com"))
                .andExpect(jsonPath("$.token").exists()) // Verifica se o token veio
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    @DisplayName("Deve retornar 401 (Unauthorized) com senha incorreta")
    void deveFalharComSenhaIncorreta() throws Exception {
        // --- Cenário ---
        Login loginRequest = new Login("teste@email.com", "senhaErrada");
        String jsonRequest = objectMapper.writeValueAsString(loginRequest);

        // --- Ação ---
        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))

                // --- Verificação ---
                .andExpect(status().isUnauthorized()); // Espera 401
        // O ControllerAdvice que criamos vai garantir esse 401
    }

    @Test
    @DisplayName("Deve retornar 401 (Unauthorized) com email inexistente")
    void deveFalharComEmailInexistente() throws Exception {
        // --- Cenário ---
        Login loginRequest = new Login("inexistente@email.com", "123456");
        String jsonRequest = objectMapper.writeValueAsString(loginRequest);

        // --- Ação ---
        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))

                // --- Verificação ---
                .andExpect(status().isUnauthorized()); // Espera 401
    }
}