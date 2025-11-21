package com.potfoliomoedas.portfolio.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "tb_moeda")
@Data
public class Moeda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // O ID da moeda na API (ex: "bitcoin", "ethereum")
    @Column(nullable = false)
    private String coinId;

    // A quantidade que o usuário possui
    @Column(nullable = false)
    private Double quantidade;

    // O relacionamento: Muitas moedas (CoinPortfolio) pertencem a UM Usuário
    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;
}