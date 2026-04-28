package com.veterinaria.servicios;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.veterinaria.dtos.EmpleadoRequestDTO;
import com.veterinaria.dtos.EmpleadoResponseDTO;
import com.veterinaria.modelos.Empleado;
import com.veterinaria.modelos.Rol;
import com.veterinaria.modelos.Usuario;
import com.veterinaria.modelos.Sede;
import com.veterinaria.modelos.VerificationToken;
import com.veterinaria.respositorios.EmpleadoRepositorio;
import com.veterinaria.respositorios.RolRespositorio;
import com.veterinaria.respositorios.UsuarioRepositorio;
import com.veterinaria.respositorios.VerificationTokenRepositorio;
import com.veterinaria.respositorios.SedeRepositorio;

@Service
public class EmpleadoServicio {

    private final EmpleadoRepositorio empleadoRepositorio;
    private final UsuarioRepositorio usuarioRepositorio;
    private final RolRespositorio rolRepositorio;
    private final PasswordEncoder passwordEncoder;
    private final SedeRepositorio sedeRepositorio;
    private final VerificationTokenRepositorio tokenRepositorio;
    private final EmailServicio emailServicio;

    public EmpleadoServicio(EmpleadoRepositorio empleadoRepositorio, UsuarioRepositorio usuarioRepositorio,
            RolRespositorio rolRepositorio, PasswordEncoder passwordEncoder, SedeRepositorio sedeRepositorio,
            VerificationTokenRepositorio tokenRepositorio, EmailServicio emailServicio) {
        this.empleadoRepositorio = empleadoRepositorio;
        this.usuarioRepositorio = usuarioRepositorio;
        this.rolRepositorio = rolRepositorio;
        this.passwordEncoder = passwordEncoder;
        this.sedeRepositorio = sedeRepositorio;
        this.tokenRepositorio = tokenRepositorio;
        this.emailServicio = emailServicio;
    }

    @Transactional
    public EmpleadoResponseDTO guardar(EmpleadoRequestDTO dto) {
        if (empleadoRepositorio.existsByDni(dto.getDni())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ya existe un empleado con este DNI");
        }

        Usuario usuarioGuardado;
        Optional<Usuario> usuarioOpt = usuarioRepositorio.findByEmail(dto.getEmail());
        if (usuarioOpt.isPresent()) {
            Usuario u = usuarioOpt.get();
            if (u.getEmpleado() != null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ya existe un empleado con este email");
            }
            // Si el usuario existe (es cliente), solo se agregan roles
            for (String nombreRol : dto.getRoles()) {
                Rol rol = rolRepositorio.findByNombre(nombreRol)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rol no encontrado: " + nombreRol));
                u.getRoles().add(rol);
            }
            usuarioGuardado = usuarioRepositorio.save(u);
        } else {
            // Usuario nuevo
            Usuario usuario = new Usuario();
            usuario.setEmail(dto.getEmail());
            // Contraseña inútil hasta que lo active
            usuario.setPassword(passwordEncoder.encode(java.util.UUID.randomUUID().toString()));
            usuario.setActivo(false); 
            
            Set<Rol> rolesAsignados = new HashSet<>();
            for (String nombreRol : dto.getRoles()) {
                Rol rol = rolRepositorio.findByNombre(nombreRol)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rol no encontrado: " + nombreRol));
                rolesAsignados.add(rol);
            }
            usuario.setRoles(rolesAsignados);
            usuarioGuardado = usuarioRepositorio.save(usuario);
        }

        List<Sede> sedesLista = sedeRepositorio.findAllById(dto.getSedeIds());
        if (sedesLista.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Sedes no encontradas");
        }
        Set<Sede> sedes = new HashSet<>(sedesLista);

        Empleado empleado = new Empleado();
        empleado.setNombre(dto.getNombre());
        empleado.setApellido(dto.getApellido());
        empleado.setDni(dto.getDni());
        empleado.setTelefono(dto.getTelefono());
        empleado.setEspecialidad(dto.getEspecialidad());
        empleado.setSueldoBase(dto.getSueldoBase());
        empleado.setSedes(sedes);
        empleado.setUsuario(usuarioGuardado);
        
        empleado.setActivo(usuarioGuardado.getActivo());

        Empleado empleadoGuardado = empleadoRepositorio.save(empleado);

        if (!usuarioGuardado.getActivo()) {
            String tokenStr = java.util.UUID.randomUUID().toString();
            VerificationToken verificationToken = new VerificationToken(
                    tokenStr, 
                    empleadoGuardado, 
                    java.time.LocalDateTime.now().plusDays(1)
            );
            tokenRepositorio.save(verificationToken);
            emailServicio.enviarCorreoConfirmacion(usuarioGuardado.getEmail(), tokenStr);
        }

        return mapearAResponse(empleadoGuardado);
    }

