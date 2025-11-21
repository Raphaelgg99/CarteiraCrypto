package com.potfoliomoedas.portfolio.controller.user;

import com.potfoliomoedas.portfolio.dto.Carteira;
import com.potfoliomoedas.portfolio.dto.MoedaDTO;
import com.potfoliomoedas.portfolio.dto.MoedaRequest;
import com.potfoliomoedas.portfolio.model.Moeda;
import com.potfoliomoedas.portfolio.service.user.impl.CarteiraServiceUserImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("usuario/carteira") // Rota base (ex: /portfolio/adicionar)
public class CarteiraControllerUser {

    @Autowired
    private CarteiraServiceUserImpl carteiraService;

    @PostMapping("/adicionar")
    public ResponseEntity<MoedaDTO> adicionarMoeda(@RequestBody MoedaRequest request) {
        // 2. O service agora retorna o DTO seguro
        MoedaDTO moedaSalvaDTO = carteiraService.adicionarMoeda(request);

        // 3. (Opcional, mas profissional) Retorne 201 Created
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest() // Pega a URL atual ("/usuario/carteira/adicionar")
                // Vamos assumir que temos um endpoint para ver a carteira
                .path("/../") // Volta para "/usuario/carteira"
                .build()
                .toUri();

        return ResponseEntity.created(location).body(moedaSalvaDTO);
    }

    @GetMapping
    public ResponseEntity<Carteira> getValorTotal() {
        Carteira carteira = carteiraService.calcularValorTotal();
        return ResponseEntity.ok(carteira);
    }

    @DeleteMapping("{coinId}")
    public ResponseEntity<Void> excluirMoeda(@PathVariable String coinId){
        carteiraService.deletarMoeda(coinId);
        return ResponseEntity.noContent().build();
    }
}
