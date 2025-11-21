package com.potfoliomoedas.portfolio.controller.admin;

import com.potfoliomoedas.portfolio.service.admin.CarteiraServiceAdmin;
import com.potfoliomoedas.portfolio.service.admin.impl.CarteiraServiceAdminImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("usuario/admin/carteira")
public class CarteiraControllerAdmin {

    @Autowired
    CarteiraServiceAdminImpl carteiraService;

    @DeleteMapping("{id}/{coinId}")
    public ResponseEntity<Void> excluirMoeda(@PathVariable String coinId, @PathVariable Long id ){
        carteiraService.deletarMoeda(coinId, id);
        return ResponseEntity.noContent().build();
    }
}
