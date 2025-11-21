package com.potfoliomoedas.portfolio.service;

import com.potfoliomoedas.portfolio.config.JWTCreator;
import com.potfoliomoedas.portfolio.config.JWTObject;
import com.potfoliomoedas.portfolio.config.SecurityConfig;
import com.potfoliomoedas.portfolio.dto.Login;
import com.potfoliomoedas.portfolio.dto.Sessao;
import com.potfoliomoedas.portfolio.exception.InvalidCredentialsException;
import com.potfoliomoedas.portfolio.model.Usuario;
import com.potfoliomoedas.portfolio.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Date;

@Service
public class LoginService {

    @Autowired
    private UsuarioRepository repository;
    @Autowired
    private BCryptPasswordEncoder encoder;
    @Autowired
    private JWTCreator jwtCreator;
    @Autowired
    private SecurityConfig securityConfig;

    public Sessao logar(Login login) {
        Usuario usuario = repository.findByEmail(login.email())
                .orElseThrow(() -> new InvalidCredentialsException("Credenciais inválidas"));

        boolean passwordOk = encoder.matches(login.senha(), usuario.getSenha());
        if (!passwordOk) {
            throw new InvalidCredentialsException("Credenciais inválidas");
        }
        System.out.println("Roles associadas ao usuário: " + usuario.getRoles());

        JWTObject jwtObject = new JWTObject();
        jwtObject.setIssuedAt(new Date(System.currentTimeMillis()));
        jwtObject.setExpiration(new Date(System.currentTimeMillis() + securityConfig.getEXPIRATION()));
        jwtObject.setRoles(usuario.getRoles());
        jwtObject.setSubject(usuario.getEmail());

        String token = jwtCreator.gerarToken(jwtObject);

        return new Sessao(usuario.getEmail(), token);
    }
}