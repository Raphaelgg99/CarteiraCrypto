package com.potfoliomoedas.portfolio.service;

import com.potfoliomoedas.portfolio.dto.MoedaPrecos;
import com.potfoliomoedas.portfolio.exception.MoedaNaoEncontradaException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class CoinGeckoService {

    @Autowired
    private RestTemplate restTemplate;

    // URL da API (simples, só preço)
    private final String API_URL = "https://api.coingecko.com/api/v3/simple/price";

    /**
     * Busca o preço atual de uma moeda em BRL.
     * O resultado será guardado em cache por 5 minutos.
     */
    @Cacheable(value = "coinPrices", key = "#coinId")
    public MoedaPrecos getPrecoAtual(String coinId) {
        System.out.println(">>> BUSCANDO PREÇO EM REAL NA API EXTERNA PARA: " + coinId);

        // Monta a URL: .../price?ids=bitcoin&vs_currencies=brl
        String url = String.format("%s?ids=%s&vs_currencies=brl,usd,eur", API_URL, coinId);

        System.out.println("Buscando valor em " + url);

        Map<String, Map<String, Number>> response;
        try {
            // A resposta da CoinGecko é um JSON aninhado:
            // { "bitcoin": { "brl": 350000.00 } }

            // Usamos Map para não precisar criar DTOs complexos
            response = restTemplate.getForObject(url, Map.class);
        } catch (Exception e) {
            // Se a API da CoinGecko falhar ou a moeda não existir
            throw new RuntimeException("Erro ao buscar preço da API: " + e.getMessage());
        }
        if (response != null && response.containsKey(coinId)) {
            // --- 2. MUDE AQUI ---
            // Pegue o preço como um "Number" genérico
            Number precoEmReal = response.get(coinId).get("brl");
            Number precoEmDolar = response.get(coinId).get("usd");
            Number precoEmEuro = response.get(coinId).get("eur");

            Double doublePrecoEmReal = precoEmReal.doubleValue();
            Double doublePrecoEmDolar = precoEmDolar.doubleValue();
            Double doublePrecoEmEuro = precoEmEuro.doubleValue();

            // Converta o "Number" para "Double" com segurança
            return new MoedaPrecos(doublePrecoEmReal, doublePrecoEmDolar, doublePrecoEmEuro);
        }
        throw new MoedaNaoEncontradaException("Não foi possível encontra a moeda " + coinId);
    }
}

