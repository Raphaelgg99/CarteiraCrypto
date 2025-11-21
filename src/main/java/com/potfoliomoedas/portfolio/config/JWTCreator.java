package com.potfoliomoedas.portfolio.config;

import io.jsonwebtoken.*;
import org.hibernate.annotations.Comments;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.security.SignatureException;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JWTCreator {
    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String ROLES_AUTHORITIES = "authorities";

    @Autowired
    private SecurityConfig securityConfig;

    public String gerarToken(JWTObject jwtObject) {
        List<String> rolesAsString = checkRoles(jwtObject.getRoles());

        String token = Jwts.builder()
                .setSubject(jwtObject.getSubject())
                .setIssuedAt(jwtObject.getIssuedAt())
                .setExpiration(jwtObject.getExpiration())
                .claim(ROLES_AUTHORITIES, rolesAsString)
                // 4. Use a config injetada (com getter)
                .signWith(SignatureAlgorithm.HS512, securityConfig.getKEY())
                .compact();

        // 5. Use a config injetada (com getter)
        return securityConfig.getPREFIX() + " " + token;
    }

    public JWTObject parseToken(String token) throws ExpiredJwtException, UnsupportedJwtException,
            MalformedJwtException, SignatureException {
        // 7. Use a config injetada (com getter)
        token = token.replace(securityConfig.getPREFIX() + " ", "");

        Claims claims = Jwts.parser()
                // 8. Use a config injetada (com getter)
                .setSigningKey(securityConfig.getKEY())
                .parseClaimsJws(token)
                .getBody();

        JWTObject object = new JWTObject();
        object.setSubject(claims.getSubject());
        object.setExpiration(claims.getExpiration());
        object.setIssuedAt(claims.getIssuedAt());
        object.setRoles((List) claims.get(ROLES_AUTHORITIES));

        return object;
    }



    private List<String> checkRoles(List<String> roles) {
        return roles.stream()
                .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)  // Adiciona "ROLE_" somente se não existir
                .collect(Collectors.toList());
    }


}