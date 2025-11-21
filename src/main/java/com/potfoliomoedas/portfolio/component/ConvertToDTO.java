package com.potfoliomoedas.portfolio.component;

import com.potfoliomoedas.portfolio.dto.MoedaDTO;
import com.potfoliomoedas.portfolio.dto.UsuarioResponseDTO;
import com.potfoliomoedas.portfolio.model.Usuario;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ConvertToDTO {

    public UsuarioResponseDTO convertUserToUserDTO(Usuario usuario) {
        // 1. Converte a carteira de Moeda -> MoedaDTO
        List<MoedaDTO> carteiraDTO = usuario.getCarteira()
                .stream()
                .map(moeda -> new MoedaDTO(
                        moeda.getCoinId(),
                        moeda.getQuantidade()
                ))
                .collect(Collectors.toList());

        // 2. Retorna o DTO principal
        return new UsuarioResponseDTO(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                carteiraDTO
        );
    }
}
