package com.potfoliomoedas.portfolio.component;

import com.potfoliomoedas.portfolio.exception.UsuarioNaoEncontradoException;
import com.potfoliomoedas.portfolio.model.Usuario;
import com.potfoliomoedas.portfolio.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class UsuarioLogado {

    @Autowired
    UsuarioRepository repository;

    public Usuario getUsuarioLogado() {
        // Pega o email (ou "subject") do usuário que o JWTFilter colocou no contexto
        String usuarioEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        return repository.findByEmail(usuarioEmail)
                .orElseThrow(() -> new UsuarioNaoEncontradoException("Usuário não encontrado: " + usuarioEmail));
    }
}
