package com.potfoliomoedas.portfolio.service.admin.impl;

import static org.junit.jupiter.api.Assertions.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

// ... (imports de todas as suas classes, DTOs, e exceções) ...
import com.potfoliomoedas.portfolio.model.Usuario;
import com.potfoliomoedas.portfolio.model.Moeda;

import com.potfoliomoedas.portfolio.repository.MoedaRepository;
import com.potfoliomoedas.portfolio.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class CarteiraServiceAdminImplTest {

    @InjectMocks
    private CarteiraServiceAdminImpl carteiraService;

    // Mocks para as dependências
    @Mock
    private MoedaRepository moedaRepository;

    @Mock
    private UsuarioRepository usuarioRepository; // Mockado, embora não usado aqui

    // Objetos de teste
    private Moeda moedaMock;

    @BeforeEach
    void setUp() {
        // Cria um mock de moeda para os testes
        moedaMock = new Moeda();
        moedaMock.setId(10L);
        moedaMock.setCoinId("bitcoin");
    }

    @Test
    @DisplayName("Admin deve deletar uma moeda de um usuário com sucesso")
    void deveDeletarMoedaComSucesso() {
        // --- Cenário (Arrange) ---
        long usuarioId = 1L;
        String coinIdParaDeletar = "bitcoin";

        // 1. Simula o 'find' (encontra a moeda)
        when(moedaRepository.findByUsuarioIdAndCoinId(usuarioId, coinIdParaDeletar.trim()))
                .thenReturn(Optional.of(moedaMock));

        // 2. Prepara o mock para o método 'delete' (que é void)
        doNothing().when(moedaRepository).delete(moedaMock);

        // --- Ação (Act) ---
        // Executa o método (não deve lançar exceção)
        assertDoesNotThrow(() -> {
            carteiraService.deletarMoeda(coinIdParaDeletar, usuarioId);
        });

        // --- Verificação (Assert/Verify) ---

        // 1. Verifica se a busca da moeda foi feita com os IDs corretos
        verify(moedaRepository, times(1)).findByUsuarioIdAndCoinId(usuarioId, coinIdParaDeletar.trim());

        // 2. Verifica se 'delete' foi chamado com a moeda EXATA
        verify(moedaRepository, times(1)).delete(moedaMock);
    }

    @Test
    @DisplayName("Admin deve lançar exceção ao tentar deletar moeda inexistente")
    void deveLancarExcecaoAoDeletarMoedaInexistente() {
        // --- Cenário (Arrange) ---
        long usuarioId = 1L;
        String coinIdInexistente = "moedafalsa";

        // 1. Simula o 'find' (NÃO encontra a moeda)
        when(moedaRepository.findByUsuarioIdAndCoinId(usuarioId, coinIdInexistente.trim()))
                .thenReturn(Optional.empty());

        // --- Ação (Act) e Verificação (Assert) ---

        // 1. Verifica se a exceção CORRETA é lançada
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> {
                    carteiraService.deletarMoeda(coinIdInexistente, usuarioId);
                }
        );

        // 2. Verifica a mensagem da exceção
        assertEquals("Moeda não encontrada para o usuário " + usuarioId, exception.getMessage());

        // 3. GARANTE que o 'delete' NUNCA foi chamado
        verify(moedaRepository, never()).delete(any(Moeda.class));
    }
}