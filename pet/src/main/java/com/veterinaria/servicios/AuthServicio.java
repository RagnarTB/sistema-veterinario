package com.veterinaria.servicios;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.veterinaria.dtos.AuthResponseDTO;
import com.veterinaria.dtos.CambiarPasswordRequestDTO;
import com.veterinaria.dtos.LoginRequestDTO;
import com.veterinaria.dtos.MensajeResponseDTO;
import com.veterinaria.dtos.RegistroClienteDTO;
import com.veterinaria.modelos.Cliente;
import com.veterinaria.modelos.Rol;
import com.veterinaria.modelos.Usuario;
import com.veterinaria.respositorios.ClienteRepositorio;
import com.veterinaria.respositorios.RolRespositorio;
import com.veterinaria.respositorios.UsuarioRepositorio;
import com.veterinaria.respositorios.TokenPreRegistroRepositorio;
import com.veterinaria.seguridad.JwtServicio;
import com.veterinaria.dtos.GoogleLoginRequestDTO;
import com.veterinaria.dtos.SolicitarRegistroCorreoDTO;
import com.veterinaria.dtos.CompletarRegistroDTO;
import com.veterinaria.modelos.TokenPreRegistro;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;

import jakarta.transaction.Transactional;

@Service
public class AuthServicio {

    private UsuarioRepositorio usuarioRepositorio;
    private RolRespositorio rolRespositorio;
    private ClienteRepositorio clienteRepositorio;
    private final RefreshTokenServicio refreshTokenServicio;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtServicio jwtServicio;
    private final org.springframework.security.core.userdetails.UserDetailsService userDetailsService;
    private final com.veterinaria.respositorios.VerificationTokenRepositorio tokenRepositorio;
    private final TokenPreRegistroRepositorio tokenPreRegistroRepositorio;
    private final GoogleTokenVerifierServicio googleTokenVerifier;
    private final EmailServicio emailServicio;

    public AuthServicio(UsuarioRepositorio usuarioRepositorio, RolRespositorio rolRespositorio,
            ClienteRepositorio clienteRepositorio, PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager, JwtServicio jwtServicio,
            org.springframework.security.core.userdetails.UserDetailsService userDetailsService,
            RefreshTokenServicio refreshTokenServicio,
            com.veterinaria.respositorios.VerificationTokenRepositorio tokenRepositorio,
            TokenPreRegistroRepositorio tokenPreRegistroRepositorio,
            GoogleTokenVerifierServicio googleTokenVerifier,
            EmailServicio emailServicio) {
        this.usuarioRepositorio = usuarioRepositorio;
        this.rolRespositorio = rolRespositorio;
        this.clienteRepositorio = clienteRepositorio;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtServicio = jwtServicio;
        this.userDetailsService = userDetailsService;
        this.refreshTokenServicio = refreshTokenServicio;
        this.tokenRepositorio = tokenRepositorio;
        this.tokenPreRegistroRepositorio = tokenPreRegistroRepositorio;
        this.googleTokenVerifier = googleTokenVerifier;
        this.emailServicio = emailServicio;
    }

    @Transactional // AQUI: Fundamental porque guardamos en 2 tablas
    public MensajeResponseDTO registrarCliente(RegistroClienteDTO dto) {

        // 1. Validar si el email y dni ya existe
        if (usuarioRepositorio.findByEmail(dto.getEmail()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El email ya está registrado");
        }
        if (clienteRepositorio.existsByDni(dto.getDni())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El DNI ya se encuentra registrado");
        }

        // 2. Buscar el Rol (Exigencia de Spring Security: empezar con ROLE_)
        Rol rolCliente = rolRespositorio.findByNombre("ROLE_CLIENTE")
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "El rol ROLE_CLIENTE no existe en la BD"));

        // 3. Crear el Usuario
        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setEmail(dto.getEmail());
        nuevoUsuario.setPassword(passwordEncoder.encode(dto.getPassword())); // ya encriptada
        nuevoUsuario.getRoles().add(rolCliente); // AQUI: Le asignamos el rol encontrado

