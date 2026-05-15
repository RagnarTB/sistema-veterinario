package com.veterinaria.servicios;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import com.veterinaria.dtos.ClienteRequestDTO;
import com.veterinaria.dtos.ClienteResponseDTO;
import com.veterinaria.modelos.Cliente;
import com.veterinaria.respositorios.ClienteRepositorio;

import jakarta.transaction.Transactional;

@Service
public class ClienteServicio {

    private ClienteRepositorio clienteRepositorio;
    private com.veterinaria.respositorios.UsuarioRepositorio usuarioRepositorio;
    private com.veterinaria.respositorios.RolRespositorio rolRespositorio;
    private com.veterinaria.respositorios.VerificationTokenRepositorio tokenRepositorio;
    private EmailServicio emailServicio;

    public ClienteServicio(ClienteRepositorio clienteRepositorio, 
                           com.veterinaria.respositorios.UsuarioRepositorio usuarioRepositorio,
                           com.veterinaria.respositorios.RolRespositorio rolRespositorio,
                           com.veterinaria.respositorios.VerificationTokenRepositorio tokenRepositorio, 
                           EmailServicio emailServicio) {
        this.clienteRepositorio = clienteRepositorio;
        this.usuarioRepositorio = usuarioRepositorio;
        this.rolRespositorio = rolRespositorio;
        this.tokenRepositorio = tokenRepositorio;
        this.emailServicio = emailServicio;
    }

    @Transactional
    public ClienteResponseDTO guardar(ClienteRequestDTO dto) {

        com.veterinaria.modelos.Usuario usuario = usuarioRepositorio.findByDni(dto.getDni()).orElse(null);
        if (usuario != null && usuario.getCliente() != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El DNI proporcionado ya está registrado como cliente.");
        }

        if (usuario == null) {
            usuario = new com.veterinaria.modelos.Usuario();
            usuario.setNombre(dto.getNombre());
            usuario.setApellido(dto.getApellido());
            usuario.setTelefono(dto.getTelefono());
            usuario.setDni(dto.getDni());
            usuario.setEmail(dto.getEmail());
            usuario.setActivo(false);
            
            com.veterinaria.modelos.Rol rolCliente = rolRespositorio.findByNombre("ROLE_CLIENTE")
                    .orElseThrow(() -> new RuntimeException("Rol ROLE_CLIENTE no encontrado"));
            usuario.getRoles().add(rolCliente);
            usuario = usuarioRepositorio.save(usuario);
        }

        Cliente cliente = new Cliente();
        cliente.setUsuario(usuario);
        cliente.setActivo(false);
        cliente.setEsInvitado(false);

        Cliente clienteGuardado = clienteRepositorio.save(cliente);

        // Generar Token de Verificacion
        String token = java.util.UUID.randomUUID().toString();
        com.veterinaria.modelos.VerificationToken verificationToken = new com.veterinaria.modelos.VerificationToken(
                token, 
                clienteGuardado, 
                java.time.LocalDateTime.now().plusDays(1)
        );
        tokenRepositorio.save(verificationToken);

        // Enviar Email
        emailServicio.enviarCorreoConfirmacion(usuario.getEmail(), token);

        ClienteResponseDTO respuesta = new ClienteResponseDTO();
        respuesta.setId(clienteGuardado.getId());
        respuesta.setNombre(usuario.getNombre());
        respuesta.setApellido(usuario.getApellido());
        respuesta.setTelefono(usuario.getTelefono());
        respuesta.setDni(usuario.getDni());
        respuesta.setEmail(usuario.getEmail());
        respuesta.setActivo(clienteGuardado.getActivo());
        respuesta.setVerificado(false); // First time creation is never verified

        return respuesta;
    }

