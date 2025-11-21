package com.potfoliomoedas.portfolio.controller.admin;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.potfoliomoedas.portfolio.config.JWTCreator;
import com.potfoliomoedas.portfolio.config.JWTObject;
import com.potfoliomoedas.portfolio.model.Usuario;
import com.potfoliomoedas.portfolio.model.Moeda;
import com.potfoliomoedas.portfolio.config.SecurityConfig;
import com.potfoliomoedas.portfolio.service.CoinGeckoService;
import com.potfoliomoedas.portfolio.repository.UsuarioRepository;
import com.potfoliomoedas.portfolio.repository.MoedaRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean; // Spring Boot 3.4+
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CarteiraControllerAdminTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private MoedaRepository moedaRepository;

    @Autowired
    private BCryptPasswordEncoder encoder;

    @Autowired
    private JWTCreator jwtCreator;

    @Autowired
    private SecurityConfig securityConfig;

    // Mockamos o serviço externo para não chamar a API real
    @MockitoBean
    private CoinGeckoService coinGeckoService;

    private Usuario adminUser;
    private Usuario regularUser;
    private String tokenAdmin;
    private String tokenUser;

    @BeforeEach
    void setUp() {
        moedaRepository.deleteAll();
        usuarioRepository.deleteAll();

        // 1. Cria o ADMIN
        Usuario admin = new Usuario();
        admin.setNome("Admin Chefe");
        admin.setEmail("admin@email.com");
        admin.setSenha(encoder.encode("123"));
        admin.setRoles(new ArrayList<>(List.of("ROLE_ADMIN", "ROLE_USER")));
        adminUser = usuarioRepository.save(admin);

        // 2. Cria o USUÁRIO COMUM (que terá a moeda)
        Usuario user = new Usuario();
        user.setNome("Usuario Comum");
        user.setEmail("user@email.com");
        user.setSenha(encoder.encode("123"));
        user.setRoles(new ArrayList<>(List.of("ROLE_USER")));
        regularUser = usuarioRepository.save(user);

        // 3. Gera Tokens
        tokenAdmin = gerarTokenValido(adminUser.getEmail(), adminUser.getRoles());
        tokenUser = gerarTokenValido(regularUser.getEmail(), regularUser.getRoles());
    }

    @Test
    @DisplayName("Admin deve excluir moeda de um usuário com sucesso (DELETE /usuario/admin/carteira/{id}/{coinId})")
    void adminDeveExcluirMoedaDeUsuario() throws Exception {
        // --- Cenário ---
        // Adicionamos uma moeda para o usuário comum
        Moeda moeda = new Moeda();
        moeda.setCoinId("bitcoin");
        moeda.setQuantidade(1.0);
        moeda.setUsuario(regularUser);
        moedaRepository.save(moeda);

        // --- Ação ---
        // O Admin tenta deletar a moeda do Usuário Comum
        // URL: /usuario/admin/carteira/{usuarioId}/{coinId}
        mockMvc.perform(delete("/usuario/admin/carteira/{id}/{coinId}", regularUser.getId(), "bitcoin")
                        .header("Authorization", tokenAdmin)) // Token de ADMIN

                // --- Verificação API ---
                .andExpect(status().isNoContent()); // 204 No Content

        // --- Verificação Banco ---
        Optional<Moeda> moedaNoBanco = moedaRepository.findByUsuarioIdAndCoinId(regularUser.getId(), "bitcoin");
        assertTrue(moedaNoBanco.isEmpty(), "A moeda deveria ter sido excluída do banco");
    }

    @Test
    @DisplayName("Usuário comum NÃO deve conseguir acessar endpoint de admin (403 Forbidden)")
    void usuarioComumNaoDeveAcessarEndpointAdmin() throws Exception {
        // --- Ação ---
        // Tentamos acessar a rota de admin com token de USER
        mockMvc.perform(delete("/usuario/admin/carteira/{id}/{coinId}", regularUser.getId(), "bitcoin")
                        .header("Authorization", tokenUser))

                // --- Verificação ---
                .andExpect(status().isForbidden()); // 403
    }

    @Test
    @DisplayName("Deve retornar 404 se o Admin tentar excluir moeda que não existe")
    void adminDeveReceber404SeMoedaNaoExiste() throws Exception {
        // --- Ação ---
        mockMvc.perform(delete("/usuario/admin/carteira/{id}/{coinId}", regularUser.getId(), "moedafantasma")
                        .header("Authorization", tokenAdmin))

                // --- Verificação ---
                .andExpect(status().isNotFound()); // 404 (vem do ControllerAdvice)
    }

    // --- Método Auxiliar ---
    private String gerarTokenValido(String email, List<String> roles) {
        JWTObject jwtObject = new JWTObject();
        jwtObject.setSubject(email);
        jwtObject.setIssuedAt(new Date(System.currentTimeMillis()));
        jwtObject.setExpiration(new Date(System.currentTimeMillis() + securityConfig.getEXPIRATION()));
        jwtObject.setRoles(roles);
        return jwtCreator.gerarToken(jwtObject);
    }
}