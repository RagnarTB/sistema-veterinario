package com.veterinaria.servicios;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.veterinaria.dtos.CitaRequestDTO;
import com.veterinaria.dtos.CitaResponseDTO;
import com.veterinaria.modelos.Cita;
import com.veterinaria.modelos.Paciente;
import com.veterinaria.modelos.Enums.EstadoCita;
import com.veterinaria.respositorios.CitaRepositorio;
import com.veterinaria.respositorios.PacienteRepositorio;

@Service
public class CitaServicio {

        private final CitaRepositorio citaRepositorio;
        private final PacienteRepositorio pacienteRepositorio;

        public CitaServicio(CitaRepositorio citaRepositorio, PacienteRepositorio pacienteRepositorio) {
                this.citaRepositorio = citaRepositorio;
                this.pacienteRepositorio = pacienteRepositorio;
        }

        public CitaResponseDTO guardar(CitaRequestDTO dto) {
                // ¡NUEVO! Buscamos a TODOS los pacientes de la lista de IDs a la vez
                List<Paciente> pacientes = pacienteRepositorio.findAllById(dto.getPacienteIds());

                if (pacientes.isEmpty()) {
                        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encontraron los pacientes");
                }

                Cita cita = new Cita();
                cita.setEstado(EstadoCita.PENDIENTE);
                cita.setFecha(dto.getFecha());
                cita.setHora(dto.getHora());
                cita.setMotivo(dto.getMotivo());
                cita.setPacientes(pacientes); // Guardamos la lista completa

                Cita citaGuardada = citaRepositorio.save(cita);

                return mapearAResponse(citaGuardada);
        }

        public List<CitaResponseDTO> listar() {
                List<Cita> citas = citaRepositorio.findAll();
                return citas.stream().map(this::mapearAResponse).collect(Collectors.toList());
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

                List<Paciente> pacientes = pacienteRepositorio.findAllById(dto.getPacienteIds());
                if (pacientes.isEmpty()) {
                        throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                                        "No se encontraron los pacientes para actualizar");
                }

                citaDb.setFecha(dto.getFecha());
                citaDb.setHora(dto.getHora());
                citaDb.setMotivo(dto.getMotivo());
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

        // --- MÉTODO AYUDANTE PARA MAPEAR ---
        private CitaResponseDTO mapearAResponse(Cita cita) {
                // Extraemos solo los IDs de la lista de pacientes para enviarlos a Angular en
                // el Response
                List<Long> pacientesIds = cita.getPacientes().stream()
                                .map(Paciente::getId)
                                .collect(Collectors.toList());

                return new CitaResponseDTO(
                                cita.getId(),
                                cita.getFecha(),
                                cita.getHora(),
                                cita.getMotivo(),
                                cita.getEstado(),
                                pacientesIds);
        }
}