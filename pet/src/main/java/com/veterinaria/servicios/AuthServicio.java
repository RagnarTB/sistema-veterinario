package com.veterinaria.servicios;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.veterinaria.dtos.AuthResponseDTO;
import com.veterinaria.dtos.LoginRequestDTO;
import com.veterinaria.dtos.MensajeResponseDTO;
import com.veterinaria.dtos.RegistroClienteDTO;
import com.veterinaria.modelos.Cliente;
import com.veterinaria.modelos.Rol;
import com.veterinaria.modelos.Usuario;
import com.veterinaria.respositorios.ClienteRepositorio;
import com.veterinaria.respositorios.RolRespositorio;
import com.veterinaria.respositorios.UsuarioRepositorio;
import com.veterinaria.seguridad.JwtServicio;

import jakarta.transaction.Transactional;

@Service
public class AuthServicio {

    private UsuarioRepositorio usuarioRepositorio;
    private RolRespositorio rolRespositorio;
    private ClienteRepositorio clienteRepositorio;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtServicio jwtServicio;
    private final org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    public AuthServicio(UsuarioRepositorio usuarioRepositorio, RolRespositorio rolRespositorio,
            ClienteRepositorio clienteRepositorio, PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager, JwtServicio jwtServicio,
            org.springframework.security.core.userdetails.UserDetailsService userDetailsService) {
        this.usuarioRepositorio = usuarioRepositorio;
        this.rolRespositorio = rolRespositorio;
        this.clienteRepositorio = clienteRepositorio;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtServicio = jwtServicio;
        this.userDetailsService = userDetailsService;
    }

    @Transactional // AQUI: Fundamental porque guardamos en 2 tablas
    public MensajeResponseDTO registrarCliente(RegistroClienteDTO dto) {

        // 1. Validar si el email y dni ya existe
        if (usuarioRepositorio.findByEmail(dto.getEmail()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El email ya está registrado");
        }
        if (clienteRepositorio.existsByDni(dto.getDni())) {
            throw new RuntimeException("El DNI ya se encuentra registrado");
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
        // Si la contraseña está mal, esto lanza un error 403 automáticamente
        authenticationManager.authenticate(
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        dto.getEmail(), dto.getPassword()));

        // 2. Si paso, buscamos los datos del usuario (roles, etc.)
        org.springframework.security.core.userdetails.UserDetails userDetails = userDetailsService
                .loadUserByUsername(dto.getEmail());

        // 3. Generamos el Token JWT criptográfico
        String token = jwtServicio.generarToken(userDetails);

        // 4. Devolvemos la llave al Frontend
        return new AuthResponseDTO(token, dto.getEmail());
    }
}