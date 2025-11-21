package com.potfoliomoedas.portfolio.dto;

import com.potfoliomoedas.portfolio.model.Moeda;

import java.util.List;

public record UsuarioResponseDTO(Long id, String nome, String email, List<MoedaDTO> carteira) {
}
