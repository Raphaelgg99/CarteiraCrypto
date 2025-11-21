package com.potfoliomoedas.portfolio.controller.user;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Imports do seu projeto
import com.potfoliomoedas.portfolio.config.JWTCreator;
import com.potfoliomoedas.portfolio.config.JWTObject;
import com.potfoliomoedas.portfolio.model.Usuario;
import com.potfoliomoedas.portfolio.model.Moeda;
import com.potfoliomoedas.portfolio.dto.MoedaRequest;
import com.potfoliomoedas.portfolio.dto.MoedaPrecos;
import com.potfoliomoedas.portfolio.config.SecurityConfig;
import com.potfoliomoedas.portfolio.service.CoinGeckoService; // <-- Importe o serviço
import com.potfoliomoedas.portfolio.repository.UsuarioRepository;
import com.potfoliomoedas.portfolio.repository.MoedaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
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
class CarteiraControllerUserTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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

    // @MockBean substitui o serviço real no contexto do Spring para este teste
    // Isso evita chamar a API externa da CoinGecko de verdade
    @MockitoBean
    private CoinGeckoService coinGeckoService;

    private Usuario usuarioLogado;
    private String tokenUsuario;

    @BeforeEach
    void setUp() {
        // Limpa o banco
        moedaRepository.deleteAll();
        usuarioRepository.deleteAll();

        // 1. Cria usuário
        Usuario user = new Usuario();
        user.setNome("Investidor Teste");
        user.setEmail("investidor@email.com");
        user.setSenha(encoder.encode("123456"));
        user.setRoles(new ArrayList<>(List.of("ROLE_USER")));
        usuarioLogado = usuarioRepository.save(user);

        // 2. Gera Token
        tokenUsuario = gerarTokenValido(usuarioLogado.getEmail(), usuarioLogado.getRoles());

        // 3. Configura o Mock da CoinGecko para retornar valores fixos
        //    Sempre que o sistema pedir um preço, retornamos 100.0 para facilitar a conta
        MoedaPrecos precosFixos = new MoedaPrecos(100.0, 20.0, 18.0); // BRL, USD, EUR
        when(coinGeckoService.getPrecoAtual(anyString())).thenReturn(precosFixos);
    }

    // --- Teste: Adicionar Moeda (POST) ---

    @Test
    @DisplayName("Deve adicionar moeda com sucesso (POST /usuario/carteira/adicionar)")
    void deveAdicionarMoedaComSucesso() throws Exception {
        // --- Cenário ---
        MoedaRequest request = new MoedaRequest("bitcoin", 2.5);
        String jsonRequest = objectMapper.writeValueAsString(request);

        // --- Ação ---
        mockMvc.perform(post("/usuario/carteira/adicionar")
                        .header("Authorization", tokenUsuario)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))

                // --- Verificação API ---
                .andExpect(status().isCreated()) // Espera 201 Created
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.coinId").value("bitcoin"))
                .andExpect(jsonPath("$.quantidade").value(2.5));

        // --- Verificação Banco ---
        Optional<Moeda> moedaNoBanco = moedaRepository.findByUsuarioIdAndCoinId(usuarioLogado.getId(), "bitcoin");
        assertTrue(moedaNoBanco.isPresent());
        assertEquals(2.5, moedaNoBanco.get().getQuantidade());
    }

    // --- Teste: Valor Total (GET) ---

    @Test
    @DisplayName("Deve calcular valor total corretamente (GET /usuario/carteira)")
    void deveRetornarValorTotalComSucesso() throws Exception {
        // --- Cenário ---
        // Vamos inserir uma moeda diretamente no banco para o usuário
        Moeda moeda = new Moeda();
        moeda.setCoinId("ethereum");
        moeda.setQuantidade(10.0);
        moeda.setUsuario(usuarioLogado);
        moedaRepository.save(moeda);

        // Lembre-se: O Mock do CoinGecko retorna R$ 100.00 por moeda
        // Cálculo esperado: 10 moedas * R$ 100.00 = R$ 1000.00

        // --- Ação ---
        mockMvc.perform(get("/usuario/carteira") // Verifique se a rota é essa ou só /usuario/carteira
                        .header("Authorization", tokenUsuario))

                // --- Verificação API ---
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.seuSaldoTotalBRL").value(1000.0)) // 10 * 100
                .andExpect(jsonPath("$.usuarioEmail").value("investidor@email.com"))
                .andExpect(jsonPath("$.moedas[0].coinId").value("ethereum"));
    }

    // --- Teste: Excluir Moeda (DELETE) ---

    @Test
    @DisplayName("Deve excluir moeda com sucesso (DELETE /usuario/carteira/{coinId})")
    void deveExcluirMoedaComSucesso() throws Exception {
        // --- Cenário ---
        // Insere moeda para deletar
        Moeda moeda = new Moeda();
        moeda.setCoinId("dogecoin");
        moeda.setQuantidade(5000.0);
        moeda.setUsuario(usuarioLogado);
        moedaRepository.save(moeda);

        // --- Ação ---
        mockMvc.perform(delete("/usuario/carteira/{coinId}", "dogecoin")
                        .header("Authorization", tokenUsuario))

                // --- Verificação API ---
                .andExpect(status().isNoContent()); // 204

        // --- Verificação Banco ---
        Optional<Moeda> moedaDeletada = moedaRepository.findByUsuarioIdAndCoinId(usuarioLogado.getId(), "dogecoin");
        assertTrue(moedaDeletada.isEmpty());
    }

    @Test
    @DisplayName("NÃO deve acessar carteira sem token")
    void naoDeveAcessarSemToken() throws Exception {
        mockMvc.perform(get("/usuario/carteira"))
                .andExpect(status().isUnauthorized()); // 401
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