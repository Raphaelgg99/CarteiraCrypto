package com.potfoliomoedas.portfolio.controller.user;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.potfoliomoedas.portfolio.config.JWTCreator;
import com.potfoliomoedas.portfolio.config.JWTObject;
import com.potfoliomoedas.portfolio.config.SecurityConfig;
import com.potfoliomoedas.portfolio.model.Usuario;
import com.potfoliomoedas.portfolio.dto.UsuarioRequestDTO;
import com.potfoliomoedas.portfolio.repository.UsuarioRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

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
import java.util.Date;
import java.util.List;

@SpringBootTest // 1. "Liga" a aplicação Spring
@AutoConfigureMockMvc // 2. Nos dá o MockMvc para fazer chamadas HTTP
@ActiveProfiles("test") // 3. Usa o application-test.properties (para o H2)
@Transactional // 4. Roda o teste em uma transação e faz rollback no final
class UsuarioControllerUserTest {

    @Autowired
    private MockMvc mockMvc; // O "Postman" do nosso teste

    @Autowired
    private ObjectMapper objectMapper; // Para converter objetos em JSON

    @Autowired
    private UsuarioRepository usuarioRepository; // O repositório REAL

    @Autowired
    private BCryptPasswordEncoder encoder;

    @Autowired
    private JWTCreator jwtCreator;

    @Autowired
    private SecurityConfig securityConfig;

    @BeforeEach
    void setUp() {
        // Limpa o banco antes de cada teste para garantir isolamento
        usuarioRepository.deleteAll();

        // 1. Cria um usuário base para testes que exigem login
        Usuario usuario = new Usuario();
        usuario.setNome("Usuario Logado Teste");
        usuario.setEmail("logado@email.com");
        usuario.setSenha(encoder.encode("senha123")); // Salva a senha criptografada
        usuario.setRoles(new ArrayList<>(List.of("USER")));

        Usuario usuarioSalvoNoBanco = usuarioRepository.save(usuario);
    }

    // --- Teste para criarUsuario ---

    @Test
    @DisplayName("Deve criar um usuário com sucesso (POST /usuario/criarusuario)")
    void deveCriarUsuarioComSucesso() throws Exception {
        // --- Cenário (Arrange) ---

        // 1. O DTO que será enviado no corpo da requisição
        UsuarioRequestDTO requestDTO = new UsuarioRequestDTO(
                "Novo Usuario",
                "novo@email.com",
                "senhaForte123"
        );
        String jsonRequest = objectMapper.writeValueAsString(requestDTO);

        // --- Ação (Act) ---
        // 2. Executa a chamada POST para o endpoint público
        mockMvc.perform(post("/usuario/criarusuario") // Endpoint do seu Controller
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))

                // --- Verificação (Assert) ---
                // 3. Verifica a RESPOSTA DA API
                .andExpect(status().isCreated()) // Espera 201 Created
                .andExpect(header().exists("Location")) // Espera o cabeçalho Location
                .andExpect(jsonPath("$.email").value("novo@email.com")) // Verifica o JSON de resposta
                .andExpect(jsonPath("$.nome").value("Novo Usuario"));

        // 4. Verificação no BANCO DE DADOS (O mais importante)
        Usuario usuarioDoBanco = usuarioRepository.findByEmail("novo@email.com")
                .orElseThrow(() -> new AssertionError("Usuário não foi salvo no banco"));

