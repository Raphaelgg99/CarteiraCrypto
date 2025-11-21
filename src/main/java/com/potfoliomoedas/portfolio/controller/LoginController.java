package com.potfoliomoedas.portfolio.controller;

import com.potfoliomoedas.portfolio.dto.Login;
import com.potfoliomoedas.portfolio.dto.Sessao;
import com.potfoliomoedas.portfolio.service.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LoginController {

    @Autowired
    LoginService loginService;

    @PostMapping("/login")
    public ResponseEntity<Sessao> logar(@RequestBody Login login){
        return ResponseEntity.ok(loginService.logar(login));
    }
}