package com.potfoliomoedas.portfolio.service.user.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.potfoliomoedas.portfolio.component.ConvertToDTO;
import com.potfoliomoedas.portfolio.component.UsuarioLogado;
import com.potfoliomoedas.portfolio.dto.UsuarioRequestDTO;
import com.potfoliomoedas.portfolio.dto.UsuarioResponseDTO;
import com.potfoliomoedas.portfolio.exception.EmailExistenteException;
import com.potfoliomoedas.portfolio.exception.EmailNullException;
import com.potfoliomoedas.portfolio.exception.NomeNullException;
import com.potfoliomoedas.portfolio.exception.SenhaNullException;
import com.potfoliomoedas.portfolio.model.Usuario;
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

import java.util.ArrayList;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceUserImplTest {

    // 1. A Classe que estamos testando
    @InjectMocks
    private UsuarioServiceUserImpl usuarioService;
    // 2. As Dependências (Mocks)
    //    Nós vamos simular o comportamento de todas as classes que o Service usa.
    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private BCryptPasswordEncoder encoder;

    @Mock
    private ConvertToDTO convertToDTO;

    @Mock
    private UsuarioLogado usuarioLogado; // Mockado mesmo que não seja usado neste método

    // 3. Objetos de Teste (Cenários)
    private UsuarioRequestDTO requestDTO;
    private Usuario usuarioSalvo;
    private UsuarioResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        // Cenário base para os testes
        requestDTO = new UsuarioRequestDTO("Nome Teste", "teste@email.com", "senha123");

        usuarioSalvo = new Usuario();
        usuarioSalvo.setId(1L);
        usuarioSalvo.setNome("Nome Teste");
        usuarioSalvo.setEmail("teste@email.com");
        usuarioSalvo.setSenha("senhaCriptografada");
        usuarioSalvo.setRoles(List.of("USER"));

        responseDTO = new UsuarioResponseDTO(1L, "Nome Teste", "teste@email.com", new ArrayList<>());
    }

    @Test
    @DisplayName("Deve criar um usuário com sucesso")
    void deveCriarUsuarioComSucesso() {
        // --- Cenário (Arrange) ---

        // 1. Simula que o e-mail NÃO existe no banco
        when(usuarioRepository.existsByEmail("teste@email.com")).thenReturn(false);

        // 2. Simula o encoder de senha
        when(encoder.encode("senha123")).thenReturn("senhaCriptografada");

        // 3. Precisamos capturar o objeto 'Usuario' que é passado para o 'save'
        //    Isso é crucial para verificar se as roles e a senha foram setadas corretamente.
        ArgumentCaptor<Usuario> usuarioCaptor = ArgumentCaptor.forClass(Usuario.class);

        // 4. Simula o método save (retornando o usuário salvo)
        when(usuarioRepository.save(usuarioCaptor.capture())).thenReturn(usuarioSalvo);

        // 5. Simula o conversor de DTO (aceitando QUALQUER objeto Usuario)
        when(convertToDTO.convertUserToUserDTO(any(Usuario.class))).thenReturn(responseDTO);

        // --- Ação (Act) ---
        UsuarioResponseDTO resultado = usuarioService.criarUsuario(requestDTO);

        // --- Verificação (Assert) ---
        assertNotNull(resultado);
        assertEquals(responseDTO, resultado);
        assertEquals("teste@email.com", resultado.email());

        // Verifica se os mocks foram chamados
        verify(usuarioRepository).existsByEmail("teste@email.com");
        verify(encoder).encode("senha123");
        verify(usuarioRepository).save(any(Usuario.class));
        verify(convertToDTO).convertUserToUserDTO(any(Usuario.class));

        // Pega o usuário que foi capturado e verifica seus dados internos
        Usuario usuarioParaSalvar = usuarioCaptor.getValue();
        assertEquals("Nome Teste", usuarioParaSalvar.getNome());
        assertEquals("senhaCriptografada", usuarioParaSalvar.getSenha());
        assertEquals(1, usuarioParaSalvar.getRoles().size());
        assertEquals("USER", usuarioParaSalvar.getRoles().get(0));
    }

    @Test
    @DisplayName("Deve lançar EmailNullException quando o email for nulo")
    void deveLancarEmailNullExceptionQuandoEmailForNulo() {
        // Cenário
        UsuarioRequestDTO dtoComEmailNulo = new UsuarioRequestDTO("Nome Teste", null, "senha123");

        // Ação e Verificação
        EmailNullException exception = assertThrows(EmailNullException.class, () -> {
            usuarioService.criarUsuario(dtoComEmailNulo);
        });

        // Verifica a mensagem da exceção
        assertEquals("Favor colocar um email", exception.getMessage());

        // Verifica se o processo foi interrompido (nada foi salvo)
        verify(usuarioRepository, never()).existsByEmail(anyString());
        verify(encoder, never()).encode(anyString());
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Deve lançar EmailNullException quando o email for vazio (blank)")
    void deveLancarEmailNullExceptionQuandoEmailForVazio() {
        // Cenário
        UsuarioRequestDTO dtoComEmailVazio = new UsuarioRequestDTO("Nome Teste", " ", "senha123");

        // Ação e Verificação
        EmailNullException exception = assertThrows(EmailNullException.class, () -> {
            usuarioService.criarUsuario(dtoComEmailVazio);
        });

        assertEquals("Favor colocar um email", exception.getMessage());
        verify(usuarioRepository, never()).existsByEmail(anyString());
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Deve lançar NomeNullException quando o nome for nulo")
    void deveLancarNomeNullExceptionQuandoNomeForNulo() {
        // Cenário
        UsuarioRequestDTO dtoComNomeNulo = new UsuarioRequestDTO(null, "teste@email.com", "senha123");

        // Ação e Verificação
        NomeNullException exception = assertThrows(NomeNullException.class, () -> {
            usuarioService.criarUsuario(dtoComNomeNulo);
        });

        assertEquals("Favor colocar um nome", exception.getMessage());
        verify(usuarioRepository, never()).existsByEmail(anyString());
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Deve lançar SenhaNullException quando a senha for nula")
    void deveLancarSenhaNullExceptionQuandoSenhaForNula() {
        // Cenário
        UsuarioRequestDTO dtoComSenhaNula = new UsuarioRequestDTO("Nome Teste", "teste@email.com", null);

        // Ação e Verificação
        SenhaNullException exception = assertThrows(SenhaNullException.class, () -> {
            usuarioService.criarUsuario(dtoComSenhaNula);
        });

        assertEquals("Favor colocar uma senha", exception.getMessage());
        verify(usuarioRepository, never()).existsByEmail(anyString());
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Deve lançar EmailExistenteException quando o email já existir")
    void deveLancarEmailExistenteExceptionQuandoEmailJaExistir() {
        // Cenário
        // 1. Simula que o e-mail JÁ existe no banco
        when(usuarioRepository.existsByEmail("teste@email.com")).thenReturn(true);

        // Ação e Verificação
        EmailExistenteException exception = assertThrows(EmailExistenteException.class, () -> {
            usuarioService.criarUsuario(requestDTO);
        });

        assertEquals("Esse email já existe", exception.getMessage());

        // Verifica se o processo parou DEPOIS da checagem de e-mail
        verify(usuarioRepository).existsByEmail("teste@email.com"); // Foi chamado
        verify(encoder, never()).encode(anyString()); // Não foi chamado
        verify(usuarioRepository, never()).save(any(Usuario.class)); // Não foi chamado
        verify(convertToDTO, never()).convertUserToUserDTO(any(Usuario.class));
    }

    @Test
    @DisplayName("Deve excluir o usuário logado com sucesso")
    void deveExcluirUsuarioComSucesso() {
        // --- Cenário (Arrange) ---

        // 1. Crie um usuário logado simulado (pode usar o 'usuarioSalvo' do setUp)
        //    (Vamos supor que o 'usuarioSalvo' foi criado no @BeforeEach)
        //    'usuarioSalvo' já existe e tem ID 1L, email, etc.

        // 2. Simula o comportamento do UsuarioLogado
        when(usuarioLogado.getUsuarioLogado()).thenReturn(usuarioSalvo);

        // 3. Simula o método delete do repositório (não precisa retornar nada)
        //    doNothing() é usado para métodos void.
        doNothing().when(usuarioRepository).delete(any(Usuario.class));

        // --- Ação (Act) ---
        usuarioService.excluirUsuario();

        // --- Verificação (Assert/Verify) ---

        // 1. Verifica se o UsuarioLogado foi chamado 1 vez
        verify(usuarioLogado, times(1)).getUsuarioLogado();

        // 2. Verifica se o método 'delete' do repositório foi chamado 1 vez
        //    com o objeto 'usuarioSalvo' EXATO.
        verify(usuarioRepository, times(1)).delete(usuarioSalvo);
    }

    @Test
    @DisplayName("Deve atualizar apenas o nome do usuário")
    void deveAtualizarApenasNome() {
        // --- Cenário (Arrange) ---
        // 1. DTO de requisição (só com nome)
        UsuarioRequestDTO requestDTO = new UsuarioRequestDTO("Nome Novo", null, null);

        // 2. Simula o usuário logado (usando o 'usuarioSalvo' do @BeforeEach)
        //    (vamos supor que o 'usuarioSalvo' tem "Nome Teste" e "teste@email.com")
        when(usuarioLogado.getUsuarioLogado()).thenReturn(usuarioSalvo);

        // 3. Simula o 'save' (apenas retorna o usuário modificado)
        when(usuarioRepository.save(usuarioSalvo)).thenReturn(usuarioSalvo);

        // 4. Simula o conversor de DTO
        when(convertToDTO.convertUserToUserDTO(usuarioSalvo)).thenReturn(responseDTO);

        // --- Ação (Act) ---
        usuarioService.atualizarUsuario(requestDTO);

        // --- Verificação (Assert/Verify) ---
        // Verifica se o nome foi alterado DENTRO do objeto 'usuarioSalvo'
        assertEquals("Nome Novo", usuarioSalvo.getNome());

        // Verifica se o 'save' foi chamado
        verify(usuarioRepository, times(1)).save(usuarioSalvo);

        // Verifica se os outros mocks (email e senha) NÃO foram chamados
        verify(usuarioRepository, never()).existsByEmail(anyString());
        verify(encoder, never()).encode(anyString());
    }

    @Test
    @DisplayName("Deve atualizar apenas a senha do usuário")
    void deveAtualizarApenasSenha() {
        // --- Cenário (Arrange) ---
        UsuarioRequestDTO requestDTO = new UsuarioRequestDTO(null, null, "novaSenha123");
        when(usuarioLogado.getUsuarioLogado()).thenReturn(usuarioSalvo);

        // 1. Simula o encoder
        when(encoder.encode("novaSenha123")).thenReturn("novaSenhaCriptografada");

        when(usuarioRepository.save(usuarioSalvo)).thenReturn(usuarioSalvo);
        when(convertToDTO.convertUserToUserDTO(usuarioSalvo)).thenReturn(responseDTO);

        // --- Ação (Act) ---
        usuarioService.atualizarUsuario(requestDTO);

        // --- Verificação (Assert/Verify) ---
        // Verifica se a senha foi alterada DENTRO do objeto 'usuarioSalvo'
        assertEquals("novaSenhaCriptografada", usuarioSalvo.getSenha());

        // Verifica se o encoder foi chamado
        verify(encoder, times(1)).encode("novaSenha123");
        verify(usuarioRepository, times(1)).save(usuarioSalvo);

        // Verifica se a checagem de email NÃO foi chamada
        verify(usuarioRepository, never()).existsByEmail(anyString());
    }

    @Test
    @DisplayName("Deve atualizar o email com sucesso (email novo e válido)")
    void deveAtualizarEmailComSucesso() {
        // --- Cenário (Arrange) ---
        UsuarioRequestDTO requestDTO = new UsuarioRequestDTO(null, "emailnovo@email.com", null);

        // (Lembre-se: 'usuarioSalvo' tem o email "teste@email.com")
        when(usuarioLogado.getUsuarioLogado()).thenReturn(usuarioSalvo);

        // 1. Simula a checagem de e-mail (retorna false = não existe)
        when(usuarioRepository.existsByEmail("emailnovo@email.com")).thenReturn(false);

        when(usuarioRepository.save(usuarioSalvo)).thenReturn(usuarioSalvo);
        when(convertToDTO.convertUserToUserDTO(usuarioSalvo)).thenReturn(responseDTO);

        // --- Ação (Act) ---
        usuarioService.atualizarUsuario(requestDTO);

        // --- Verificação (Assert/Verify) ---
        // Verifica se o email foi alterado
        assertEquals("emailnovo@email.com", usuarioSalvo.getEmail());

        // Verifica se a checagem de e-mail foi feita
        verify(usuarioRepository, times(1)).existsByEmail("emailnovo@email.com");
        verify(usuarioRepository, times(1)).save(usuarioSalvo);
    }

    @Test
    @DisplayName("Deve lançar EmailExistenteException ao tentar atualizar para um email duplicado")
    void deveLancarEmailExistenteException() {
        // --- Cenário (Arrange) ---
        UsuarioRequestDTO requestDTO = new UsuarioRequestDTO(null, "emailduplicado@email.com", null);
        when(usuarioLogado.getUsuarioLogado()).thenReturn(usuarioSalvo);

        // 1. Simula a checagem de e-mail (retorna true = JÁ EXISTE)
        when(usuarioRepository.existsByEmail("emailduplicado@email.com")).thenReturn(true);

        // --- Ação (Act) e Verificação (Assert) ---
        EmailExistenteException exception = assertThrows(EmailExistenteException.class, () -> {
            usuarioService.atualizarUsuario(requestDTO);
        });

        assertEquals("Esse email já existe", exception.getMessage());

        // Verifica se o 'save' NUNCA foi chamado
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    @DisplayName("NÃO deve checar o e-mail se o e-mail enviado for o mesmo do usuário")
    void naoDeveChecarEmailSeForOMesmo() {
        // --- Cenário (Arrange) ---
        // (Lembre-se: 'usuarioSalvo' tem o email "teste@email.com")
        UsuarioRequestDTO requestDTO = new UsuarioRequestDTO("Novo Nome", "teste@email.com", null);

        when(usuarioLogado.getUsuarioLogado()).thenReturn(usuarioSalvo);
        when(usuarioRepository.save(usuarioSalvo)).thenReturn(usuarioSalvo);
        when(convertToDTO.convertUserToUserDTO(usuarioSalvo)).thenReturn(responseDTO);

        // --- Ação (Act) ---
        usuarioService.atualizarUsuario(requestDTO);

        // --- Verificação (Assert/Verify) ---
        // O nome deve ser atualizado
        assertEquals("Novo Nome", usuarioSalvo.getNome());

        // O email NÃO deve ser checado
        verify(usuarioRepository, never()).existsByEmail(anyString());

        // O save deve acontecer
        verify(usuarioRepository, times(1)).save(usuarioSalvo);
    }
}