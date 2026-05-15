package com.veterinaria.servicios.auth;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import com.veterinaria.dtos.AuthResponseDTO;
import com.veterinaria.dtos.GoogleLoginRequestDTO;
import com.veterinaria.excepciones.ResourceNotFoundException;
import com.veterinaria.excepciones.TokenInvalidException;
import com.veterinaria.modelos.Cliente;
import com.veterinaria.modelos.Rol;
import com.veterinaria.modelos.Usuario;
import com.veterinaria.respositorios.ClienteRepositorio;
import com.veterinaria.respositorios.RolRespositorio;
import com.veterinaria.respositorios.UsuarioRepositorio;
import com.veterinaria.seguridad.JwtServicio;
import com.veterinaria.servicios.GoogleTokenVerifierServicio;
import com.veterinaria.servicios.RefreshTokenServicio;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;

@Service
public class AuthGoogleServicio {

    private final UsuarioRepositorio usuarioRepositorio;
    private final RolRespositorio rolRespositorio;
    private final ClienteRepositorio clienteRepositorio;
    private final PasswordEncoder passwordEncoder;
    private final JwtServicio jwtServicio;
    private final org.springframework.security.core.userdetails.UserDetailsService userDetailsService;
    private final RefreshTokenServicio refreshTokenServicio;
    private final GoogleTokenVerifierServicio googleTokenVerifier;

    public AuthGoogleServicio(
            UsuarioRepositorio usuarioRepositorio,
            RolRespositorio rolRespositorio,
            ClienteRepositorio clienteRepositorio,
            PasswordEncoder passwordEncoder,
            JwtServicio jwtServicio,
            org.springframework.security.core.userdetails.UserDetailsService userDetailsService,
            RefreshTokenServicio refreshTokenServicio,
            GoogleTokenVerifierServicio googleTokenVerifier) {
        this.usuarioRepositorio = usuarioRepositorio;
        this.rolRespositorio = rolRespositorio;
        this.clienteRepositorio = clienteRepositorio;
        this.passwordEncoder = passwordEncoder;
        this.jwtServicio = jwtServicio;
        this.userDetailsService = userDetailsService;
        this.refreshTokenServicio = refreshTokenServicio;
        this.googleTokenVerifier = googleTokenVerifier;
    }

    @Transactional
    public Object loginConGoogle(GoogleLoginRequestDTO dto) {
        GoogleIdToken.Payload payload = googleTokenVerifier.verificarToken(dto.getIdToken());
        String email = payload.getEmail();
        Boolean emailVerified = payload.getEmailVerified();

        if (emailVerified == null || !emailVerified) {
            throw new TokenInvalidException("El email de Google no está verificado");
        }

        java.util.Optional<Usuario> usuarioOpt = usuarioRepositorio.findByEmail(email);

        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            // Vincular cuenta si no lo está
            if (usuario.getGoogleSubject() == null) {
                usuario.setGoogleSubject(payload.getSubject());
                usuario.setGoogleVinculado(true);
                usuarioRepositorio.save(usuario);
            }

            if (usuario.getActivo()) {
                // Loguear usando los mismos métodos que en login
                org.springframework.security.core.userdetails.UserDetails userDetails = userDetailsService.loadUserByUsername(email);
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
                String refreshToken = refreshTokenServicio.crearRefreshTokenParaUsuario(usuario);

                java.util.List<Long> sedeIds = null;
                if (usuario.getEmpleado() != null) {
                    sedeIds = usuario.getEmpleado().getSedes().stream()
                            .map(com.veterinaria.modelos.Sede::getId)
                            .collect(java.util.stream.Collectors.toList());
                }

                return new AuthResponseDTO(token, refreshToken, email, rolesDisponibles, sedeIds, requiresRoleSelection);
            } else {
                // Usuario existe pero inactivo (Falta Completar Registro)
                if (usuario.getNombre() == null) {
                    usuario.setNombre((String) payload.get("given_name"));
                    usuario.setApellido((String) payload.get("family_name"));
                    usuarioRepositorio.save(usuario);
                }

                java.util.Map<String, Object> registroRequerido = new java.util.HashMap<>();
                registroRequerido.put("requireRegistration", true);
                registroRequerido.put("email", email);
                registroRequerido.put("nombre", payload.get("given_name"));
                registroRequerido.put("apellido", payload.get("family_name"));
                registroRequerido.put("googleToken", dto.getIdToken());
                return registroRequerido;
            }
        } else {
            // Usuario NO existe: Crear INACTIVO
            Rol rolCliente = rolRespositorio.findByNombre("ROLE_CLIENTE")
                    .orElseThrow(() -> new ResourceNotFoundException("El rol ROLE_CLIENTE no existe"));

            Usuario nuevoUsuario = new Usuario();
            nuevoUsuario.setEmail(email);
            nuevoUsuario.setPassword(passwordEncoder.encode(java.util.UUID.randomUUID().toString()));
            nuevoUsuario.setGoogleSubject(payload.getSubject());
            nuevoUsuario.setGoogleVinculado(true);
            nuevoUsuario.setActivo(false);
            nuevoUsuario.setNombre((String) payload.get("given_name"));
            nuevoUsuario.setApellido((String) payload.get("family_name"));
            nuevoUsuario.setDni("");
            nuevoUsuario.setTelefono("");
            nuevoUsuario.getRoles().add(rolCliente);
            usuarioRepositorio.save(nuevoUsuario);

            Cliente nuevoCliente = new Cliente();
            nuevoCliente.setUsuario(nuevoUsuario);
            nuevoCliente.setActivo(false);
            clienteRepositorio.save(nuevoCliente);

            // Devolver requireRegistration
            java.util.Map<String, Object> registroRequerido = new java.util.HashMap<>();
            registroRequerido.put("requireRegistration", true);
            registroRequerido.put("email", email);
            registroRequerido.put("nombre", payload.get("given_name"));
            registroRequerido.put("apellido", payload.get("family_name"));
            registroRequerido.put("googleToken", dto.getIdToken());
            return registroRequerido;
        }
    }
}
