package com.potfoliomoedas.portfolio.dto;

public record MoedaResponse(
        String coinId,
        Double quantidade,
        Double precoAtualBRL,
        Double seuSaldoEmBRL,
        Double precoAtualUSD,
        Double seuSaldoEmUSD,
        Double precoAtualEUR,
        Double seuSaldoEmEUR
) {}