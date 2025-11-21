package com.potfoliomoedas.portfolio.service.admin.impl;

import static org.junit.jupiter.api.Assertions.*;
// Crie esta classe se ela não existir
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

// ... (imports das suas classes Usuario, UsuarioRepository, etc.) ...
import com.potfoliomoedas.portfolio.component.ConvertToDTO;
import com.potfoliomoedas.portfolio.dto.UsuarioRequestDTO;
import com.potfoliomoedas.portfolio.dto.UsuarioResponseDTO;
import com.potfoliomoedas.portfolio.exception.EmailExistenteException;
import com.potfoliomoedas.portfolio.exception.UsuarioNaoEncontradoException;

import com.potfoliomoedas.portfolio.model.Usuario;
import com.potfoliomoedas.portfolio.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceAdminImplTest {

    @InjectMocks
    private UsuarioServiceAdminImpl usuarioService;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private BCryptPasswordEncoder encoder; // Mockado mesmo que não usado aqui

    @Mock
    private ConvertToDTO convertToDTO; // Mockado mesmo que não usado aqui

    private Usuario usuarioMock;

    @BeforeEach
    void setUp() {
        // Cria um mock de usuário para os testes
        usuarioMock = new Usuario();
        usuarioMock.setId(1L);
        usuarioMock.setNome("Usuario Mock");
        usuarioMock.setEmail("mock@email.com");
    }

    @Test
    @DisplayName("Deve excluir um usuário com sucesso quando o ID existir")
    void deveExcluirUsuarioComSucesso() {
        // --- Cenário (Arrange) ---
        long idParaExcluir = 1L;

        // 1. Simula o findById (encontra o usuário)
        when(usuarioRepository.findById(idParaExcluir)).thenReturn(Optional.of(usuarioMock));

        // 2. Prepara o mock para o método 'delete' (que é void)
        doNothing().when(usuarioRepository).delete(usuarioMock);

        // --- Ação (Act) ---
        // Executa o método (não deve lançar exceção)
        assertDoesNotThrow(() -> {
            usuarioService.excluirUsuario(idParaExcluir);
        });

        // --- Verificação (Assert/Verify) ---
        // 1. Verifica se 'findById' foi chamado com o ID correto
        verify(usuarioRepository, times(1)).findById(idParaExcluir);

        // 2. Verifica se 'delete' foi chamado com o usuário EXATO que encontramos
        verify(usuarioRepository, times(1)).delete(usuarioMock);
    }

    @Test
    @DisplayName("Deve lançar UsuarioNaoEncontradoException quando o ID não existir")
    void deveLancarExcecaoQuandoUsuarioNaoEncontrado() {
        // --- Cenário (Arrange) ---
        long idInexistente = 99L;

        // 1. Simula o findById (não encontra o usuário)
        when(usuarioRepository.findById(idInexistente)).thenReturn(Optional.empty());

        // --- Ação (Act) e Verificação (Assert) ---

        // 1. Verifica se a exceção CORRETA é lançada
        UsuarioNaoEncontradoException exception = assertThrows(
                UsuarioNaoEncontradoException.class,
                () -> {
                    usuarioService.excluirUsuario(idInexistente);
                }
        );

        // 2. (Opcional) Verifica a mensagem da exceção
        assertEquals("Usuario não encontrado", exception.getMessage());

        // 3. Verifica se 'findById' foi chamado
        verify(usuarioRepository, times(1)).findById(idInexistente);

        // 4. GARANTE que o 'delete' NUNCA foi chamado
        verify(usuarioRepository, never()).delete(any(Usuario.class));
    }

    @Test
    @DisplayName("Deve retornar uma lista de UsuarioResponseDTO com sucesso")
    void deveRetornarListaDeUsuariosDTO() {
        // --- Cenário (Arrange) ---

        // 1. Crie um segundo mock de usuário para ter uma lista
        Usuario usuarioMock2 = new Usuario();
        usuarioMock2.setId(2L);
        usuarioMock2.setNome("Usuario Mock 2");
        usuarioMock2.setEmail("mock2@email.com");

        List<Usuario> listaDeUsuarios = List.of(usuarioMock, usuarioMock2);

        // 2. Crie os DTOs de resposta esperados
        UsuarioResponseDTO responseDTO1 = new UsuarioResponseDTO(1L, "Usuario Mock", "mock@email.com", new ArrayList<>());
        UsuarioResponseDTO responseDTO2 = new UsuarioResponseDTO(2L, "Usuario Mock 2", "mock2@email.com", new ArrayList<>());

        // 3. Simula o findAll (retorna a lista de 2 usuários)
        when(usuarioRepository.findAll()).thenReturn(listaDeUsuarios);

        // 4. Simula o conversor (será chamado para cada usuário)
        when(convertToDTO.convertUserToUserDTO(usuarioMock)).thenReturn(responseDTO1);
        when(convertToDTO.convertUserToUserDTO(usuarioMock2)).thenReturn(responseDTO2);

        // --- Ação (Act) ---
        List<UsuarioResponseDTO> resultado = usuarioService.listarTodos();

        // --- Verificação (Assert/Verify) ---

        // 1. Verifica a lista de resultado
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertEquals("mock@email.com", resultado.get(0).email());
        assertEquals("mock2@email.com", resultado.get(1).email());

        // 2. Verifica se o findAll foi chamado
        verify(usuarioRepository, times(1)).findAll();

        // 3. Verifica se o conversor foi chamado 2 vezes
        verify(convertToDTO, times(2)).convertUserToUserDTO(any(Usuario.class));
        verify(convertToDTO, times(1)).convertUserToUserDTO(usuarioMock);
        verify(convertToDTO, times(1)).convertUserToUserDTO(usuarioMock2);
    }

    @Test
    @DisplayName("Deve retornar uma lista vazia quando não houver usuários")
    void deveRetornarListaVazia() {
        // --- Cenário (Arrange) ---

        // 1. Simula o findAll (retorna uma lista vazia)
        when(usuarioRepository.findAll()).thenReturn(Collections.emptyList());

        // --- Ação (Act) ---
        List<UsuarioResponseDTO> resultado = usuarioService.listarTodos();

        // --- Verificação (Assert/Verify) ---

        // 1. Verifica se a lista está vazia
        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());

        // 2. Verifica se o findAll foi chamado
        verify(usuarioRepository, times(1)).findAll();

        // 3. GARANTE que o conversor NUNCA foi chamado
        verify(convertToDTO, never()).convertUserToUserDTO(any(Usuario.class));
    }


    @Test
    @DisplayName("Deve encontrar um usuário pelo ID e retornar o DTO")
    void deveEncontrarUsuarioPorId() {
        // --- Cenário (Arrange) ---
        long idParaBuscar = 1L;

        // 1. Crie o DTO de resposta esperado
        UsuarioResponseDTO responseDTO = new UsuarioResponseDTO(1L, "Usuario Mock", "mock@email.com", new ArrayList<>());

        // 2. Simula o findById (encontra o 'usuarioMock' do @BeforeEach)
        when(usuarioRepository.findById(idParaBuscar)).thenReturn(Optional.of(usuarioMock));

        // 3. Simula o conversor de DTO
        when(convertToDTO.convertUserToUserDTO(usuarioMock)).thenReturn(responseDTO);

        // --- Ação (Act) ---
        UsuarioResponseDTO resultado = usuarioService.encontrarUsuario(idParaBuscar);

        // --- Verificação (Assert/Verify) ---

        // 1. Verifica se o DTO retornado é o correto
        assertNotNull(resultado);
        assertEquals(1L, resultado.id());
        assertEquals("mock@email.com", resultado.email());

        // 2. Verifica se 'findById' foi chamado
        verify(usuarioRepository, times(1)).findById(idParaBuscar);

        // 3. Verifica se o conversor foi chamado
        verify(convertToDTO, times(1)).convertUserToUserDTO(usuarioMock);
    }

    @Test
    @DisplayName("Deve lançar UsuarioNaoEncontradoException ao tentar encontrar um ID inexistente")
    void deveLancarExcecaoAoEncontrarUsuarioInexistente() {
        // --- Cenário (Arrange) ---
        long idInexistente = 99L;

        // 1. Simula o findById (não encontra o usuário)
        when(usuarioRepository.findById(idInexistente)).thenReturn(Optional.empty());

        // --- Ação (Act) e Verificação (Assert) ---

        // 1. Verifica se a exceção CORRETA é lançada
        UsuarioNaoEncontradoException exception = assertThrows(
                UsuarioNaoEncontradoException.class,
                () -> {
                    usuarioService.encontrarUsuario(idInexistente);
                }
        );

        // 2. Verifica a mensagem
        assertEquals("Usuario não encontrado", exception.getMessage());

        // 3. Verifica se 'findById' foi chamado
        verify(usuarioRepository, times(1)).findById(idInexistente);

        // 4. GARANTE que o conversor NUNCA foi chamado
        verify(convertToDTO, never()).convertUserToUserDTO(any(Usuario.class));
    }

    @Test
    @DisplayName("Deve atualizar todos os campos de um usuário com sucesso")
    void deveAtualizarUsuarioCompleto() {
        // --- Cenário (Arrange) ---
        long idParaAtualizar = 1L;
        UsuarioRequestDTO requestDTO = new UsuarioRequestDTO(
                "Nome Novo",
                "emailnovo@email.com",
                "novaSenha123"
        );

        // 1. Simula o findById (encontra o 'usuarioMock' do @BeforeEach)
        when(usuarioRepository.findById(idParaAtualizar)).thenReturn(Optional.of(usuarioMock));

        // 2. Simula a checagem de e-mail (retorna false = não existe)
        when(usuarioRepository.existsByEmail("emailnovo@email.com")).thenReturn(false);

        // 3. Simula o encoder
        when(encoder.encode("novaSenha123")).thenReturn("novaSenhaCriptografada");

        // 4. Simula o save
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioMock);

        // 5. Simula o conversor de DTO
        when(convertToDTO.convertUserToUserDTO(usuarioMock)).thenReturn(
                // Retorna um DTO com os dados já atualizados
                new UsuarioResponseDTO(1L, "Nome Novo", "emailnovo@email.com", new ArrayList<>())
        );

        // --- Ação (Act) ---
        UsuarioResponseDTO resultado = usuarioService.atualizarUsuario(idParaAtualizar, requestDTO);

        // --- Verificação (Assert/Verify) ---
        // 1. Verifica o resultado
        assertNotNull(resultado);
        assertEquals("Nome Novo", resultado.nome());
        assertEquals("emailnovo@email.com", resultado.email());

        // 2. Verifica se o objeto 'usuarioMock' foi modificado ANTES de salvar
        assertEquals("Nome Novo", usuarioMock.getNome());
        assertEquals("emailnovo@email.com", usuarioMock.getEmail());
        assertEquals("novaSenhaCriptografada", usuarioMock.getSenha());

        // 3. Verifica se todos os mocks foram chamados
        verify(usuarioRepository, times(1)).findById(idParaAtualizar);
        verify(usuarioRepository, times(1)).existsByEmail("emailnovo@email.com");
        verify(encoder, times(1)).encode("novaSenha123");
        verify(usuarioRepository, times(1)).save(usuarioMock);
    }

    @Test
    @DisplayName("Deve lançar UsuarioNaoEncontradoException ao tentar atualizar usuário inexistente")
    void deveLancarExcecaoAoAtualizarUsuarioInexistente() {
        // --- Cenário (Arrange) ---
        long idInexistente = 99L;
        UsuarioRequestDTO requestDTO = new UsuarioRequestDTO("Nome Novo", null, null);

        // 1. Simula o findById (não encontra)
        when(usuarioRepository.findById(idInexistente)).thenReturn(Optional.empty());

        // --- Ação (Act) e Verificação (Assert) ---
        UsuarioNaoEncontradoException exception = assertThrows(
                UsuarioNaoEncontradoException.class,
                () -> {
                    usuarioService.atualizarUsuario(idInexistente, requestDTO);
                }
        );

        assertEquals("Usuario não encontrado", exception.getMessage());

        // 2. GARANTE que nada mais foi chamado
        verify(usuarioRepository, never()).existsByEmail(anyString());
        verify(encoder, never()).encode(anyString());
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Deve lançar EmailExistenteException ao tentar atualizar para um email duplicado")
    void deveLancarExcecaoAoAtualizarParaEmailDuplicado() {
        // --- Cenário (Arrange) ---
        long idParaAtualizar = 1L;
        UsuarioRequestDTO requestDTO = new UsuarioRequestDTO(null, "emailduplicado@email.com", null);

        // 1. Simula o findById (encontra o 'usuarioMock')
        when(usuarioRepository.findById(idParaAtualizar)).thenReturn(Optional.of(usuarioMock));

        // 2. Simula a checagem de e-mail (retorna true = JÁ EXISTE)
        when(usuarioRepository.existsByEmail("emailduplicado@email.com")).thenReturn(true);

        // --- Ação (Act) e Verificação (Assert) ---
        EmailExistenteException exception = assertThrows(
                EmailExistenteException.class,
                () -> {
                    usuarioService.atualizarUsuario(idParaAtualizar, requestDTO);
                }
        );

        assertEquals("Esse email já existe", exception.getMessage());

        // 3. GARANTE que o save não foi chamado
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }
}