    public Page<ClienteResponseDTO> listarTodos(String buscar, Boolean estado, Pageable pageable) {
        Page<Cliente> pagina;
        if (buscar != null && !buscar.trim().isEmpty()) {
            pagina = clienteRepositorio.buscarClientesConRol(buscar, estado, pageable);
        } else {
            pagina = clienteRepositorio.findAllConRol(estado, pageable);
        }

        return pagina.map(cliente -> {
            boolean verificado = cliente.getUsuario() != null && cliente.getUsuario().getPassword() != null;
            com.veterinaria.modelos.Usuario u = cliente.getUsuario();
            return new ClienteResponseDTO(
                cliente.getId(),
                u != null ? u.getNombre() : "",
                u != null ? u.getApellido() : "",
                u != null ? u.getTelefono() : "",
                u != null ? u.getDni() : "",
                u != null ? u.getEmail() : "",
                cliente.getActivo(),
                verificado);
        });
    }

    public ClienteResponseDTO buscarPorId(Long id) {
        return clienteRepositorio.findById(id)
                .map(cliente -> {
                    boolean verificado = cliente.getUsuario() != null && cliente.getUsuario().getPassword() != null;
                    com.veterinaria.modelos.Usuario u = cliente.getUsuario();
                    return new ClienteResponseDTO(
                        cliente.getId(),
                        u != null ? u.getNombre() : "",
                        u != null ? u.getApellido() : "",
                        u != null ? u.getTelefono() : "",
                        u != null ? u.getDni() : "",
                        u != null ? u.getEmail() : "",
                        cliente.getActivo(),
                        verificado);
                })
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Cliente no encontrado con ID: " + id));

    }

    @Transactional
    public ClienteResponseDTO actualizar(Long id, ClienteRequestDTO dto) {
        Cliente clientedb = clienteRepositorio.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente no encontrado con ID: " + id));

        com.veterinaria.modelos.Usuario u = clientedb.getUsuario();
        if (u != null) {
            u.setNombre(dto.getNombre());
            u.setApellido(dto.getApellido());
            u.setTelefono(dto.getTelefono());
            u.setDni(dto.getDni());
            u.setEmail(dto.getEmail());
            usuarioRepositorio.save(u);
        }

        Cliente clienteGuardado = clienteRepositorio.save(clientedb);

        boolean verificado = clienteGuardado.getUsuario() != null && clienteGuardado.getUsuario().getPassword() != null;
        return new ClienteResponseDTO(
                clienteGuardado.getId(),
                u != null ? u.getNombre() : "",
                u != null ? u.getApellido() : "",
                u != null ? u.getTelefono() : "",
                u != null ? u.getDni() : "",
                u != null ? u.getEmail() : "",
                clienteGuardado.getActivo(),
                verificado
        );
    }

    @Transactional
    public void cambiarEstado(Long id, Boolean estado) {
        Cliente clientedb = clienteRepositorio.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente no encontrado con ID:" + id));

        if (estado) {
            // El admin acaba de darle click a "Activar".
            clientedb.setActivo(false); 
            // Si el usuario no tiene contraseña (nuevo), enviamos correo
            if (clientedb.getUsuario() != null && clientedb.getUsuario().getPassword() == null) {
                // Generar nuevo token y reenviar
                String token = java.util.UUID.randomUUID().toString();
                com.veterinaria.modelos.VerificationToken verificationToken = new com.veterinaria.modelos.VerificationToken(
                        token, 
                        clientedb, 
                        java.time.LocalDateTime.now().plusDays(1)
                );
                tokenRepositorio.save(verificationToken);
                String correo = clientedb.getUsuario().getEmail();
                if (correo != null) {
                    emailServicio.enviarCorreoConfirmacion(correo, token);
                }
            } else {
                clientedb.setActivo(true);
            }
        } else {
            // Desactivar inmediatamente el rol de cliente
            clientedb.setActivo(false);
            // Solo desactivamos el LOGIN si NO es empleado
            if (clientedb.getUsuario() != null && clientedb.getUsuario().getEmpleado() == null) {
                clientedb.getUsuario().setActivo(false);
                usuarioRepositorio.save(clientedb.getUsuario());
            }
        }

        if (clientedb.getPacientes() != null) {
            clientedb.getPacientes().forEach(paciente -> paciente.setActivo(estado != null ? estado : false));
        }
        clienteRepositorio.save(clientedb);
    }

}
