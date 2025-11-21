package com.potfoliomoedas.portfolio;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching // Habilita o cache do Spring
public class CarteiraCryptoApplication {
    public static void main(String[] args) {
		SpringApplication.run(CarteiraCryptoApplication.class, args);
	}

}
