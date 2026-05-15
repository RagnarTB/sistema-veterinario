package com.veterinaria.seguridad;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import java.time.Duration;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtServicio {

    @Value("${jwt.secret}")
    private String secretKey;

    private static final Duration ACCESS_TTL = Duration.ofMinutes(15);

    // 1. GENERAR EL TOKEN (Se usa en el Login)
    public String generarToken(UserDetails userDetails) {
        return generarToken(new HashMap<>(), userDetails);
    }

    public String generarToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername()) // El subject es el email
                .setIssuedAt(new Date(System.currentTimeMillis())) // Fecha de creación
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TTL.toMillis()))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256) // Firmado con algoritmo seguro
                .compact();
    }

    // 2. VALIDAR EL TOKEN (Se usa cuando el usuario hace peticiones)
    public boolean esTokenValido(String token, UserDetails userDetails) {
        final String username = extraerUsername(token);
        return (username.equals(userDetails.getUsername())) && !esTokenExpirado(token);
    }

    public String extraerUsername(String token) {
        return extraerClaim(token, Claims::getSubject);
    }

    public boolean esTokenExpirado(String token) {
        return extraerExpiracion(token).before(new Date());
    }

    public java.util.List<String> extraerRoles(String token) {
        return extraerClaim(token, claims -> claims.get("roles", java.util.List.class));
    }

    private Date extraerExpiracion(String token) {
        return extraerClaim(token, Claims::getExpiration);
    }

    public <T> T extraerClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extraerTodosLosClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extraerTodosLosClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}