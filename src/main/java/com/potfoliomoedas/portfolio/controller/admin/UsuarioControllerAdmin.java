package com.potfoliomoedas.portfolio.controller.admin;

import com.potfoliomoedas.portfolio.dto.UsuarioRequestDTO;
import com.potfoliomoedas.portfolio.dto.UsuarioResponseDTO;
import com.potfoliomoedas.portfolio.service.admin.UsuarioServiceAdmin;
import com.potfoliomoedas.portfolio.service.user.UsuarioServiceUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("usuario/admin")
public class UsuarioControllerAdmin {

        @Autowired
        private UsuarioServiceAdmin usuarioService;

        @DeleteMapping("{id}")
        public ResponseEntity<Void> excluirUsuario(@PathVariable Long id){
            usuarioService.excluirUsuario(id);
            return ResponseEntity.noContent().build();
        }

        @GetMapping("{id}")
        public ResponseEntity<UsuarioResponseDTO> encontrarUsuario(@PathVariable Long id){
            return ResponseEntity.ok(usuarioService.encontrarUsuario(id));
        }

        @GetMapping("/listartodos")
        public ResponseEntity<List<UsuarioResponseDTO>> listarTodos(){
            return ResponseEntity.ok(usuarioService.listarTodos());
        }

        @PutMapping("{id}")
        public ResponseEntity<UsuarioResponseDTO> atualizarUsuario(@PathVariable Long id,
                                                                   @RequestBody UsuarioRequestDTO requestDTO){
            return ResponseEntity.ok(usuarioService.atualizarUsuario(id, requestDTO));
        }
    }

