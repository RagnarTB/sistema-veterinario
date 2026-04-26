package com.veterinaria.servicios;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.veterinaria.dtos.PacienteRequestDTO;
import com.veterinaria.dtos.PacienteResponseDTO;
import com.veterinaria.modelos.Cliente;
import com.veterinaria.modelos.Especie;
import com.veterinaria.modelos.Paciente;
import com.veterinaria.respositorios.ClienteRepositorio;
import com.veterinaria.respositorios.EspecieRepositorio;
import com.veterinaria.respositorios.PacienteRepositorio;

@Service
public class PacienteServicio {

        private final PacienteRepositorio pacienteRepositorio;
        private final ClienteRepositorio clienteRepositorio;
        private final EspecieRepositorio especieRepositorio; // Declarado

        // 1. CORRECCIÓN: Constructor con los 3 repositorios inyectados
        public PacienteServicio(PacienteRepositorio pacienteRepositorio,
                        ClienteRepositorio clienteRepositorio,
                        EspecieRepositorio especieRepositorio) {
                this.pacienteRepositorio = pacienteRepositorio;
                this.clienteRepositorio = clienteRepositorio;
                this.especieRepositorio = especieRepositorio;
        }

        public PacienteResponseDTO guardar(PacienteRequestDTO dto) {
                Cliente dueno = clienteRepositorio.findById(dto.getClienteId())
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                "Cliente no encontrado con ID: " + dto.getClienteId()));

                Especie especie = especieRepositorio.findById(dto.getEspecieId())
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                "Especie no encontrada con id: " + dto.getEspecieId()));

                Paciente paciente = new Paciente();
                paciente.setNombre(dto.getNombre());
                paciente.setEspecie(especie);
                paciente.setRaza(dto.getRaza());
                paciente.setFechaNacimiento(dto.getFechaNacimiento());
                paciente.setCliente(dueno);

                Paciente pacienteGuardado = pacienteRepositorio.save(paciente);

                // Usamos nuestro método ayudante
                return mapearAResponse(pacienteGuardado);
        }

        public Page<PacienteResponseDTO> listarTodos(String buscar, Pageable pageable) {
                Page<Paciente> pagina;
                if (buscar != null && !buscar.trim().isEmpty()) {
                        pagina = pacienteRepositorio.findByNombreContainingIgnoreCase(buscar, pageable);
                } else {
                        pagina = pacienteRepositorio.findAll(pageable);
                }
                return pagina.map(this::mapearAResponse);
        }

        public PacienteResponseDTO buscarPorId(Long id) {
                return pacienteRepositorio.findById(id)
                                .map(this::mapearAResponse) // Usamos nuestro método ayudante
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                "Paciente no encontrado con ID: " + id));
        }

        public PacienteResponseDTO actualizar(Long id, PacienteRequestDTO dto) {
                Paciente pacienteDb = pacienteRepositorio.findById(id)
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                "Paciente no encontrado con ID: " + id));

                Cliente dueno = clienteRepositorio.findById(dto.getClienteId())
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                "Cliente no encontrado con ID: " + dto.getClienteId()));

                // 2. CORRECCIÓN: Buscamos la Especie en BD antes de actualizar
                Especie especie = especieRepositorio.findById(dto.getEspecieId())
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                "Especie no encontrada con id: " + dto.getEspecieId()));

                pacienteDb.setNombre(dto.getNombre());
                pacienteDb.setEspecie(especie); // Asignamos el objeto Especie
                pacienteDb.setRaza(dto.getRaza());
                pacienteDb.setFechaNacimiento(dto.getFechaNacimiento());
                pacienteDb.setCliente(dueno);

                Paciente pacienteGuardado = pacienteRepositorio.save(pacienteDb);

                return mapearAResponse(pacienteGuardado);
        }

        public void cambiarEstado(Long id, Boolean estado) {
                Paciente pacienteDb = pacienteRepositorio.findById(id)
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                "Paciente no encontrado con ID: " + id));
                pacienteDb.setActivo(estado);
                pacienteRepositorio.save(pacienteDb);
        }

        // --- 3. CORRECCIÓN: EL MÉTODO AYUDANTE (Refactorización Limpia) ---
        private PacienteResponseDTO mapearAResponse(Paciente paciente) {
                return new PacienteResponseDTO(
                                paciente.getId(),
                                paciente.getNombre(),
                                paciente.getEspecie().getNombre(), // Extraemos el nombre de la especie
                                paciente.getRaza(),
                                paciente.getFechaNacimiento(),
                                paciente.getCliente().getId(),
                                paciente.getCliente().getNombre() + " " + paciente.getCliente().getApellido(),
                                paciente.getActivo());
        }
}