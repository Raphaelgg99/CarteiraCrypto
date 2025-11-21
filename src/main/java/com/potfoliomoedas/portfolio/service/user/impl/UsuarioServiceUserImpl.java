package com.potfoliomoedas.portfolio.service.user.impl;

import com.potfoliomoedas.portfolio.component.ConvertToDTO;
import com.potfoliomoedas.portfolio.component.UsuarioLogado;
import com.potfoliomoedas.portfolio.dto.MoedaDTO;
import com.potfoliomoedas.portfolio.dto.UsuarioRequestDTO;
import com.potfoliomoedas.portfolio.dto.UsuarioResponseDTO;
import com.potfoliomoedas.portfolio.exception.*;
import com.potfoliomoedas.portfolio.model.Usuario;
import com.potfoliomoedas.portfolio.repository.UsuarioRepository;
import com.potfoliomoedas.portfolio.service.user.UsuarioServiceUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UsuarioServiceUserImpl implements UsuarioServiceUser {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private BCryptPasswordEncoder encoder;

    @Autowired
    private UsuarioLogado usuarioLogado;

    @Autowired
    private ConvertToDTO convertToDTO;

    @Override
    public UsuarioResponseDTO criarUsuario(UsuarioRequestDTO requestDTO){
        if (requestDTO.email() == null || requestDTO.email().isBlank()){
            throw new EmailNullException("Favor colocar um email");
        }
        if(requestDTO.nome() == null || requestDTO.nome().isBlank()){
            throw new NomeNullException("Favor colocar um nome");
        }
        if(requestDTO.senha() == null || requestDTO.senha().isBlank()){
            throw new SenhaNullException("Favor colocar uma senha");
        }

        if(usuarioRepository.existsByEmail(requestDTO.email())){
            throw new EmailExistenteException("Esse email já existe");
        }
        Usuario novoUsuario = new Usuario();
        novoUsuario.setNome(requestDTO.nome());
        novoUsuario.setEmail(requestDTO.email());
        novoUsuario.setRoles(new ArrayList<>(List.of("USER")));
        novoUsuario.setSenha(encoder.encode(requestDTO.senha()));
        usuarioRepository.save(novoUsuario);
        return convertToDTO.convertUserToUserDTO(novoUsuario);
    }

    @Override
    public void excluirUsuario(){
        Usuario usuario = usuarioLogado.getUsuarioLogado();
        usuarioRepository.delete(usuario);
    }

    @Override
    public UsuarioResponseDTO atualizarUsuario(UsuarioRequestDTO requestDTO){
        Usuario usuario = usuarioLogado.getUsuarioLogado();
        if(requestDTO.nome() != null && !requestDTO.nome().isBlank()) {
            usuario.setNome(requestDTO.nome());
        }
        if(requestDTO.email() != null && !requestDTO.email().isBlank()
                && !requestDTO.email().equals(usuario.getEmail())) {
            if (usuarioRepository.existsByEmail(requestDTO.email())) {
                throw new EmailExistenteException("Esse email já existe");
            }
            usuario.setEmail(requestDTO.email());
        }
        if(requestDTO.senha() != null && !requestDTO.senha().isBlank()) {
            usuario.setSenha(encoder.encode(requestDTO.senha()));
        }
        usuarioRepository.save(usuario);
        return convertToDTO.convertUserToUserDTO(usuario);
    }

}
