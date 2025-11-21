package com.potfoliomoedas.portfolio.repository;

import com.potfoliomoedas.portfolio.model.Moeda;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MoedaRepository extends JpaRepository<Moeda, Long> {
    // O Spring Data JPA vai criar magicamente este método para nós:
    // "Encontre uma lista de CoinPortfolio baseando-se no ID do Usuário"
    List<Moeda> findByUsuarioId(Long usuarioId);

    // Você também pode querer um método para encontrar uma moeda específica
    // no portfólio de um usuário
    // "Encontre um CoinPortfolio baseando-se no ID do Usuário E no coinId"
    Optional<Moeda> findByUsuarioIdAndCoinId(Long usuarioId, String coinId);
}

