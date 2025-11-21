package com.potfoliomoedas.portfolio.service.admin.impl;

import com.potfoliomoedas.portfolio.component.ConvertToDTO;
import com.potfoliomoedas.portfolio.dto.MoedaDTO;
import com.potfoliomoedas.portfolio.dto.UsuarioRequestDTO;
import com.potfoliomoedas.portfolio.dto.UsuarioResponseDTO;
import com.potfoliomoedas.portfolio.exception.EmailExistenteException;
import com.potfoliomoedas.portfolio.exception.EmailNullException;
import com.potfoliomoedas.portfolio.exception.UsuarioNaoEncontradoException;
import com.potfoliomoedas.portfolio.model.Usuario;
import com.potfoliomoedas.portfolio.repository.UsuarioRepository;
import com.potfoliomoedas.portfolio.service.admin.UsuarioServiceAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UsuarioServiceAdminImpl implements UsuarioServiceAdmin {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private BCryptPasswordEncoder encoder;

    @Autowired
    ConvertToDTO convertToDTO;

    @Override
    public void excluirUsuario(Long id){
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNaoEncontradoException("Usuario não encontrado"));;
        usuarioRepository.delete(usuario);
    }

    @Override
    public UsuarioResponseDTO encontrarUsuario(Long id){
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNaoEncontradoException("Usuario não encontrado"));
        return convertToDTO.convertUserToUserDTO(usuario);
    }

    @Override
    public List<UsuarioResponseDTO> listarTodos(){
        return usuarioRepository.findAll()
                .stream()
                // Mapeia usando o método helper
                .map(usuario -> convertToDTO.convertUserToUserDTO(usuario))
                .collect(Collectors.toList());
    }

    @Override
    public UsuarioResponseDTO atualizarUsuario(Long id,
                                               UsuarioRequestDTO requestDTO){
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNaoEncontradoException("Usuario não encontrado"));
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

    // Adicione este método privado

}

