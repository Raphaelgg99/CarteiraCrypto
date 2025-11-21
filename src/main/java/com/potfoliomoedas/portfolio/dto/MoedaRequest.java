package com.potfoliomoedas.portfolio.dto;

public record MoedaRequest(
        String coinId, // Ex: "bitcoin"
        Double quantidade // Ex: 2.5
) {}
