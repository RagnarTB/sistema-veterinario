package com.veterinaria.servicios.auth;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import com.veterinaria.dtos.AuthResponseDTO;
import com.veterinaria.dtos.CompletarRegistroDTO;
import com.veterinaria.dtos.MensajeResponseDTO;
import com.veterinaria.dtos.RegistroClienteDTO;
import com.veterinaria.dtos.SolicitarRegistroCorreoDTO;
import com.veterinaria.eventos.RegistroCorreoEvent;
import com.veterinaria.excepciones.BusinessLogicException;
import com.veterinaria.excepciones.ResourceNotFoundException;
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
public class AuthRegistroServicio {

    private final UsuarioRepositorio usuarioRepositorio;
    private final RolRespositorio rolRespositorio;
    private final ClienteRepositorio clienteRepositorio;
    private final PasswordEncoder passwordEncoder;
    private final JwtServicio jwtServicio;
    private final org.springframework.security.core.userdetails.UserDetailsService userDetailsService;
    private final RefreshTokenServicio refreshTokenServicio;
    private final com.veterinaria.respositorios.VerificationTokenRepositorio tokenRepositorio;
    private final GoogleTokenVerifierServicio googleTokenVerifier;
    private final ApplicationEventPublisher eventPublisher;

    public AuthRegistroServicio(
            UsuarioRepositorio usuarioRepositorio,
            RolRespositorio rolRespositorio,
            ClienteRepositorio clienteRepositorio,
            PasswordEncoder passwordEncoder,
            JwtServicio jwtServicio,
            org.springframework.security.core.userdetails.UserDetailsService userDetailsService,
            RefreshTokenServicio refreshTokenServicio,
            com.veterinaria.respositorios.VerificationTokenRepositorio tokenRepositorio,
            GoogleTokenVerifierServicio googleTokenVerifier,
            ApplicationEventPublisher eventPublisher) {
        this.usuarioRepositorio = usuarioRepositorio;
        this.rolRespositorio = rolRespositorio;
        this.clienteRepositorio = clienteRepositorio;
        this.passwordEncoder = passwordEncoder;
        this.jwtServicio = jwtServicio;
        this.userDetailsService = userDetailsService;
        this.refreshTokenServicio = refreshTokenServicio;
        this.tokenRepositorio = tokenRepositorio;
        this.googleTokenVerifier = googleTokenVerifier;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public MensajeResponseDTO registrarCliente(RegistroClienteDTO dto) {
        if (usuarioRepositorio.findByEmail(dto.getEmail()).isPresent()) {
            throw new BusinessLogicException("El email ya está registrado");
        }
        if (usuarioRepositorio.existsByDni(dto.getDni())) {
            throw new BusinessLogicException("El DNI ya se encuentra registrado");
        }

        Rol rolCliente = rolRespositorio.findByNombre("ROLE_CLIENTE")
                .orElseThrow(() -> new ResourceNotFoundException("El rol ROLE_CLIENTE no existe en la BD"));

        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setEmail(dto.getEmail());
        nuevoUsuario.setPassword(passwordEncoder.encode(dto.getPassword()));
        nuevoUsuario.setNombre(dto.getNombre());
        nuevoUsuario.setApellido(dto.getApellido());
        nuevoUsuario.setDni(dto.getDni());
        nuevoUsuario.setTelefono(dto.getTelefono());
        nuevoUsuario.getRoles().add(rolCliente);

        Usuario usuarioGuardado = usuarioRepositorio.save(nuevoUsuario);

        Cliente nuevoCliente = new Cliente();
        nuevoCliente.setUsuario(usuarioGuardado);

        clienteRepositorio.save(nuevoCliente);

        return new MensajeResponseDTO("Cliente registrado con éxito");
    }

    @Transactional
    public MensajeResponseDTO solicitarRegistroCorreo(SolicitarRegistroCorreoDTO dto) {
        java.util.Optional<Usuario> usuarioOpt = usuarioRepositorio.findByEmail(dto.getEmail());
        
        Cliente cliente;
        if (usuarioOpt.isPresent()) {
            Usuario u = usuarioOpt.get();
            if (u.getActivo()) {
                throw new BusinessLogicException("El email ya está registrado y activo");
            } else {
                cliente = u.getCliente();
                if (cliente == null) throw new BusinessLogicException("Error: Cliente no encontrado");
            }
        } else {
            Rol rolCliente = rolRespositorio.findByNombre("ROLE_CLIENTE")
                    .orElseThrow(() -> new ResourceNotFoundException("El rol ROLE_CLIENTE no existe"));
                    
            Usuario nuevoUsuario = new Usuario();
            nuevoUsuario.setEmail(dto.getEmail());
            nuevoUsuario.setPassword(passwordEncoder.encode(java.util.UUID.randomUUID().toString()));
            nuevoUsuario.setActivo(false);
            nuevoUsuario.setNombre("");
            nuevoUsuario.setApellido("");
            nuevoUsuario.setDni("");
            nuevoUsuario.setTelefono("");
            nuevoUsuario.getRoles().add(rolCliente);
            Usuario usuarioGuardado = usuarioRepositorio.save(nuevoUsuario);

            cliente = new Cliente();
            cliente.setUsuario(usuarioGuardado);
            cliente.setActivo(false);
            clienteRepositorio.save(cliente);
        }

        String tokenStr = java.util.UUID.randomUUID().toString();
        com.veterinaria.modelos.VerificationToken token = new com.veterinaria.modelos.VerificationToken(
            tokenStr,
            cliente,
            java.time.LocalDateTime.now().plusHours(24)
        );
        tokenRepositorio.save(token);

        // Disparamos evento asíncrono para el envío de correo
        eventPublisher.publishEvent(new RegistroCorreoEvent(dto.getEmail(), tokenStr));

        return new MensajeResponseDTO("Se ha enviado un enlace de registro a tu correo");
    }

    @Transactional
    public AuthResponseDTO completarRegistro(CompletarRegistroDTO dto) {
        String email;
        Usuario usuario;
        Cliente cliente;

        if (dto.getToken().length() > 100) {
            GoogleIdToken.Payload payload = googleTokenVerifier.verificarToken(dto.getToken());
            email = payload.getEmail();
            usuario = usuarioRepositorio.findByEmail(email)
                .orElseThrow(() -> new BusinessLogicException("Usuario no encontrado"));
            cliente = usuario.getCliente();
        } else {
            com.veterinaria.modelos.VerificationToken tokenEntity = tokenRepositorio.findByToken(dto.getToken())
                .orElseThrow(() -> new BusinessLogicException("Enlace de registro inválido o expirado"));
            
            if (tokenEntity.getFechaExpiracion().isBefore(java.time.LocalDateTime.now())) {
                throw new BusinessLogicException("El enlace de registro ha expirado");
            }
            cliente = tokenEntity.getCliente();
            usuario = cliente.getUsuario();
            email = usuario.getEmail();
            
            if (dto.getPassword() == null || dto.getPassword().trim().isEmpty()) {
                throw new BusinessLogicException("La contraseña es obligatoria");
            }
            usuario.setPassword(passwordEncoder.encode(dto.getPassword()));
            tokenRepositorio.delete(tokenEntity);
        }

        if (!email.equalsIgnoreCase(dto.getEmail())) {
            throw new BusinessLogicException("El email no coincide con el token");
        }

        java.util.Optional<Usuario> usuarioDni = usuarioRepositorio.findByDni(dto.getDni());
        if (usuarioDni.isPresent() && !usuarioDni.get().getId().equals(usuario.getId())) {
            throw new BusinessLogicException("El DNI ya se encuentra registrado por otro usuario");
        }

        cliente.setActivo(true);
        clienteRepositorio.save(cliente);

        usuario.setNombre(dto.getNombre());
        usuario.setApellido(dto.getApellido());
        usuario.setDni(dto.getDni());
        usuario.setTelefono(dto.getTelefono());
        usuario.setActivo(true);
        usuarioRepositorio.save(usuario);

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

        String tokenJwt = jwtServicio.generarToken(extraClaims, userDetails);
        String refreshToken = refreshTokenServicio.crearRefreshTokenParaUsuario(usuario);

        java.util.List<Long> sedeIds = null;
        if (usuario.getEmpleado() != null) {
            sedeIds = usuario.getEmpleado().getSedes().stream()
                .map(com.veterinaria.modelos.Sede::getId)
                .collect(java.util.stream.Collectors.toList());
        }

        return new AuthResponseDTO(tokenJwt, refreshToken, email, rolesDisponibles, sedeIds, requiresRoleSelection);
    }

    @Transactional
    public MensajeResponseDTO confirmarToken(String tokenStr, String password) {
        com.veterinaria.modelos.VerificationToken token = tokenRepositorio.findByToken(tokenStr)
            .orElseThrow(() -> new BusinessLogicException("Token inválido o no existe"));

        if (token.getFechaExpiracion().isBefore(java.time.LocalDateTime.now())) {
            throw new BusinessLogicException("El enlace de verificación ha expirado");
        }

        if (token.getCliente() != null) {
            Cliente cliente = token.getCliente();
            Usuario usuario = cliente.getUsuario();
            if (usuario == null) {
                usuario = new Usuario();
                usuario.setNombre("");
                usuario.setApellido("");
                usuario.setDni("");
                usuario.setTelefono("");
                Rol rolCliente = rolRespositorio.findByNombre("ROLE_CLIENTE")
                        .orElseThrow(() -> new ResourceNotFoundException("El rol ROLE_CLIENTE no existe en la BD"));
                usuario.getRoles().add(rolCliente);
            }
            
            usuario.setPassword(passwordEncoder.encode(password));
            usuario.setActivo(true);
            usuarioRepositorio.save(usuario);

            cliente.setUsuario(usuario);
            cliente.setActivo(true);
            clienteRepositorio.save(cliente);
        } else if (token.getEmpleado() != null) {
            com.veterinaria.modelos.Empleado empleado = token.getEmpleado();
            Usuario usuario = empleado.getUsuario();
            if (usuario == null) {
                throw new BusinessLogicException("El empleado no tiene un usuario asignado");
            }
            usuario.setPassword(passwordEncoder.encode(password));
            usuario.setActivo(true);
            usuarioRepositorio.save(usuario);

            empleado.setActivo(true);
        }

        tokenRepositorio.delete(token);

        return new MensajeResponseDTO("Cuenta confirmada. Ya puedes iniciar sesión.");
    }
}
