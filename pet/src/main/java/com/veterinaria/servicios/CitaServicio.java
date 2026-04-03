package com.veterinaria.servicios;

import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.veterinaria.dtos.CitaRequestDTO;
import com.veterinaria.dtos.CitaResponseDTO;
import com.veterinaria.modelos.Cita;
import com.veterinaria.modelos.Paciente;
import com.veterinaria.modelos.ServicioMedico;
import com.veterinaria.modelos.Usuario;
import com.veterinaria.modelos.Enums.EstadoCita;
import com.veterinaria.respositorios.CitaRepositorio;
import com.veterinaria.respositorios.PacienteRepositorio;
import com.veterinaria.respositorios.ServicioMedicoRepositorio;
import com.veterinaria.respositorios.UsuarioRepositorio;

@Service
public class CitaServicio {

        private final CitaRepositorio citaRepositorio;
        private final PacienteRepositorio pacienteRepositorio;
        private final ServicioMedicoRepositorio servicioRepositorio;
        private final UsuarioRepositorio usuarioRepositorio;
        private final List<EstadoCita> ESTADOS_IGNORADOS = List.of(EstadoCita.CANCELADA, EstadoCita.NO_ASISTIO);

        public CitaServicio(CitaRepositorio citaRepositorio, PacienteRepositorio pacienteRepositorio,
                        ServicioMedicoRepositorio servicioRepositorio, UsuarioRepositorio usuarioRepositorio) {
                this.citaRepositorio = citaRepositorio;
                this.pacienteRepositorio = pacienteRepositorio;
                this.servicioRepositorio = servicioRepositorio;
                this.usuarioRepositorio = usuarioRepositorio;
        }

        public CitaResponseDTO guardar(CitaRequestDTO dto) {
                ServicioMedico servicio = servicioRepositorio.findById(dto.getServicioId())
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                "Servicio no encontrado"));

                Usuario veterinario = usuarioRepositorio.findById(dto.getVeterinarioId())
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                "Veterinario no encontrado"));

                List<Paciente> pacientes = pacienteRepositorio.findAllById(dto.getPacienteIds());
                if (pacientes.isEmpty()) {
                        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encontraron los pacientes");
                }

                int tiempoTotalOcupado = servicio.getDuracionMinutos() + servicio.getBufferMinutos();
                LocalTime horaFinCalculada = dto.getHoraInicio().plusMinutes(tiempoTotalOcupado);

                // Pasamos -1L porque al ser una cita NUEVA, no hay ningún ID real que ignorar
                boolean existeCruce = citaRepositorio.existeCruceDeHorario(
                                veterinario.getId(),
                                dto.getFecha(),
                                dto.getHoraInicio(),
                                horaFinCalculada,
                                -1L,
                                ESTADOS_IGNORADOS);

                if (existeCruce) {
                        throw new ResponseStatusException(HttpStatus.CONFLICT,
                                        "El veterinario ya tiene una cita ocupando este horario.");
                }

                Cita cita = new Cita();
                cita.setEstado(EstadoCita.AGENDADA);
                cita.setFecha(dto.getFecha());
                cita.setHoraInicio(dto.getHoraInicio());
                cita.setHoraFin(horaFinCalculada);
                cita.setMotivo(dto.getMotivo());
                cita.setServicio(servicio);
                cita.setVeterinario(veterinario);
                cita.setPacientes(pacientes);

                Cita citaGuardada = citaRepositorio.save(cita);
                return mapearAResponse(citaGuardada);
        }

        public List<CitaResponseDTO> listar() {
                return citaRepositorio.findAll().stream().map(this::mapearAResponse).collect(Collectors.toList());
        }

        public CitaResponseDTO buscarPorId(Long id) {
                return citaRepositorio.findById(id)
                                .map(this::mapearAResponse)
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                "Cita no encontrada con ID: " + id));
        }

        public CitaResponseDTO actualizar(Long id, CitaRequestDTO dto) {
                Cita citaDb = citaRepositorio.findById(id)
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                "Cita no encontrada con ID: " + id));

                ServicioMedico servicio = servicioRepositorio.findById(dto.getServicioId())
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                "Servicio no encontrado"));

                Usuario veterinario = usuarioRepositorio.findById(dto.getVeterinarioId())
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                "Veterinario no encontrado"));

                List<Paciente> pacientes = pacienteRepositorio.findAllById(dto.getPacienteIds());
                if (pacientes.isEmpty()) {
                        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encontraron los pacientes");
                }

                // Recalculamos tiempos por si cambió de servicio (ej. de Consulta a Cirugía)
                int tiempoTotalOcupado = servicio.getDuracionMinutos() + servicio.getBufferMinutos();
                LocalTime horaFinCalculada = dto.getHoraInicio().plusMinutes(tiempoTotalOcupado);

                // AQUÍ ESTÁ LA MAGIA: Pasamos el 'id' de la cita actual para que el sistema la
                // ignore en la búsqueda de cruces
                boolean existeCruce = citaRepositorio.existeCruceDeHorario(
                                veterinario.getId(),
                                dto.getFecha(),
                                dto.getHoraInicio(),
                                horaFinCalculada,
                                id,
                                ESTADOS_IGNORADOS);

                if (existeCruce) {
                        throw new ResponseStatusException(HttpStatus.CONFLICT,
                                        "No se puede reprogramar: El veterinario ya tiene otro compromiso en ese horario.");
                }

                citaDb.setFecha(dto.getFecha());
                citaDb.setHoraInicio(dto.getHoraInicio());
                citaDb.setHoraFin(horaFinCalculada);
                citaDb.setMotivo(dto.getMotivo());
                citaDb.setServicio(servicio);
                citaDb.setVeterinario(veterinario);
                citaDb.setPacientes(pacientes);

                Cita citaGuardada = citaRepositorio.save(citaDb);
                return mapearAResponse(citaGuardada);
        }

        public void eliminar(Long id) {
                Cita citaDb = citaRepositorio.findById(id)
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                "Cita no encontrada con ID: " + id));
                citaRepositorio.delete(citaDb);
        }

        private CitaResponseDTO mapearAResponse(Cita cita) {
                List<Long> pacientesIds = cita.getPacientes().stream()
                                .map(Paciente::getId)
                                .collect(Collectors.toList());

                return new CitaResponseDTO(
                                cita.getId(),
                                cita.getFecha(),
                                cita.getHoraInicio(),
                                cita.getHoraFin(),
                                cita.getServicio().getNombre(), // En el orden correcto (5)
                                cita.getVeterinario().getId(), // En el orden correcto (6)
                                cita.getMotivo(), // En el orden correcto (7)
                                cita.getEstado(), // En el orden correcto (8)
                                pacientesIds // (9)
                );
        }
}