        assertNotNull(usuarioDoBanco);
        assertEquals("Novo Usuario", usuarioDoBanco.getNome());
        // Verifica se a senha foi salva CRIPTOGRAFADA
        assertTrue(encoder.matches("senhaForte123", usuarioDoBanco.getSenha()));
    }

    @Test
    @DisplayName("Deve excluir o usuário logado com sucesso (DELETE /usuario)")
    void deveExcluirUsuarioComSucesso() throws Exception {
        // --- Cenário (Arrange) ---
        // 1. Gera um token JWT válido para o usuário que criamos no @BeforeEach
        String token = gerarTokenValido("logado@email.com", List.of("USER"));

        // Verifica se o usuário EXISTE no banco antes
        assertTrue(usuarioRepository.existsByEmail("logado@email.com"));

        // --- Ação (Act) ---
        // 2. Executa a chamada DELETE com o token de autorização
        mockMvc.perform(delete("/usuario")
                        .header("Authorization", token)) // <-- A AUTENTICAÇÃO

                // --- Verificação (Assert) ---
                .andExpect(status().isNoContent()); // Espera 204 No Content

        // 3. Verificação no BANCO DE DADOS
        // Verifica se o usuário foi REALMENTE deletado
        assertFalse(usuarioRepository.existsByEmail("logado@email.com"),
                "O usuário não foi deletado do banco de dados");
    }

    @Test
    @DisplayName("NÃO deve excluir usuário se o token JWT não for enviado (DELETE /usuario)")
    void naoDeveExcluirUsuarioSemToken() throws Exception {
        // --- Ação (Act) ---
        // 1. Executa a chamada DELETE SEM o token
        mockMvc.perform(delete("/usuario"))

                .andExpect(status().isUnauthorized());
    }


    // --- Método Auxiliar para gerar tokens (você precisa dele) ---
    private String gerarTokenValido(String email, List<String> roles) {
        JWTObject jwtObject = new JWTObject();
        jwtObject.setSubject(email);
        jwtObject.setIssuedAt(new Date(System.currentTimeMillis()));
        jwtObject.setExpiration(new Date(System.currentTimeMillis() + securityConfig.getEXPIRATION()));
        jwtObject.setRoles(roles);

        // Usa o JWTCreator REAL
        return jwtCreator.gerarToken(jwtObject);
    }

    @Test
    @DisplayName("Deve atualizar o nome do usuário logado com sucesso (PUT /usuario)")
    void deveAtualizarUsuarioComSucesso() throws Exception {
        // --- Cenário (Arrange) ---

        // 1. O usuário 'usuarioSalvoNoBanco' (email: logado@email.com) já existe
        //    com o nome "Usuario Logado Teste".

        // 2. O DTO de requisição com os novos dados
        UsuarioRequestDTO requestDTO = new UsuarioRequestDTO(
                "Nome Atualizado", // Novo nome
                null, // Sem email
                "novaSenha123"  // Nova senha
        );
        String jsonRequest = objectMapper.writeValueAsString(requestDTO);

        // 3. Gera um token JWT válido para esse usuário
        String token = gerarTokenValido("logado@email.com", List.of("USER"));

        // --- Ação (Act) ---
        // 4. Executa a chamada PUT com o token e o JSON
        mockMvc.perform(put("/usuario")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))

                // --- Verificação (Assert) ---
                // 5. Verifica a resposta da API
                .andExpect(status().isOk()) // Espera 200 OK
                .andExpect(jsonPath("$.nome").value("Nome Atualizado")) // Verifica o JSON de resposta
                .andExpect(jsonPath("$.email").value("logado@email.com")); // Email não mudou

        // 6. Verificação no BANCO DE DADOS
        Usuario usuarioDoBanco = usuarioRepository.findByEmail("logado@email.com").get();

        assertEquals("Nome Atualizado", usuarioDoBanco.getNome());
        // Verifica se a nova senha foi salva CRIPTOGRAFADA
        assertTrue(encoder.matches("novaSenha123", usuarioDoBanco.getSenha()));
    }

    @Test
    @DisplayName("NÃO deve atualizar usuário se o token JWT não for enviado (PUT /usuario)")
    void naoDeveAtualizarUsuarioSemToken() throws Exception {
        // --- Cenário (Arrange) ---
        UsuarioRequestDTO requestDTO = new UsuarioRequestDTO("Nome Novo", null, null);
        String jsonRequest = objectMapper.writeValueAsString(requestDTO);

        // --- Ação (Act) ---
        // 1. Executa a chamada PUT SEM o token
        mockMvc.perform(put("/usuario")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))

                // --- Verificação (Assert) ---
                // 2. Espera 401 Unauthorized
                .andExpect(status().isUnauthorized());
    }
}