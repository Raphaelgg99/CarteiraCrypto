package com.potfoliomoedas.portfolio.service.user;

import com.potfoliomoedas.portfolio.dto.UsuarioRequestDTO;
import com.potfoliomoedas.portfolio.dto.UsuarioResponseDTO;

import java.util.List;

public interface UsuarioServiceUser {

    UsuarioResponseDTO criarUsuario(UsuarioRequestDTO requestDTO);
    void excluirUsuario();
    UsuarioResponseDTO atualizarUsuario(UsuarioRequestDTO requestDTO);
}
