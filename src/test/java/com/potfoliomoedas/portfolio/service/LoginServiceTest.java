package com.potfoliomoedas.portfolio.service;

import static org.junit.jupiter.api.Assertions.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

// ... (imports de todas as suas classes, DTOs, e exceções) ...
import com.potfoliomoedas.portfolio.config.JWTCreator;
import com.potfoliomoedas.portfolio.config.JWTObject;
import com.potfoliomoedas.portfolio.model.Usuario;
import com.potfoliomoedas.portfolio.dto.Login;
import com.potfoliomoedas.portfolio.dto.Sessao;
import com.potfoliomoedas.portfolio.config.SecurityConfig;
import com.potfoliomoedas.portfolio.exception.InvalidCredentialsException;

import com.potfoliomoedas.portfolio.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class LoginServiceTest {

    @InjectMocks
    private LoginService loginService;

    @Mock
    private UsuarioRepository repository;

    @Mock
    private BCryptPasswordEncoder encoder;

    @Mock
    private JWTCreator jwtCreator;

    @Mock
    private SecurityConfig securityConfig;

    // Objetos de teste
    private Login loginRequest;
    private Usuario usuarioDoBanco;
    private String tokenFalso;

    @BeforeEach
    void setUp() {
        // 1. A requisição que o usuário envia
        loginRequest = new Login("teste@email.com", "senha123");

        // 2. O usuário que simulamos existir no banco
        usuarioDoBanco = new Usuario();
        usuarioDoBanco.setId(1L);
        usuarioDoBanco.setEmail("teste@email.com");
        usuarioDoBanco.setSenha("senhaCriptografadaDoBanco");
        usuarioDoBanco.setRoles(List.of("USER"));

        // 3. O token que o creator vai simular
        tokenFalso = "Bearer fake.jwt.token";
    }

    @Test
    @DisplayName("Deve realizar login com sucesso e retornar uma Sessao")
    void deveLogarComSucesso() {
        // --- Cenário (Arrange) ---

        // 1. Simula o repositório encontrando o usuário
        when(repository.findByEmail("teste@email.com")).thenReturn(Optional.of(usuarioDoBanco));

        // 2. Simula o encoder (senha bate)
        when(encoder.matches("senha123", "senhaCriptografadaDoBanco")).thenReturn(true);

        // 3. Simula a config de expiração
        when(securityConfig.getEXPIRATION()).thenReturn(3600000L); // 1 hora

        // 4. Captura o JWTObject para verificar os dados
        ArgumentCaptor<JWTObject> jwtObjectCaptor = ArgumentCaptor.forClass(JWTObject.class);

        // 5. Simula o criador de token
        when(jwtCreator.gerarToken(jwtObjectCaptor.capture())).thenReturn(tokenFalso);

        // --- Ação (Act) ---
        Sessao sessao = loginService.logar(loginRequest);

        // --- Verificação (Assert/Verify) ---
        assertNotNull(sessao);
        assertEquals("teste@email.com", sessao.login());
        assertEquals(tokenFalso, sessao.token());

        // Verifica o objeto que foi passado para o criador de token
        JWTObject jwtObject = jwtObjectCaptor.getValue();
        assertEquals("teste@email.com", jwtObject.getSubject());
        assertEquals(List.of("USER"), jwtObject.getRoles());

        // Verifica se os mocks foram chamados
        verify(repository, times(1)).findByEmail("teste@email.com");
        verify(encoder, times(1)).matches("senha123", "senhaCriptografadaDoBanco");
        verify(jwtCreator, times(1)).gerarToken(any(JWTObject.class));
    }

    @Test
    @DisplayName("Deve lançar InvalidCredentialsException quando o email não for encontrado")
    void deveLancarExcecaoQuandoEmailNaoEncontrado() {
        // --- Cenário (Arrange) ---

        // 1. Simula o repositório NÃO encontrando o usuário
        when(repository.findByEmail("teste@email.com")).thenReturn(Optional.empty());

        // --- Ação (Act) e Verificação (Assert) ---
        InvalidCredentialsException exception = assertThrows(
                InvalidCredentialsException.class,
                () -> {
                    loginService.logar(loginRequest);
                }
        );

        assertEquals("Credenciais inválidas", exception.getMessage());

        // GARANTE que o resto do processo não rodou
        verify(encoder, never()).matches(anyString(), anyString());
        verify(jwtCreator, never()).gerarToken(any(JWTObject.class));
    }

    @Test
    @DisplayName("Deve lançar InvalidCredentialsException quando a senha estiver incorreta")
    void deveLancarExcecaoQuandoSenhaIncorreta() {
        // --- Cenário (Arrange) ---

        // 1. Simula o repositório encontrando o usuário
        when(repository.findByEmail("teste@email.com")).thenReturn(Optional.of(usuarioDoBanco));

        // 2. Simula o encoder (senha NÃO bate)
        when(encoder.matches("senha123", "senhaCriptografadaDoBanco")).thenReturn(false);

        // --- Ação (Act) e Verificação (Assert) ---
        InvalidCredentialsException exception = assertThrows(
                InvalidCredentialsException.class,
                () -> {
                    loginService.logar(loginRequest);
                }
        );

        assertEquals("Credenciais inválidas", exception.getMessage());

        // GARANTE que o token não foi gerado
        verify(repository, times(1)).findByEmail("teste@email.com");
        verify(encoder, times(1)).matches("senha123", "senhaCriptografadaDoBanco");
        verify(jwtCreator, never()).gerarToken(any(JWTObject.class));
    }
}