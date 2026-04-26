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
    private com.veterinaria.respositorios.VerificationTokenRepositorio tokenRepositorio;
    private EmailServicio emailServicio;

    public ClienteServicio(ClienteRepositorio clienteRepositorio, 
                           com.veterinaria.respositorios.VerificationTokenRepositorio tokenRepositorio, 
                           EmailServicio emailServicio) {
        this.clienteRepositorio = clienteRepositorio;
        this.tokenRepositorio = tokenRepositorio;
        this.emailServicio = emailServicio;
    }

    public ClienteResponseDTO guardar(ClienteRequestDTO dto) {

        if (clienteRepositorio.existsByDni(dto.getDni())) {
            throw new RuntimeException("El DNI ya se encuentra registrado");
        }

        Cliente cliente = new Cliente();
        cliente.setNombre(dto.getNombre());
        cliente.setApellido(dto.getApellido());
        cliente.setTelefono(dto.getTelefono());
        cliente.setDni(dto.getDni());
        cliente.setEmail(dto.getEmail());

        // El cliente se crea inactivo hasta que verifique su correo
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
        emailServicio.enviarCorreoConfirmacion(clienteGuardado.getEmail(), token);

        ClienteResponseDTO respuesta = new ClienteResponseDTO();
        respuesta.setId(clienteGuardado.getId());
        respuesta.setNombre(clienteGuardado.getNombre());
        respuesta.setApellido(clienteGuardado.getApellido());
        respuesta.setTelefono(clienteGuardado.getTelefono());
        respuesta.setDni(clienteGuardado.getDni());
        respuesta.setEmail(clienteGuardado.getEmail());
        respuesta.setActivo(clienteGuardado.getActivo());

        respuesta.setActivo(clienteGuardado.getActivo());
        respuesta.setVerificado(false); // First time creation is never verified

        return respuesta;
    }

    public Page<ClienteResponseDTO> listarTodos(String buscar, Pageable pageable) {
        Page<Cliente> pagina;
        if (buscar != null && !buscar.trim().isEmpty()) {
            pagina = clienteRepositorio.findByNombreContainingIgnoreCaseOrApellidoContainingIgnoreCaseOrDniContaining(
                    buscar, buscar, buscar, pageable);
        } else {
            pagina = clienteRepositorio.findAll(pageable);
        }

        return pagina.map(cliente -> {
            boolean verificado = cliente.getUsuario() != null && cliente.getUsuario().getPassword() != null;
            return new ClienteResponseDTO(
                cliente.getId(),
                cliente.getNombre(),
                cliente.getApellido(),
                cliente.getTelefono(),
                cliente.getDni(),
                cliente.getEmail(),
                cliente.getActivo(),
                verificado);
        });
    }

    public ClienteResponseDTO buscarPorId(Long id) {
        return clienteRepositorio.findById(id)
                .map(cliente -> {
                    boolean verificado = cliente.getUsuario() != null && cliente.getUsuario().getPassword() != null;
                    return new ClienteResponseDTO(
                        cliente.getId(),
                        cliente.getNombre(),
                        cliente.getApellido(),
                        cliente.getTelefono(),
                        cliente.getDni(), 
                        cliente.getEmail(), 
                        cliente.getActivo(),
                        verificado);
                })
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Cliente no encontrado con ID: " + id));

    }

    public ClienteResponseDTO actualizar(Long id, ClienteRequestDTO dto) {
        Cliente clientedb = clienteRepositorio.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente no encontrado con ID: " + id));

        clientedb.setNombre(dto.getNombre());
        clientedb.setApellido(dto.getApellido());
        clientedb.setTelefono(dto.getTelefono());
        clientedb.setDni(dto.getDni());
        clientedb.setEmail(dto.getEmail());

        Cliente clienteGuardado = clienteRepositorio.save(clientedb);

        boolean verificado = clienteGuardado.getUsuario() != null && clienteGuardado.getUsuario().getPassword() != null;
        return new ClienteResponseDTO(
                clienteGuardado.getId(),
                clienteGuardado.getNombre(),
                clienteGuardado.getApellido(),
                clienteGuardado.getTelefono(),
                clienteGuardado.getDni(),
                clienteGuardado.getEmail(),
                clienteGuardado.getActivo(),
                verificado
        );
    }

    @Transactional
    public void cambiarEstado(Long id, Boolean estado) {
        Cliente clientedb = clienteRepositorio.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente no encontrado con ID:" + id));

        if (estado) {
            // El admin acaba de darle click a "Activar". Se le envía el correo de confirmación
            // y permanece inactivo hasta que responda al correo configurando su clave.
            clientedb.setActivo(false); 
            if (clientedb.getUsuario() != null) {
                clientedb.getUsuario().setActivo(false);
            }
            // Generar nuevo token y reenviar
            String token = java.util.UUID.randomUUID().toString();
            com.veterinaria.modelos.VerificationToken verificationToken = new com.veterinaria.modelos.VerificationToken(
                    token, 
                    clientedb, 
                    java.time.LocalDateTime.now().plusDays(1)
            );
            tokenRepositorio.save(verificationToken);
            emailServicio.enviarCorreoConfirmacion(clientedb.getEmail(), token);
        } else {
            // Desactivar inmediatamente
            clientedb.setActivo(false);
            if (clientedb.getUsuario() != null) {
                clientedb.getUsuario().setActivo(false);
            }
        }

        if (clientedb.getPacientes() != null) {
            clientedb.getPacientes().forEach(paciente -> paciente.setActivo(estado != null ? estado : false));
        }
        clienteRepositorio.save(clientedb);
    }

}