    public Page<EmpleadoResponseDTO> listarTodos(String buscar, Pageable pageable) {
        Page<Empleado> pagina;
        if (buscar != null && !buscar.trim().isEmpty()) {
            pagina = empleadoRepositorio.findByNombreContainingIgnoreCaseOrApellidoContainingIgnoreCaseOrDniContaining(
                    buscar, buscar, buscar, pageable);
        } else {
            pagina = empleadoRepositorio.findAll(pageable);
        }
        return pagina.map(this::mapearAResponse);
    }

    public EmpleadoResponseDTO buscarPorId(Long id) {
        return empleadoRepositorio.findById(id)
                .map(this::mapearAResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Empleado no encontrado con ID: " + id));
    }

    @Transactional
    public EmpleadoResponseDTO actualizar(Long id, EmpleadoRequestDTO dto) {
        Empleado empleado = empleadoRepositorio.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Empleado no encontrado con ID: " + id));

        if (!empleado.getDni().equals(dto.getDni()) && empleadoRepositorio.existsByDni(dto.getDni())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ya existe otro empleado con este DNI");
        }

        Usuario usuario = empleado.getUsuario();
        if (!usuario.getEmail().equals(dto.getEmail()) && usuarioRepositorio.findByEmail(dto.getEmail()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ya existe otro usuario con este email");
        }

        usuario.setEmail(dto.getEmail());
        Set<Rol> rolesAsignados = new HashSet<>();
        for (String nombreRol : dto.getRoles()) {
            Rol rol = rolRepositorio.findByNombre(nombreRol)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rol no encontrado: " + nombreRol));
            rolesAsignados.add(rol);
        }
        usuario.setRoles(rolesAsignados);
        usuarioRepositorio.save(usuario);

        List<Sede> sedesLista = sedeRepositorio.findAllById(dto.getSedeIds());
        if (sedesLista.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Sedes no encontradas");
        }
        Set<Sede> sedes = new HashSet<>(sedesLista);

        empleado.setNombre(dto.getNombre());
        empleado.setApellido(dto.getApellido());
        empleado.setDni(dto.getDni());
        empleado.setTelefono(dto.getTelefono());
        empleado.setEspecialidad(dto.getEspecialidad());
        empleado.setSueldoBase(dto.getSueldoBase());
        empleado.setSedes(sedes);

        Empleado empleadoGuardado = empleadoRepositorio.save(empleado);
        return mapearAResponse(empleadoGuardado);
    }

    @Transactional
    public void cambiarEstado(Long id, Boolean estado) {
        Empleado empleado = empleadoRepositorio.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Empleado no encontrado con ID: " + id));
        
        Usuario usuario = empleado.getUsuario();

        if (estado) {
            // El admin acaba de darle click a "Activar"
            if (!usuario.getActivo() && usuario.getPassword() != null && usuario.getEmpleado() != null && usuario.getCliente() == null) {
                // Generar nuevo token y reenviar si la contraseña era la dummy
                String tokenStr = java.util.UUID.randomUUID().toString();
                VerificationToken verificationToken = new VerificationToken(
                        tokenStr, 
                        empleado, 
                        java.time.LocalDateTime.now().plusDays(1)
                );
                tokenRepositorio.save(verificationToken);
                emailServicio.enviarCorreoConfirmacion(usuario.getEmail(), tokenStr);
                // Permanece inactivo hasta que configure la contraseña
                empleado.setActivo(false);
            } else {
                empleado.setActivo(true);
            }
        } else {
            empleado.setActivo(false);
        }
        empleadoRepositorio.save(empleado);
        
        // Bloquear usuario solo si no es cliente. Opcion B recomendada: 
        // Si el empleado se desactiva pero es cliente, NO bloqueamos la cuenta.
        if (usuario != null && usuario.getCliente() == null) {
            usuario.setActivo(estado);
            usuarioRepositorio.save(usuario);
        }
    }

    private EmpleadoResponseDTO mapearAResponse(Empleado empleado) {
        Set<String> rolesNombres = empleado.getUsuario().getRoles().stream()
                .map(Rol::getNombre)
                .collect(Collectors.toSet());

        Set<Long> sedeIds = empleado.getSedes().stream().map(Sede::getId).collect(Collectors.toSet());
        Set<String> sedeNombres = empleado.getSedes().stream().map(Sede::getNombre).collect(Collectors.toSet());

        return new EmpleadoResponseDTO(
                empleado.getId(),
                empleado.getUsuario().getId(),
                empleado.getUsuario().getEmail(),
                rolesNombres,
                empleado.getNombre(),
                empleado.getApellido(),
                empleado.getDni(),
                empleado.getTelefono(),
                empleado.getEspecialidad(),
                empleado.getSueldoBase(),
                empleado.getActivo(),
                sedeIds,
                sedeNombres
        );
    }
}
