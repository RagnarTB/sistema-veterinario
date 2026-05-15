package com.veterinaria.seguridad;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtServicio jwtServicio;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtServicio jwtServicio, UserDetailsService userDetailsService) {
        this.jwtServicio = jwtServicio;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        // 1. Extraer el header "Authorization"
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        // Si no hay header o no empieza con "Bearer ", lo ignoramos y seguimos (Spring
        // Security lo bloqueará después si la ruta es privada)
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2. Extraer el token (quitamos la palabra "Bearer " que tiene 7 letras)
        jwt = authHeader.substring(7);
        try {
            userEmail = jwtServicio.extraerUsername(jwt);
        } catch (JwtException | IllegalArgumentException ex) {
            // Token inválido/expirado/malformado: no autenticamos y dejamos que Spring Security
            // responda 401 si el endpoint lo requiere.
            filterChain.doFilter(request, response);
            return;
        }

        // 3. Si hay email en el token y el usuario aún no está autenticado en este hilo
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            try {
                // 4. Extraer roles del token sin tocar la BD
                java.util.List<String> rolesStr = jwtServicio.extraerRoles(jwt);
                
                java.util.List<org.springframework.security.core.authority.SimpleGrantedAuthority> authorities = 
                    rolesStr != null ? rolesStr.stream()
                        .map(org.springframework.security.core.authority.SimpleGrantedAuthority::new)
                        .toList() : java.util.Collections.emptyList();

                // 5. Autenticar usando los datos del JWT
                org.springframework.security.core.userdetails.User principal = 
                    new org.springframework.security.core.userdetails.User(userEmail, "", authorities);

                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        principal, null, authorities);

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            } catch (Exception ex) {
                // Token parse failed for roles, ignore and continue chain
            }
        }

        filterChain.doFilter(request, response); // Continuar con la petición
    }
}