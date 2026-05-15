package com.veterinaria.servicios.auth;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import com.veterinaria.dtos.AuthResponseDTO;
import com.veterinaria.dtos.CambiarPasswordRequestDTO;
import com.veterinaria.dtos.LoginRequestDTO;
import com.veterinaria.dtos.MensajeResponseDTO;
import com.veterinaria.excepciones.BusinessLogicException;
import com.veterinaria.excepciones.ResourceNotFoundException;
import com.veterinaria.excepciones.TokenInvalidException;
import com.veterinaria.modelos.Usuario;
import com.veterinaria.respositorios.UsuarioRepositorio;
import com.veterinaria.seguridad.JwtServicio;
import com.veterinaria.servicios.RefreshTokenServicio;

@Service
public class AuthLoginServicio {

    private final UsuarioRepositorio usuarioRepositorio;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtServicio jwtServicio;
    private final org.springframework.security.core.userdetails.UserDetailsService userDetailsService;
    private final RefreshTokenServicio refreshTokenServicio;

    public AuthLoginServicio(
            UsuarioRepositorio usuarioRepositorio,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtServicio jwtServicio,
            org.springframework.security.core.userdetails.UserDetailsService userDetailsService,
            RefreshTokenServicio refreshTokenServicio) {
        this.usuarioRepositorio = usuarioRepositorio;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtServicio = jwtServicio;
        this.userDetailsService = userDetailsService;
        this.refreshTokenServicio = refreshTokenServicio;
    }

    public AuthResponseDTO login(LoginRequestDTO dto) {
        authenticationManager.authenticate(
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        dto.getEmail(), dto.getPassword()));

        org.springframework.security.core.userdetails.UserDetails userDetails = userDetailsService
                .loadUserByUsername(dto.getEmail());

        java.util.List<String> rolesDisponibles = userDetails.getAuthorities().stream()
                .map(auth -> auth.getAuthority())
                .collect(java.util.stream.Collectors.toList());

        java.util.Map<String, Object> extraClaims = new java.util.HashMap<>();

        boolean requiresRoleSelection = rolesDisponibles.size() > 1;
        java.util.List<String> rolesToken;
        if (requiresRoleSelection) {
            rolesToken = java.util.Collections.singletonList("ROLE_PRE_AUTH");
        } else {
            rolesToken = rolesDisponibles;
        }

        extraClaims.put("roles", rolesToken);

        String token = jwtServicio.generarToken(extraClaims, userDetails);

        Usuario usuario = usuarioRepositorio.findByEmail(dto.getEmail())
                .orElseThrow(() -> new TokenInvalidException("Usuario no válido"));

        String refreshToken = refreshTokenServicio.crearRefreshTokenParaUsuario(usuario);

        java.util.List<Long> sedeIds = null;
        if (usuario.getEmpleado() != null) {
            sedeIds = usuario.getEmpleado().getSedes().stream()
                    .map(com.veterinaria.modelos.Sede::getId)
                    .collect(java.util.stream.Collectors.toList());
        }

        return new AuthResponseDTO(token, refreshToken, dto.getEmail(), rolesDisponibles, sedeIds, requiresRoleSelection);
    }

    @Transactional
    public MensajeResponseDTO cambiarPassword(String emailUsuarioLogueado, CambiarPasswordRequestDTO dto) {
        Usuario usuario = usuarioRepositorio.findByEmail(emailUsuarioLogueado)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        if (!passwordEncoder.matches(dto.getPasswordActual(), usuario.getPassword())) {
            throw new BusinessLogicException("La contraseña actual es incorrecta");
        }

        if (passwordEncoder.matches(dto.getPasswordNueva(), usuario.getPassword())) {
            throw new BusinessLogicException("La nueva contraseña no puede ser igual a la anterior");
        }

        usuario.setPassword(passwordEncoder.encode(dto.getPasswordNueva()));
        usuarioRepositorio.save(usuario);

        refreshTokenServicio.revocarTodosLosTokensDeUsuario(usuario.getId());

        return new MensajeResponseDTO("Contraseña actualizada correctamente");
    }

    public AuthResponseDTO refreshToken(String refreshTokenRaw) {
        String refreshTokenNuevo = refreshTokenServicio.rotarRefreshToken(refreshTokenRaw);

        Usuario usuario = refreshTokenServicio.obtenerUsuarioDesdeRefreshToken(refreshTokenNuevo);
        org.springframework.security.core.userdetails.UserDetails userDetails = userDetailsService
                .loadUserByUsername(usuario.getEmail());

        java.util.List<String> roles = userDetails.getAuthorities().stream()
                .map(auth -> auth.getAuthority())
                .collect(java.util.stream.Collectors.toList());

        java.util.Map<String, Object> extraClaims = new java.util.HashMap<>();
        extraClaims.put("roles", roles);

        String token = jwtServicio.generarToken(extraClaims, userDetails);

        java.util.List<Long> sedeIds = null;
        if (usuario.getEmpleado() != null) {
            sedeIds = usuario.getEmpleado().getSedes().stream()
                    .map(com.veterinaria.modelos.Sede::getId)
                    .collect(java.util.stream.Collectors.toList());
        }

        return new AuthResponseDTO(token, refreshTokenNuevo, usuario.getEmail(), roles, sedeIds);
    }

    @Transactional
    public MensajeResponseDTO logout(String refreshTokenRaw) {
        refreshTokenServicio.revocarRefreshToken(refreshTokenRaw);
        return new MensajeResponseDTO("Sesión cerrada correctamente");
    }

    @Transactional
    public AuthResponseDTO seleccionarRol(String email, String rolSeleccionado) {
        Usuario usuario = usuarioRepositorio.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        boolean tieneRol = usuario.getRoles().stream()
                .anyMatch(r -> r.getNombre().equals(rolSeleccionado));

        if (!tieneRol) {
            throw new BusinessLogicException("El usuario no posee el rol seleccionado: " + rolSeleccionado);
        }

        java.util.Map<String, Object> extraClaims = new java.util.HashMap<>();
        extraClaims.put("roles", java.util.Collections.singletonList(rolSeleccionado));

        org.springframework.security.core.userdetails.UserDetails userDetails =
                userDetailsService.loadUserByUsername(email);
        String token = jwtServicio.generarToken(extraClaims, userDetails);
        String refreshToken = refreshTokenServicio.crearRefreshTokenParaUsuario(usuario);

        java.util.List<Long> sedeIds = null;
        if (usuario.getEmpleado() != null) {
            sedeIds = usuario.getEmpleado().getSedes().stream()
                    .map(com.veterinaria.modelos.Sede::getId)
                    .collect(java.util.stream.Collectors.toList());
        }

        return new AuthResponseDTO(token, refreshToken, email, java.util.Collections.singletonList(rolSeleccionado), sedeIds);
    }
}