        Usuario usuarioGuardado = usuarioRepositorio.save(nuevoUsuario);

        // 4. Crear la ENTIDAD Cliente (no un DTO) y vincularla
        Cliente nuevoCliente = new Cliente();
        nuevoCliente.setNombre(dto.getNombre());
        nuevoCliente.setApellido(dto.getApellido());
        nuevoCliente.setDni(dto.getDni());
        nuevoCliente.setTelefono(dto.getTelefono());
        nuevoCliente.setEmail(dto.getEmail());
        nuevoCliente.setUsuario(usuarioGuardado); // AQUI: Magia relacional

        clienteRepositorio.save(nuevoCliente); // AQUI: Faltaba guardarlo

        return new MensajeResponseDTO("Cliente registrado con éxito");
    }

    public AuthResponseDTO login(LoginRequestDTO dto) {
        // 1. El Manager verifica si el email y la contraseña son correctos
        authenticationManager.authenticate(
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        dto.getEmail(), dto.getPassword()));

        // 2. Si paso, buscamos los datos del usuario (roles, etc.)
        org.springframework.security.core.userdetails.UserDetails userDetails = userDetailsService
                .loadUserByUsername(dto.getEmail());

        // 3. Embebemos los roles como claim en el JWT para que el frontend los lea
        java.util.List<String> roles = userDetails.getAuthorities().stream()
                .map(auth -> auth.getAuthority())
                .collect(java.util.stream.Collectors.toList());

        java.util.Map<String, Object> extraClaims = new java.util.HashMap<>();
        extraClaims.put("roles", roles);

        String token = jwtServicio.generarToken(extraClaims, userDetails);

        Usuario usuario = usuarioRepositorio.findByEmail(dto.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no válido"));

        String refreshToken = refreshTokenServicio.crearRefreshTokenParaUsuario(usuario);

        // 4. Devolvemos la llave al Frontend (incluimos roles para que el sidebar los use)
        return new AuthResponseDTO(token, refreshToken, dto.getEmail(), roles);
    }

    @Transactional
    public MensajeResponseDTO cambiarPassword(String emailUsuarioLogueado, CambiarPasswordRequestDTO dto) {
        Usuario usuario = usuarioRepositorio.findByEmail(emailUsuarioLogueado)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        // Validar que la contraseña actual ingresada coincida con la de la BD
        if (!passwordEncoder.matches(dto.getPasswordActual(), usuario.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La contraseña actual es incorrecta");
        }

        // Validar que la nueva no sea igual a la antigua (opcional pero recomendado)
        if (passwordEncoder.matches(dto.getPasswordNueva(), usuario.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La nueva contraseña no puede ser igual a la anterior");
        }

        // Cifrar y guardar
        usuario.setPassword(passwordEncoder.encode(dto.getPasswordNueva()));
        usuarioRepositorio.save(usuario);

        // Al cambiar password, revocamos todos los refresh tokens del usuario
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
        return new AuthResponseDTO(token, refreshTokenNuevo, usuario.getEmail(), roles);
    }

    @Transactional
    public MensajeResponseDTO logout(String refreshTokenRaw) {
        refreshTokenServicio.revocarRefreshToken(refreshTokenRaw);
        return new MensajeResponseDTO("Sesión cerrada correctamente");
    }

    @Transactional
    public MensajeResponseDTO confirmarToken(String tokenStr, String password) {
        com.veterinaria.modelos.VerificationToken token = tokenRepositorio.findByToken(tokenStr)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token inválido o no existe"));

        if (token.getFechaExpiracion().isBefore(java.time.LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El enlace de verificación ha expirado");
        }

        if (token.getCliente() != null) {
            Cliente cliente = token.getCliente();

            Usuario usuario = cliente.getUsuario();
            if (usuario == null) {
                usuario = new Usuario();
                usuario.setEmail(cliente.getEmail());
                Rol rolCliente = rolRespositorio.findByNombre("ROLE_CLIENTE")
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "El rol ROLE_CLIENTE no existe en la BD"));
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
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "El empleado no tiene un usuario asignado");
            }
            usuario.setPassword(passwordEncoder.encode(password));
            usuario.setActivo(true);
            usuarioRepositorio.save(usuario);

            empleado.setActivo(true);
            // El repositorio de empleado no está inyectado aquí, pero Cascade o guardar al usuario no guarda al empleado.
            // Para simplicidad, podemos usar un flush o confiar en que EmpleadoServicio lo maneje.
            // Afortunadamente, tenemos el objeto. Pero AuthServicio no tiene empleadoRepositorio.
            // Opcion 1: Inyectar EmpleadoRepositorio en AuthServicio.
            // Opcion 2: Solo modificar y confiar en el Transactional para que hibernate lo guarde.
            // Al estar en @Transactional, los cambios a entidades administradas se guardarán al hacer commit!
        }

        tokenRepositorio.delete(token);

        return new MensajeResponseDTO("Cuenta confirmada. Ya puedes iniciar sesión.");
    }

    public Object loginConGoogle(GoogleLoginRequestDTO dto) {
        GoogleIdToken.Payload payload = googleTokenVerifier.verificarToken(dto.getIdToken());
        String email = payload.getEmail();
        Boolean emailVerified = payload.getEmailVerified();

        if (emailVerified == null || !emailVerified) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "El email de Google no está verificado");
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
                java.util.List<String> roles = userDetails.getAuthorities().stream()
                        .map(auth -> auth.getAuthority())
                        .collect(java.util.stream.Collectors.toList());

                java.util.Map<String, Object> extraClaims = new java.util.HashMap<>();
                extraClaims.put("roles", roles);

                String token = jwtServicio.generarToken(extraClaims, userDetails);
                String refreshToken = refreshTokenServicio.crearRefreshTokenParaUsuario(usuario);

                return new AuthResponseDTO(token, refreshToken, email, roles);
            } else {
                // Usuario existe pero inactivo (Falta Completar Registro)
                Cliente c = usuario.getCliente();
                if (c != null && c.getNombre() == null) {
                    c.setNombre((String) payload.get("given_name"));
                    c.setApellido((String) payload.get("family_name"));
                    clienteRepositorio.save(c);
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
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "El rol ROLE_CLIENTE no existe"));
                
            Usuario nuevoUsuario = new Usuario();
            nuevoUsuario.setEmail(email);
            nuevoUsuario.setPassword(passwordEncoder.encode(java.util.UUID.randomUUID().toString()));
            nuevoUsuario.setGoogleSubject(payload.getSubject());
            nuevoUsuario.setGoogleVinculado(true);
            nuevoUsuario.setActivo(false);
            nuevoUsuario.getRoles().add(rolCliente);
            usuarioRepositorio.save(nuevoUsuario);

            Cliente nuevoCliente = new Cliente();
            nuevoCliente.setEmail(email);
            nuevoCliente.setNombre((String) payload.get("given_name"));
            nuevoCliente.setApellido((String) payload.get("family_name"));
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

    @Transactional
    public MensajeResponseDTO solicitarRegistroCorreo(SolicitarRegistroCorreoDTO dto) {
        java.util.Optional<Usuario> usuarioOpt = usuarioRepositorio.findByEmail(dto.getEmail());
        
        Cliente cliente;
        if (usuarioOpt.isPresent()) {
            Usuario u = usuarioOpt.get();
            if (u.getActivo()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El email ya está registrado y activo");
            } else {
                cliente = u.getCliente();
                if (cliente == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error: Cliente no encontrado");
            }
        } else {
            // Crear usuario inactivo
            Rol rolCliente = rolRespositorio.findByNombre("ROLE_CLIENTE")
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "El rol ROLE_CLIENTE no existe"));
                    
            Usuario nuevoUsuario = new Usuario();
            nuevoUsuario.setEmail(dto.getEmail());
            nuevoUsuario.setPassword(passwordEncoder.encode(java.util.UUID.randomUUID().toString()));
            nuevoUsuario.setActivo(false);
            nuevoUsuario.getRoles().add(rolCliente);
            Usuario usuarioGuardado = usuarioRepositorio.save(nuevoUsuario);

            // Crear cliente inactivo
            cliente = new Cliente();
            cliente.setEmail(dto.getEmail());
            cliente.setUsuario(usuarioGuardado);
            cliente.setActivo(false);
            clienteRepositorio.save(cliente);
        }

        // Generar token de verificación real (ligado al cliente)
        String tokenStr = java.util.UUID.randomUUID().toString();
        com.veterinaria.modelos.VerificationToken token = new com.veterinaria.modelos.VerificationToken(
            tokenStr,
            cliente,
            java.time.LocalDateTime.now().plusHours(24)
        );
        tokenRepositorio.save(token);

        emailServicio.enviarCorreoRegistroCliente(dto.getEmail(), tokenStr);

        return new MensajeResponseDTO("Se ha enviado un enlace de registro a tu correo");
    }

    @Transactional
    public AuthResponseDTO completarRegistro(CompletarRegistroDTO dto) {
        String email;
        Usuario usuario;
        Cliente cliente;

        // Si el token parece ser un JWT de Google (largo), validarlo
        if (dto.getToken().length() > 100) {
            GoogleIdToken.Payload payload = googleTokenVerifier.verificarToken(dto.getToken());
            email = payload.getEmail();
            usuario = usuarioRepositorio.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Usuario no encontrado"));
            cliente = usuario.getCliente();
        } else {
            // Es token de correo
            com.veterinaria.modelos.VerificationToken tokenEntity = tokenRepositorio.findByToken(dto.getToken())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Enlace de registro inválido o expirado"));
            
            if (tokenEntity.getFechaExpiracion().isBefore(java.time.LocalDateTime.now())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El enlace de registro ha expirado");
            }
            cliente = tokenEntity.getCliente();
            usuario = cliente.getUsuario();
            email = usuario.getEmail();
            
            if (dto.getPassword() == null || dto.getPassword().trim().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La contraseña es obligatoria");
            }
            usuario.setPassword(passwordEncoder.encode(dto.getPassword()));
            tokenRepositorio.delete(tokenEntity);
        }

        if (!email.equalsIgnoreCase(dto.getEmail())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El email no coincide con el token");
        }

        // Validar si el DNI que intentan guardar YA pertenece a OTRO cliente
        java.util.Optional<Cliente> clienteDni = clienteRepositorio.findByDni(dto.getDni());
        if (clienteDni.isPresent() && !clienteDni.get().getId().equals(cliente.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El DNI ya se encuentra registrado por otro usuario");
        }

        // Actualizar el cliente
        cliente.setNombre(dto.getNombre());
        cliente.setApellido(dto.getApellido());
        cliente.setDni(dto.getDni());
        cliente.setTelefono(dto.getTelefono());
        cliente.setActivo(true);
        clienteRepositorio.save(cliente);

        usuario.setActivo(true);
        usuarioRepositorio.save(usuario);

        // Generar JWT
        org.springframework.security.core.userdetails.UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        java.util.List<String> roles = userDetails.getAuthorities().stream()
                .map(auth -> auth.getAuthority())
                .collect(java.util.stream.Collectors.toList());

        java.util.Map<String, Object> extraClaims = new java.util.HashMap<>();
        extraClaims.put("roles", roles);

        String tokenJwt = jwtServicio.generarToken(extraClaims, userDetails);
        String refreshToken = refreshTokenServicio.crearRefreshTokenParaUsuario(usuario);

        return new AuthResponseDTO(tokenJwt, refreshToken, email, roles);
    }
}