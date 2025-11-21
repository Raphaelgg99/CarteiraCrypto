package com.potfoliomoedas.portfolio.service.admin;

import com.potfoliomoedas.portfolio.dto.UsuarioRequestDTO;
import com.potfoliomoedas.portfolio.dto.UsuarioResponseDTO;

import java.util.List;

public interface UsuarioServiceAdmin {

    void excluirUsuario(Long id);
    UsuarioResponseDTO encontrarUsuario(Long id);
    List<UsuarioResponseDTO> listarTodos();
    UsuarioResponseDTO atualizarUsuario(Long id, UsuarioRequestDTO requestDTO);
}