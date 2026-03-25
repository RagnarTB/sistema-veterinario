package com.veterinaria.servicios;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.veterinaria.dtos.PacienteRequestDTO;
import com.veterinaria.dtos.PacienteResponseDTO;
import com.veterinaria.modelos.Cliente;
import com.veterinaria.modelos.Paciente;
import com.veterinaria.respositorios.ClienteRepositorio;
import com.veterinaria.respositorios.PacienteRepositorio;

import jakarta.persistence.EntityNotFoundException;

@Service
public class PacienteServicio {

        private PacienteRepositorio pacienteRepositorio;

        // Agregamos el repositorio de clientes para poder buscar al dueño
        private final ClienteRepositorio clienteRepositorio;

        // Actualizamos el constructor
        public PacienteServicio(PacienteRepositorio pacienteRepositorio, ClienteRepositorio clienteRepositorio) {
                this.pacienteRepositorio = pacienteRepositorio;
                this.clienteRepositorio = clienteRepositorio;
        }

        // Nota cómo cambiamos la firma: Recibe un RequestDTO y devuelve un ResponseDTO
        public PacienteResponseDTO guardar(PacienteRequestDTO dto) {
                // 1. PRIMERO: Buscamos al dueño por su ID
                Cliente dueno = clienteRepositorio.findById(dto.getClienteId())
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                "No se puede crear el paciente. Cliente no encontrado con ID: "
                                                                + dto.getClienteId()));

                // 2. Mapeo de entrada: DTO -> Entidad
                Paciente paciente = new Paciente();
                paciente.setNombre(dto.getNombre());
                paciente.setEspecie(dto.getEspecie());
                paciente.setRaza(dto.getRaza());

                // 3. LA RELACIÓN MÁGICA: Le asignamos el dueño al perrito
                paciente.setCliente(dueno);

                // 4. Guardar en base de datos
                Paciente pacienteGuardado = pacienteRepositorio.save(paciente);

                // 5. Mapeo de salida: Entidad -> DTO
                PacienteResponseDTO respuesta = new PacienteResponseDTO();
                respuesta.setId(pacienteGuardado.getId());
                respuesta.setNombre(pacienteGuardado.getNombre());
                respuesta.setEspecie(pacienteGuardado.getEspecie());
                respuesta.setRaza(pacienteGuardado.getRaza());
                respuesta.setClienteId(dueno.getId());

                return respuesta;
        }

        public List<PacienteResponseDTO> listarTodos() {
                // 1. Obtenemos todas las entidades de la base de datos
                List<Paciente> pacientes = pacienteRepositorio.findAll();
                // 2. Transformamos la lista de Entidades a una lista de DTOs
                return pacientes.stream()
                                .map(paciente -> new PacienteResponseDTO(paciente.getId(), paciente.getNombre(),
                                                paciente.getEspecie(), paciente.getRaza(),
                                                paciente.getCliente().getId()))
                                .toList();
        }

        public PacienteResponseDTO buscarPorId(Long id) {
                return pacienteRepositorio.findById(id)
                                .map(paciente -> new PacienteResponseDTO(
                                                paciente.getId(),
                                                paciente.getNombre(),
                                                paciente.getEspecie(),
                                                paciente.getRaza(),
                                                paciente.getCliente().getId()))
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                "Paciente no encontrado con ID: " + id));
        }

        public PacienteResponseDTO actualizar(Long id, PacienteRequestDTO dto) {
                // 1. PRIMERO BUSCAMOS: Usamos el mismo bloque de código que ya conoces
                // Si no existe, explota con un 404 y no avanza a la siguiente línea.
                Paciente pacienteDb = pacienteRepositorio.findById(id)
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                "Paciente no encontrado con ID: " + id));

                // 2. Buscamos al dueño (¡ESTO FALTABA!)
                Cliente dueno = clienteRepositorio.findById(dto.getClienteId())
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                "Cliente no encontrado con ID: " + dto.getClienteId()));

                // 2. LUEGO MODIFICAMOS: Actualizamos la entidad que sacamos de la base de datos
                // con los datos nuevos que vienen en el DTO "sobre de correo"
                pacienteDb.setNombre(dto.getNombre());
                pacienteDb.setEspecie(dto.getEspecie());
                pacienteDb.setRaza(dto.getRaza());

                // 3. GUARDAMOS: Como la entidad "pacienteDb" ya tiene un ID,
                // el .save() de JPA es lo suficientemente inteligente para saber que
                // debe hacer un UPDATE en lugar de un INSERT.
                Paciente pacienteGuardado = pacienteRepositorio.save(pacienteDb);

                // 4. DEVOLVEMOS EL TICKET: Mapeamos a ResponseDTO
                return new PacienteResponseDTO(
                                pacienteGuardado.getId(),
                                pacienteGuardado.getNombre(),
                                pacienteGuardado.getEspecie(),
                                pacienteGuardado.getRaza(),
                                pacienteGuardado.getCliente().getId());
        }

        public void eliminar(Long id) {
                // 1. Verificamos si existe (reutilizamos la lógica que ya dominas)
                Paciente pacienteDb = pacienteRepositorio.findById(id)
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                "Paciente no encontrado con ID: " + id));

                // 2. Si pasó la línea anterior sin explotar, significa que existe, así que lo
                // borramos
                pacienteRepositorio.delete(pacienteDb);
        }

}
