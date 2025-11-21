package com.potfoliomoedas.portfolio.service.user.impl;

import com.potfoliomoedas.portfolio.component.UsuarioLogado;
import com.potfoliomoedas.portfolio.dto.*;
import com.potfoliomoedas.portfolio.model.Moeda;
import com.potfoliomoedas.portfolio.model.Usuario;
import com.potfoliomoedas.portfolio.repository.MoedaRepository;
import com.potfoliomoedas.portfolio.repository.UsuarioRepository;
import com.potfoliomoedas.portfolio.service.CoinGeckoService;
import com.potfoliomoedas.portfolio.service.user.CarteiraServiceUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;

@Service
public class CarteiraServiceUserImpl implements CarteiraServiceUser {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private MoedaRepository moedaRepository;

    @Autowired
    private CoinGeckoService coinGeckoService;

    @Autowired
    UsuarioLogado usuarioLogado;

    /**
     * Adiciona uma moeda ao portfólio do usuário logado.
     * Se a moeda já existir, apenas soma a quantidade.
     */
    public MoedaDTO adicionarMoeda(MoedaRequest requestDTO) {
        // 1. Pega o usuário logado
        Usuario usuario = usuarioLogado.getUsuarioLogado();

        // 2. Verifica se o usuário já tem essa moeda
        Optional<Moeda> moeda = moedaRepository
                .findByUsuarioIdAndCoinId(usuario.getId(), requestDTO.coinId());

        Moeda moedaParaSalvar;

        if (moeda.isPresent()) {
            // Se já tem, atualiza a quantidade
            moedaParaSalvar = moeda.get();
            moedaParaSalvar.setQuantidade(moedaParaSalvar.getQuantidade() + requestDTO.quantidade());
        } else {
            // Se não tem, cria uma nova
            moedaParaSalvar = new Moeda();
            moedaParaSalvar.setCoinId(requestDTO.coinId());
            moedaParaSalvar.setQuantidade(requestDTO.quantidade());
            moedaParaSalvar.setUsuario(usuario);
        }
        coinGeckoService.getPrecoAtual(moedaParaSalvar.getCoinId());
        Moeda moedaSalva = moedaRepository.save(moedaParaSalvar);
        return new MoedaDTO(moedaSalva.getCoinId(), moedaSalva.getQuantidade());
    }

    /**
     * Calcula o valor total da carteira do usuário logado.
     */
    public Carteira calcularValorTotal() {
        // 1. Pega o usuário logado
        Usuario usuario = usuarioLogado.getUsuarioLogado();

        // 2. Busca todas as moedas desse usuário no nosso banco
        List<Moeda> carteira = moedaRepository.findByUsuarioId(usuario.getId());

        double valorTotalEmSuaCarteiraBRL = 0.0;
        double valorTotalEmSuaCarteiraUSD = 0.0;
        double valorTotalEmSuaCarteiraEUR = 0.0;
        List<MoedaResponse> moedas = new ArrayList<>();

        // 3. Itera sobre cada moeda que o usuário possui
        for (Moeda moeda : carteira) {
            MoedaPrecos moedaPrecos = coinGeckoService.getPrecoAtual(moeda.getCoinId());

            Double precoAtualEmReal = moedaPrecos.valorBRL();

            double valorEmSuaCarteiraBRL = precoAtualEmReal * moeda.getQuantidade();

            Double precoAtualEmDolar = moedaPrecos.valorUSD();

            double valorEmSuaCarteiraUSD = precoAtualEmDolar * moeda.getQuantidade();

            double precoAtualEmEuro = moedaPrecos.valorEUR();

            double valorEmSuaCarteiraEUR = precoAtualEmEuro * moeda.getQuantidade();

            // 5. Adiciona aos DTOs de resposta
            moedas.add(new MoedaResponse(
                    moeda.getCoinId(),
                    moeda.getQuantidade(),
                    round(precoAtualEmReal),
                    round(valorEmSuaCarteiraBRL),
                    round(precoAtualEmDolar),
                    round(valorEmSuaCarteiraUSD),
                    round(precoAtualEmEuro),
                    round(valorEmSuaCarteiraEUR)
            ));

            valorTotalEmSuaCarteiraBRL += valorEmSuaCarteiraBRL;
            valorTotalEmSuaCarteiraUSD += valorEmSuaCarteiraUSD;
            valorTotalEmSuaCarteiraEUR += valorEmSuaCarteiraEUR;

        }

        // 6. Retorna o DTO completo
        return new Carteira(
                usuario.getEmail(),
                round(valorTotalEmSuaCarteiraBRL),
                round(valorTotalEmSuaCarteiraUSD),
                round(valorTotalEmSuaCarteiraEUR),
                moedas
        );
    }

    @Override
    public void deletarMoeda(String coinId) {
        Usuario usuario = usuarioLogado.getUsuarioLogado();
        // 1. Encontre a moeda EXATA que você quer deletar
        Moeda moedaParaDeletar = moedaRepository.findByUsuarioIdAndCoinId(usuario.getId(), coinId.trim())
                .orElseThrow(() -> new RuntimeException("Moeda não encontrada no portfólio"));

        // 2. Delete a moeda usando o repositório DELA
        moedaRepository.delete(moedaParaDeletar);
    }


    private Double round(Double valor) {
        if (valor == null) {
            return 0.0;
        }
        return BigDecimal.valueOf(valor)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }
}
