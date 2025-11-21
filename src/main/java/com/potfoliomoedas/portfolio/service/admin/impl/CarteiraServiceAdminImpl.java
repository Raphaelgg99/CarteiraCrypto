package com.potfoliomoedas.portfolio.service.admin.impl;

import com.potfoliomoedas.portfolio.dto.MoedaRequest;
import com.potfoliomoedas.portfolio.exception.MoedaNaoEncontradaException;
import com.potfoliomoedas.portfolio.exception.UsuarioNaoEncontradoException;
import com.potfoliomoedas.portfolio.model.Moeda;
import com.potfoliomoedas.portfolio.model.Usuario;
import com.potfoliomoedas.portfolio.repository.MoedaRepository;
import com.potfoliomoedas.portfolio.repository.UsuarioRepository;
import com.potfoliomoedas.portfolio.service.admin.CarteiraServiceAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CarteiraServiceAdminImpl implements CarteiraServiceAdmin {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private MoedaRepository moedaRepository;

    @Override
    public void deletarMoeda(String coinId, Long id) {
        // 1. Encontre a moeda EXATA
        Moeda moedaParaDeletar = moedaRepository.findByUsuarioIdAndCoinId(id, coinId.trim())
                .orElseThrow(() -> new MoedaNaoEncontradaException("Moeda não encontrada para o usuário " + id));

        // 2. Delete a moeda DIRETAMENTE
        moedaRepository.delete(moedaParaDeletar);
    }

}
