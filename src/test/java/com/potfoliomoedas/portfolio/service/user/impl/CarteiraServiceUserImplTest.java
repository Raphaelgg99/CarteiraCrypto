package com.potfoliomoedas.portfolio.service.user.impl;

import static org.junit.jupiter.api.Assertions.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

// ... (imports de todas as suas classes, DTOs, e exceções) ...
import com.potfoliomoedas.portfolio.component.UsuarioLogado;
import com.potfoliomoedas.portfolio.dto.Carteira;
import com.potfoliomoedas.portfolio.model.Usuario;
import com.potfoliomoedas.portfolio.model.Moeda;
import com.potfoliomoedas.portfolio.dto.MoedaRequest;
import com.potfoliomoedas.portfolio.dto.MoedaDTO;
import com.potfoliomoedas.portfolio.dto.MoedaPrecos; // O DTO que o CoinGecko retorna
import com.potfoliomoedas.portfolio.exception.MoedaNaoEncontradaException; // (ou a exceção que ele lança)

import com.potfoliomoedas.portfolio.repository.MoedaRepository;
import com.potfoliomoedas.portfolio.repository.UsuarioRepository;
import com.potfoliomoedas.portfolio.service.CoinGeckoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class CarteiraServiceUserImplTest {

    @InjectMocks
    private CarteiraServiceUserImpl carteiraService;

    // Mocks para todas as dependências
    @Mock
    private UsuarioRepository usuarioRepository; // (Embora não usado aqui, pode ser necessário em outros)

    @Mock
    private MoedaRepository moedaRepository;

    @Mock
    private CoinGeckoService coinGeckoService;

    @Mock
    private UsuarioLogado usuarioLogado;

    // Objetos de teste
    private Usuario usuarioMock;
    private MoedaPrecos precoMock;

    @BeforeEach
    void setUp() {
        // Cria um mock de usuário para todos os testes
        usuarioMock = new Usuario();
        usuarioMock.setId(1L);
        usuarioMock.setEmail("teste@email.com");

        // Cria um mock de preço para a validação
        precoMock = new MoedaPrecos(1.0, 1.0, 1.0); // (BRL, USD, EUR)
    }

    @Test
    @DisplayName("Deve adicionar uma moeda NOVA ao portfólio com sucesso")
    void deveAdicionarMoedaNovaComSucesso() {
        // --- Cenário (Arrange) ---
        MoedaRequest requestDTO = new MoedaRequest("bitcoin", 2.5);

        // 1. Simula o usuário logado
        when(usuarioLogado.getUsuarioLogado()).thenReturn(usuarioMock);

        // 2. Simula o 'find' (retorna vazio = moeda é NOVA)
        when(moedaRepository.findByUsuarioIdAndCoinId(1L, "bitcoin")).thenReturn(Optional.empty());

        // 3. Simula a validação do CoinGecko (passa com sucesso)
        when(coinGeckoService.getPrecoAtual("bitcoin")).thenReturn(precoMock);

        // 4. Captura a moeda que será salva
        ArgumentCaptor<Moeda> moedaCaptor = ArgumentCaptor.forClass(Moeda.class);

        // 5. Simula o 'save' (retorna o que foi passado para ele)
        when(moedaRepository.save(moedaCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        // --- Ação (Act) ---
        MoedaDTO resultado = carteiraService.adicionarMoeda(requestDTO);

        // --- Verificação (Assert/Verify) ---
        assertNotNull(resultado);
        assertEquals("bitcoin", resultado.coinId());
        assertEquals(2.5, resultado.quantidade());

        // Pega o objeto 'Moeda' que foi capturado no 'save'
        Moeda moedaSalva = moedaCaptor.getValue();

        // Verifica se o 'else' foi executado
        assertEquals("bitcoin", moedaSalva.getCoinId());
        assertEquals(2.5, moedaSalva.getQuantidade());
        assertEquals(usuarioMock, moedaSalva.getUsuario()); // Verifica se o usuário foi setado

        // Verifica se todos os mocks foram chamados
        verify(moedaRepository).findByUsuarioIdAndCoinId(1L, "bitcoin");
        verify(coinGeckoService).getPrecoAtual("bitcoin");
        verify(moedaRepository).save(any(Moeda.class));
    }

    @Test
    @DisplayName("Deve SOMAR a quantidade de uma moeda existente no portfólio")
    void deveSomarQuantidadeDeMoedaExistente() {
        // --- Cenário (Arrange) ---
        MoedaRequest requestDTO = new MoedaRequest("ethereum", 5.0); // Adicionando +5.0

        // 1. Simula a moeda que JÁ EXISTE no banco
        Moeda moedaExistente = new Moeda();
        moedaExistente.setId(10L);
        moedaExistente.setCoinId("ethereum");
        moedaExistente.setQuantidade(2.0); // Usuário já tinha 2.0
        moedaExistente.setUsuario(usuarioMock);

        // 2. Simula o usuário logado
        when(usuarioLogado.getUsuarioLogado()).thenReturn(usuarioMock);

        // 3. Simula o 'find' (retorna a moeda existente)
        when(moedaRepository.findByUsuarioIdAndCoinId(1L, "ethereum")).thenReturn(Optional.of(moedaExistente));

        // 4. Simula a validação do CoinGecko
        when(coinGeckoService.getPrecoAtual("ethereum")).thenReturn(precoMock);

        // 5. Simula o 'save'
        when(moedaRepository.save(any(Moeda.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // --- Ação (Act) ---
        MoedaDTO resultado = carteiraService.adicionarMoeda(requestDTO);

        // --- Verificação (Assert/Verify) ---
        assertNotNull(resultado);
        assertEquals("ethereum", resultado.coinId());
        assertEquals(7.0, resultado.quantidade()); // 2.0 (antigo) + 5.0 (novo) = 7.0

        // Verifica se os mocks foram chamados
        verify(moedaRepository).findByUsuarioIdAndCoinId(1L, "ethereum");
        verify(coinGeckoService).getPrecoAtual("ethereum");

        // Verifica se o 'save' foi chamado com o objeto 'moedaExistente' atualizado
        verify(moedaRepository).save(moedaExistente);
        assertEquals(7.0, moedaExistente.getQuantidade()); // Verifica se o objeto original foi modificado
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar adicionar moeda inválida (que falha no CoinGecko)")
    void deveLancarExcecaoQuandoMoedaInvalida() {
        // --- Cenário (Arrange) ---
        MoedaRequest requestDTO = new MoedaRequest("moedainvalida", 1.0);

        // 1. Simula o usuário logado
        when(usuarioLogado.getUsuarioLogado()).thenReturn(usuarioMock);

        // 2. Simula o 'find' (retorna vazio)
        when(moedaRepository.findByUsuarioIdAndCoinId(1L, "moedainvalida")).thenReturn(Optional.empty());

        // 3. Simula a validação do CoinGecko (LANÇA EXCEÇÃO)
        when(coinGeckoService.getPrecoAtual("moedainvalida"))
                .thenThrow(new MoedaNaoEncontradaException("Moeda não encontrada"));

        // --- Ação (Act) e Verificação (Assert) ---
        assertThrows(MoedaNaoEncontradaException.class, () -> {
            carteiraService.adicionarMoeda(requestDTO);
        });

        // --- Verificação (Verify) ---
        // GARANTE que o 'save' NUNCA foi chamado
        verify(moedaRepository, never()).save(any(Moeda.class));
    }

    @Test
    @DisplayName("Deve calcular o valor total do portfólio com múltiplas moedas")
    void deveCalcularValorTotalComSucesso() {
        // --- Cenário (Arrange) ---

        // 1. Crie as moedas que o usuário tem no banco
        Moeda bitcoin = new Moeda();
        bitcoin.setCoinId("bitcoin");
        bitcoin.setQuantidade(0.5); // Usuário tem 0.5 BTC
        bitcoin.setUsuario(usuarioMock);

        Moeda ethereum = new Moeda();
        ethereum.setCoinId("ethereum");
        ethereum.setQuantidade(10.0); // Usuário tem 10 ETH
        ethereum.setUsuario(usuarioMock);

        List<Moeda> carteiraDoBanco = List.of(bitcoin, ethereum);

        // 2. Crie os preços que a API da CoinGecko vai simular
        MoedaPrecos precoBitcoin = new MoedaPrecos(350000.00, 65000.00, 60000.00); // (BRL, USD, EUR)
        MoedaPrecos precoEthereum = new MoedaPrecos(18000.50, 3500.10, 3000.00); // (BRL, USD, EUR)

        // 3. Simula o usuário logado
        when(usuarioLogado.getUsuarioLogado()).thenReturn(usuarioMock);

        // 4. Simula o repositório (retorna a carteira com 2 moedas)
        when(moedaRepository.findByUsuarioId(usuarioMock.getId())).thenReturn(carteiraDoBanco);

        // 5. Simula o CoinGeckoService (uma chamada para cada moeda)
        when(coinGeckoService.getPrecoAtual("bitcoin")).thenReturn(precoBitcoin);
        when(coinGeckoService.getPrecoAtual("ethereum")).thenReturn(precoEthereum);

        // --- Ação (Act) ---
        Carteira resultado = carteiraService.calcularValorTotal();

        // --- Verificação (Assert/Verify) ---

        // 1. Calcule os valores esperados
        // BTC BRL: 0.5 * 350000.00 = 175000.00
        // ETH BRL: 10.0 * 18000.50 = 180005.00
        // Total BRL: 175000.00 + 180005.00 = 355005.00

        // BTC USD: 0.5 * 65000.00 = 32500.00
        // ETH USD: 10.0 * 3500.10 = 35001.00
        // Total USD: 32500.00 + 35001.00 = 67501.00

        // BTC EUR: 0.5 * 60000.00 = 30000.00
        // ETH EUR: 10.0 * 3000.00 = 30000.00
        // Total EUR: 30000.00 + 30000.00 = 60000.00

        // 2. Verifica os totais gerais (já arredondados pelo seu método 'round')
        assertNotNull(resultado);
        assertEquals(355005.00, resultado.seuSaldoTotalBRL());
        assertEquals(67501.00, resultado.seuSaldoTotalUSD());
        assertEquals(60000.00, resultado.seuSaldoTotalEUR());

        // 3. Verifica se a lista de moedas detalhadas está correta
        assertEquals(2, resultado.moedas().size());
        assertEquals("bitcoin", resultado.moedas().get(0).coinId());
        assertEquals(175000.00, resultado.moedas().get(0).seuSaldoEmBRL());
        assertEquals(32500.00, resultado.moedas().get(0).seuSaldoEmUSD());

        // 4. Verifica se os mocks foram chamados
        verify(usuarioLogado, times(1)).getUsuarioLogado();
        verify(moedaRepository, times(1)).findByUsuarioId(usuarioMock.getId());
        verify(coinGeckoService, times(2)).getPrecoAtual(anyString());
        verify(coinGeckoService).getPrecoAtual("bitcoin");
        verify(coinGeckoService).getPrecoAtual("ethereum");
    }

    @Test
    @DisplayName("Deve retornar valor total 0.0 quando a carteira estiver vazia")
    void deveRetornarZeroQuandoCarteiraVazia() {
        // --- Cenário (Arrange) ---

        // 1. Simula o usuário logado
        when(usuarioLogado.getUsuarioLogado()).thenReturn(usuarioMock);

        // 2. Simula o repositório (retorna uma LISTA VAZIA)
        when(moedaRepository.findByUsuarioId(usuarioMock.getId())).thenReturn(Collections.emptyList());

        // --- Ação (Act) ---
        Carteira resultado = carteiraService.calcularValorTotal();

        // --- Verificação (Assert/Verify) ---

        // 1. Verifica se todos os totais são 0.0
        assertNotNull(resultado);
        assertEquals(0.0, resultado.seuSaldoTotalBRL());
        assertEquals(0.0, resultado.seuSaldoTotalUSD());
        assertEquals(0.0, resultado.seuSaldoTotalEUR());

        // 2. Verifica se a lista de moedas está vazia
        assertTrue(resultado.moedas().isEmpty());

        // 3. Verifica os mocks
        verify(usuarioLogado, times(1)).getUsuarioLogado();
        verify(moedaRepository, times(1)).findByUsuarioId(usuarioMock.getId());

        // 4. GARANTE que o CoinGecko NUNCA foi chamado (pois o loop 'for' não rodou)
        verify(coinGeckoService, never()).getPrecoAtual(anyString());
    }

    @Test
    @DisplayName("Deve deletar uma moeda do portfólio do usuário logado")
    void deveDeletarMoedaComSucesso() {
        // --- Cenário (Arrange) ---
        String coinIdParaDeletar = "bitcoin";

        // 1. Crie a moeda mock que "existe" no banco
        Moeda moedaMock = new Moeda();
        moedaMock.setId(10L);
        moedaMock.setCoinId(coinIdParaDeletar);
        moedaMock.setUsuario(usuarioMock);

        // 2. Simula o usuário logado (do @BeforeEach)
        when(usuarioLogado.getUsuarioLogado()).thenReturn(usuarioMock);

        // 3. Simula o 'find' (encontra a moeda)
        when(moedaRepository.findByUsuarioIdAndCoinId(usuarioMock.getId(), coinIdParaDeletar.trim()))
                .thenReturn(Optional.of(moedaMock));

        // 4. Prepara o mock para o método 'delete' (que é void)
        doNothing().when(moedaRepository).delete(moedaMock);

        // --- Ação (Act) ---
        // Executa o método (não deve lançar exceção)
        assertDoesNotThrow(() -> {
            carteiraService.deletarMoeda(coinIdParaDeletar);
        });

        // --- Verificação (Assert/Verify) ---
        // 1. Verifica se o usuário logado foi buscado
        verify(usuarioLogado, times(1)).getUsuarioLogado();

        // 2. Verifica se a busca da moeda foi feita
        verify(moedaRepository, times(1)).findByUsuarioIdAndCoinId(usuarioMock.getId(), coinIdParaDeletar.trim());

        // 3. Verifica se 'delete' foi chamado com a moeda EXATA
        verify(moedaRepository, times(1)).delete(moedaMock);
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar deletar moeda inexistente")
    void deveLancarExcecaoAoDeletarMoedaInexistente() {
        // --- Cenário (Arrange) ---
        String coinIdInexistente = "moedafalsa";

        // 1. Simula o usuário logado
        when(usuarioLogado.getUsuarioLogado()).thenReturn(usuarioMock);

        // 2. Simula o 'find' (NÃO encontra a moeda)
        when(moedaRepository.findByUsuarioIdAndCoinId(usuarioMock.getId(), coinIdInexistente.trim()))
                .thenReturn(Optional.empty());

        // --- Ação (Act) e Verificação (Assert) ---

        // 1. Verifica se a exceção CORRETA é lançada
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> {
                    carteiraService.deletarMoeda(coinIdInexistente);
                }
        );

        // 2. (Opcional) Verifica a mensagem da exceção
        assertEquals("Moeda não encontrada no portfólio", exception.getMessage());

        // 3. Verifica se o 'find' foi chamado
        verify(moedaRepository, times(1)).findByUsuarioIdAndCoinId(usuarioMock.getId(), coinIdInexistente.trim());

        // 4. GARANTE que o 'delete' NUNCA foi chamado
        verify(moedaRepository, never()).delete(any(Moeda.class));
    }
}