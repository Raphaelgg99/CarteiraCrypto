package com.potfoliomoedas.portfolio.controller.user;

import com.potfoliomoedas.portfolio.dto.UsuarioRequestDTO;
import com.potfoliomoedas.portfolio.dto.UsuarioResponseDTO;
import com.potfoliomoedas.portfolio.service.user.UsuarioServiceUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/usuario")
public class UsuarioControllerUser {

    @Autowired
    private UsuarioServiceUser usuarioService;

    @DeleteMapping
    public ResponseEntity<Void> excluirUsuario(){
        usuarioService.excluirUsuario();
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/criarusuario")
    public ResponseEntity<UsuarioResponseDTO> criarUsuario(@RequestBody UsuarioRequestDTO requestDTO) {
        // 1. Crie o usuário e pegue o DTO de resposta
        UsuarioResponseDTO usuarioCriado = usuarioService.criarUsuario(requestDTO);

        // 2. Construa a URI para o novo usuário (ex: http://localhost:8080/usuario/11)
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest() // Pega a URL atual ("/usuario/adicionar")
               .path("/{id}")        // Adiciona "/{id}"
                .buildAndExpand(usuarioCriado.id()) // Substitui o {id} pelo ID do usuário
                .toUri();             // Converte para URI

        // 3. Retorne 201 Created, com a URI no cabeçalho Location e o DTO no corpo
        return ResponseEntity.created(location).body(usuarioCriado);
    }


    @PutMapping
    public ResponseEntity<UsuarioResponseDTO> atualizarUsuario(@RequestBody UsuarioRequestDTO requestDTO){
        return ResponseEntity.ok(usuarioService.atualizarUsuario(requestDTO));
    }
}
