package com.potfoliomoedas.portfolio.controller.admin;

import static org.junit.jupiter.api.Assertions.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Imports do seu projeto
import com.potfoliomoedas.portfolio.config.JWTCreator;
import com.potfoliomoedas.portfolio.config.JWTObject;
import com.potfoliomoedas.portfolio.dto.UsuarioRequestDTO;
import com.potfoliomoedas.portfolio.model.Usuario;
import com.potfoliomoedas.portfolio.config.SecurityConfig;
import com.potfoliomoedas.portfolio.repository.UsuarioRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

// Imports do Spring e Java
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

import java.util.ArrayList; // <-- Importante para a correção do bug
import java.util.Date;
import java.util.List;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test") // Usa o application-test.properties (H2)
@Transactional // Faz rollback no final de cada teste
class UsuarioControllerAdminTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private BCryptPasswordEncoder encoder;

    @Autowired
    private JWTCreator jwtCreator;

    @Autowired
    private SecurityConfig securityConfig;

    @Autowired
    private ObjectMapper objectMapper;

    // Precisamos de dois usuários para este teste
    private Usuario adminUser;
    private Usuario regularUser;

    // E dois tokens
    private String adminToken;
    private String userToken;

    @BeforeEach
    void setUp() {
        usuarioRepository.deleteAll();

        // 1. Cria um usuário ADMIN
        Usuario admin = new Usuario();
        admin.setNome("Admin Teste");
        admin.setEmail("admin@email.com");
        admin.setSenha(encoder.encode("admin123"));
        // CORREÇÃO do bug do Hibernate: Use ArrayList
        admin.setRoles(new ArrayList<>(List.of("ROLE_ADMIN", "ROLE_USER")));
        adminUser = usuarioRepository.save(admin);

        // Gera um token para o ADMIN
        adminToken = gerarTokenValido(adminUser.getEmail(), adminUser.getRoles());

        // 2. Cria um usuário REGULAR (que será o alvo a ser deletado)
        Usuario user = new Usuario();
        user.setNome("Usuario Normal");
        user.setEmail("user@email.com");
        user.setSenha(encoder.encode("user123"));
        user.setRoles(new ArrayList<>(List.of("ROLE_USER"))); // CORREÇÃO
        regularUser = usuarioRepository.save(user);

        // Gera um token para o USER
        userToken = gerarTokenValido(regularUser.getEmail(), regularUser.getRoles());
    }

    // --- Testes para excluirUsuario(Long id) ---

    @Test
    @DisplayName("Admin deve excluir um usuário com sucesso (DELETE /usuario/admin/{id})")
    void adminDeveExcluirUsuarioComSucesso() throws Exception {
        // --- Cenário (Arrange) ---
        // Queremos deletar o 'regularUser' usando o 'adminToken'
        Long idParaDeletar = regularUser.getId();

        // Verifica se o usuário existe ANTES
        assertTrue(usuarioRepository.existsById(idParaDeletar));

        // --- Ação (Act) ---
        mockMvc.perform(delete("/usuario/admin/{id}", idParaDeletar) // O endpoint
                        .header("Authorization", adminToken)) // Autenticado como ADMIN

                // --- Verificação (Assert) ---
                .andExpect(status().isNoContent()); // Espera 204 No Content

        // Verificação no BANCO DE DADOS
        assertFalse(usuarioRepository.existsById(idParaDeletar),
                "O usuário deveria ter sido deletado do banco");
    }

    @Test
    @DisplayName("NÃO deve excluir usuário se o token for de ROLE_USER (DELETE /usuario/admin/{id})")
    void naoDeveExcluirUsuarioComRoleUser() throws Exception {
        // --- Cenário (Arrange) ---
        Long idParaDeletar = regularUser.getId();

        // --- Ação (Act) ---
        mockMvc.perform(delete("/usuario/admin/{id}", idParaDeletar)
                        .header("Authorization", userToken)) // Autenticado como USER

                // --- Verificação (Assert) ---
                .andExpect(status().isForbidden()); // Espera 403 Forbidden
    }

    @Test
    @DisplayName("NÃO deve excluir usuário se não houver token (DELETE /usuario/admin/{id})")
    void naoDeveExcluirUsuarioSemToken() throws Exception {
        // --- Ação (Act) ---
        mockMvc.perform(delete("/usuario/admin/{id}", regularUser.getId())) // Sem token

                // --- Verificação (Assert) ---
                .andExpect(status().isUnauthorized()); // Espera 401 Unauthorized
    }

    @Test
    @DisplayName("Admin deve receber 404 ao tentar excluir ID inexistente (DELETE /usuario/admin/{id})")
    void adminDeveReceber404AoExcluirIdInexistente() throws Exception {
        // --- Cenário (Arrange) ---
        long idInexistente = 9999L;

        // --- Ação (Act) ---
        mockMvc.perform(delete("/usuario/admin/{id}", idInexistente)
                        .header("Authorization", adminToken)) // Autenticado como ADMIN

                // --- Verificação (Assert) ---
                .andExpect(status().isNotFound()); // Espera 404 Not Found (do ControllerAdvice)
    }


    // --- Método Auxiliar para gerar tokens ---
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
    @DisplayName("Admin deve encontrar um usuário pelo ID com sucesso (GET /usuario/admin/{id})")
    void adminDeveEncontrarUsuarioPorId() throws Exception {
        // --- Cenário (Arrange) ---
        // Queremos buscar o 'regularUser' (user@email.com) usando o 'adminToken'
        Long idParaBuscar = regularUser.getId();

        // --- Ação (Act) ---
        mockMvc.perform(get("/usuario/admin/{id}", idParaBuscar) // O endpoint
                        .header("Authorization", adminToken)) // Autenticado como ADMIN

                // --- Verificação (Assert) ---
                .andExpect(status().isOk()) // Espera 200 OK
                .andExpect(jsonPath("$.email").value("user@email.com")) // Verifica o JSON
                .andExpect(jsonPath("$.nome").value("Usuario Normal"));
    }

    @Test
    @DisplayName("NÃO deve encontrar usuário se o token for de ROLE_USER (GET /usuario/admin/{id})")
    void naoDeveEncontrarUsuarioComRoleUser() throws Exception {
        // --- Cenário (Arrange) ---
        Long idParaBuscar = regularUser.getId();

        // --- Ação (Act) ---
        mockMvc.perform(get("/usuario/admin/{id}", idParaBuscar)
                        .header("Authorization", userToken)) // Autenticado como USER

                // --- Verificação (Assert) ---
                .andExpect(status().isForbidden()); // Espera 403 Forbidden
    }

    @Test
    @DisplayName("NÃO deve encontrar usuário se não houver token (GET /usuario/admin/{id})")
    void naoDeveEncontrarUsuarioSemToken() throws Exception {
        // --- Ação (Act) ---
        mockMvc.perform(get("/usuario/admin/{id}", regularUser.getId())) // Sem token

                // --- Verificação (Assert) ---
                .andExpect(status().isUnauthorized()); // Espera 401 Unauthorized
    }

    @Test
    @DisplayName("Admin deve receber 404 ao tentar encontrar ID inexistente (GET /usuario/admin/{id})")
    void adminDeveReceber404AoEncontrarIdInexistente() throws Exception {
        // --- Cenário (Arrange) ---
        long idInexistente = 9999L;

        // --- Ação (Act) ---
        mockMvc.perform(get("/usuario/admin/{id}", idInexistente)
                        .header("Authorization", adminToken)) // Autenticado como ADMIN

                // --- Verificação (Assert) ---
                .andExpect(status().isNotFound()); // Espera 404 Not Found (do ControllerAdvice)
    }

    @Test
    @DisplayName("Admin deve listar todos os usuários com sucesso (GET /usuario/admin/listartodos)")
    void adminDeveListarTodosUsuarios() throws Exception {
        // --- Cenário (Arrange) ---
        // O @BeforeEach já criou 2 usuários (adminUser e regularUser)

        // --- Ação (Act) ---
        mockMvc.perform(get("/usuario/admin/listartodos") // O endpoint
                        .header("Authorization", adminToken)) // Autenticado como ADMIN

                // --- Verificação (Assert) ---
                .andExpect(status().isOk()) // Espera 200 OK
                .andExpect(jsonPath("$.length()").value(2)) // Verifica se a lista tem 2 usuários
                .andExpect(jsonPath("$[0].email").value(adminUser.getEmail()))
                .andExpect(jsonPath("$[1].email").value(regularUser.getEmail()));
    }

    @Test
    @DisplayName("NÃO deve listar usuários se o token for de ROLE_USER (GET /usuario/admin/listartodos)")
    void naoDeveListarUsuariosComRoleUser() throws Exception {
        // --- Ação (Act) ---
        mockMvc.perform(get("/usuario/admin/listartodos")
                        .header("Authorization", userToken)) // Autenticado como USER

                // --- Verificação (Assert) ---
                .andExpect(status().isForbidden()); // Espera 403 Forbidden
    }

    @Test
    @DisplayName("NÃO deve listar usuários se não houver token (GET /usuario/admin/listartodos)")
    void naoDeveListarUsuariosSemToken() throws Exception {
        // --- Ação (Act) ---
        mockMvc.perform(get("/usuario/admin/listartodos")) // Sem token

                // --- Verificação (Assert) ---
                .andExpect(status().isUnauthorized()); // Espera 401 Unauthorized
    }

    @Test
    @DisplayName("Admin deve atualizar um usuário com sucesso (PUT /usuario/admin/{id})")
    void adminDeveAtualizarUsuarioComSucesso() throws Exception {
        // --- Cenário (Arrange) ---
        // Queremos atualizar o 'regularUser' (user@email.com) usando o 'adminToken'
        Long idParaAtualizar = regularUser.getId();

        UsuarioRequestDTO requestDTO = new UsuarioRequestDTO(
                "Nome Atualizado Pelo Admin",
                "novoemail@admin.com",
                "novaSenhaAdmin"
        );
        String jsonRequest = objectMapper.writeValueAsString(requestDTO);

        // --- Ação (Act) ---
        mockMvc.perform(put("/usuario/admin/{id}", idParaAtualizar) // O endpoint
                        .header("Authorization", adminToken) // Autenticado como ADMIN
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))

                // --- Verificação (Assert) ---
                .andExpect(status().isOk()) // Espera 200 OK
                .andExpect(jsonPath("$.nome").value("Nome Atualizado Pelo Admin"))
                .andExpect(jsonPath("$.email").value("novoemail@admin.com"));

        // Verificação no BANCO DE DADOS
        Usuario usuarioAtualizado = usuarioRepository.findById(idParaAtualizar).get();
        assertEquals("Nome Atualizado Pelo Admin", usuarioAtualizado.getNome());
        assertEquals("novoemail@admin.com", usuarioAtualizado.getEmail());
        assertTrue(encoder.matches("novaSenhaAdmin", usuarioAtualizado.getSenha()));
    }

    @Test
    @DisplayName("NÃO deve atualizar usuário se o token for de ROLE_USER (PUT /usuario/admin/{id})")
    void naoDeveAtualizarUsuarioComRoleUser() throws Exception {
        // --- Cenário (Arrange) ---
        UsuarioRequestDTO requestDTO = new UsuarioRequestDTO("Nome Novo", null, null);
        String jsonRequest = objectMapper.writeValueAsString(requestDTO);

        // --- Ação (Act) ---
        mockMvc.perform(put("/usuario/admin/{id}", regularUser.getId())
                        .header("Authorization", userToken) // Autenticado como USER
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))

                // --- Verificação (Assert) ---
                .andExpect(status().isForbidden()); // Espera 403 Forbidden
    }

    @Test
    @DisplayName("NÃO deve atualizar usuário se não houver token (PUT /usuario/admin/{id})")
    void naoDeveAtualizarUsuarioSemToken() throws Exception {
        // --- Cenário (Arrange) ---
        UsuarioRequestDTO requestDTO = new UsuarioRequestDTO("Nome Novo", null, null);
        String jsonRequest = objectMapper.writeValueAsString(requestDTO);

        // --- Ação (Act) ---
        mockMvc.perform(put("/usuario/admin/{id}", regularUser.getId()) // Sem token
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))

                // --- Verificação (Assert) ---
                .andExpect(status().isUnauthorized()); // Espera 401 Unauthorized
    }

    @Test
    @DisplayName("Admin deve receber 409 (Conflict) ao tentar atualizar para email duplicado")
    void adminDeveReceber409AoAtualizarParaEmailDuplicado() throws Exception {
        // --- Cenário (Arrange) ---
        // O 'adminUser' (admin@email.com) já existe.
        // Vamos tentar atualizar o 'regularUser' para o email do admin.

        UsuarioRequestDTO requestDTO = new UsuarioRequestDTO(null, "admin@email.com", null);
        String jsonRequest = objectMapper.writeValueAsString(requestDTO);

        // --- Ação (Act) ---
        mockMvc.perform(put("/usuario/admin/{id}", regularUser.getId())
                        .header("Authorization", adminToken) // Autenticado como ADMIN
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))

                // --- Verificação (Assert) ---
                .andExpect(status().isConflict()); // Espera 409 Conflict (do ControllerAdvice)
    }
}