package com.veterinaria.servicios;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.veterinaria.dtos.CitaRequestDTO;
import com.veterinaria.dtos.CitaResponseDTO;
import com.veterinaria.dtos.SlotDisponibilidadDTO;
import com.veterinaria.modelos.Cita;
import com.veterinaria.modelos.HorarioVeterinario;
import com.veterinaria.modelos.Paciente;
import com.veterinaria.modelos.ServicioMedico;
import com.veterinaria.modelos.Usuario;
import com.veterinaria.modelos.Enums.EstadoCita;
import com.veterinaria.respositorios.CitaRepositorio;
import com.veterinaria.respositorios.DiaBloqueadoRepositorio;
import com.veterinaria.respositorios.HorarioVeterinarioRepositorio;
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

        private final HorarioVeterinarioRepositorio horarioRepositorio;
        private final DiaBloqueadoRepositorio diaBloqueadoRepositorio;

        public CitaServicio(CitaRepositorio citaRepositorio, PacienteRepositorio pacienteRepositorio,
                        ServicioMedicoRepositorio servicioRepositorio, UsuarioRepositorio usuarioRepositorio,
                        HorarioVeterinarioRepositorio horarioRepositorio,
                        DiaBloqueadoRepositorio diaBloqueadoRepositorio) {
                this.citaRepositorio = citaRepositorio;
                this.pacienteRepositorio = pacienteRepositorio;
                this.servicioRepositorio = servicioRepositorio;
                this.usuarioRepositorio = usuarioRepositorio;
                this.horarioRepositorio = horarioRepositorio;
                this.diaBloqueadoRepositorio = diaBloqueadoRepositorio;
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

        public Page<CitaResponseDTO> listar(Pageable pageable) {
                return citaRepositorio.findAll(pageable).map(this::mapearAResponse);
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

        // EL MOTOR DE DISPONIBILIDAD
        public List<SlotDisponibilidadDTO> obtenerDisponibilidad(Long veterinarioId, LocalDate fecha, Long servicioId) {

                // 1. Si el día es feriado o el doctor pidió permiso, devolvemos lista vacía
                // inmediatamente
                if (diaBloqueadoRepositorio.estaBloqueadoElDia(fecha, veterinarioId)) {
                        return List.of();
                }

                // 2. Buscamos el horario de trabajo del doctor para ese día (ej. LUNES)
                HorarioVeterinario horario = horarioRepositorio
                                .findByVeterinarioIdAndDiaSemana(veterinarioId, fecha.getDayOfWeek())
                                .orElse(null);

                if (horario == null) {
                        return List.of(); // Ese día no trabaja
                }

                // 3. Calculamos cuánto dura la atención completa
                ServicioMedico servicio = servicioRepositorio.findById(servicioId)
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                "Servicio no encontrado"));
                int duracionTotal = servicio.getDuracionMinutos() + servicio.getBufferMinutos();

                // 4. Traemos todas las citas que ya tiene el doctor ese día
                List<Cita> citasDelDia = citaRepositorio.buscarCitasAgendadasDelDia(veterinarioId, fecha,
                                ESTADOS_IGNORADOS);

                List<SlotDisponibilidadDTO> slotsDisponibles = new java.util.ArrayList<>();
                LocalTime horaActual = horario.getHoraEntrada();
                if (fecha.equals(LocalDate.now()) && LocalTime.now().isAfter(horaActual)) {
                        horaActual = LocalTime.now();
                }

                // 5. El Bucle Principal: Iteramos minuto a minuto generando bloques
                while (horaActual.plusMinutes(duracionTotal).compareTo(horario.getHoraSalida()) <= 0) {
                        LocalTime finSlot = horaActual.plusMinutes(duracionTotal);

                        // A. ¿Choca con el refrigerio? (Si tiene refrigerio configurado)
                        boolean chocaConRefrigerio = false;
                        if (horario.getInicioRefrigerio() != null && horario.getFinRefrigerio() != null) {
                                if (horaActual.isBefore(horario.getFinRefrigerio())
                                                && finSlot.isAfter(horario.getInicioRefrigerio())) {
                                        chocaConRefrigerio = true;
                                        // Saltamos el tiempo directo al fin del refrigerio para ahorrar iteraciones
                                        horaActual = horario.getFinRefrigerio();
                                        continue;
                                }
                        }

                        // B. ¿Choca con alguna cita existente?
                        boolean chocaConCita = false;
                        for (Cita cita : citasDelDia) {
                                if (horaActual.isBefore(cita.getHoraFin()) && finSlot.isAfter(cita.getHoraInicio())) {
                                        chocaConCita = true;
                                        // Saltamos el tiempo al final de esa cita para buscar el siguiente hueco
                                        horaActual = cita.getHoraFin();
                                        break;
                                }
                        }

                        // C. Si sobrevivió a las validaciones, ¡Tenemos un hueco libre!
                        if (!chocaConRefrigerio && !chocaConCita) {
                                slotsDisponibles.add(new SlotDisponibilidadDTO(horaActual, finSlot));
                                // Avanzamos la hora para buscar el siguiente bloque.
                                // Podrías avanzar 'duracionTotal' o intervalos fijos de 15 mins. Lo haremos
                                // fijo cada 15 mins para dar flexibilidad al usuario.
                                horaActual = horaActual.plusMinutes(15);
                        }
                }

                return slotsDisponibles;
        }

        // MÉTODO PARA EL TABLERO DE RECEPCIÓN
        public void cambiarEstado(Long id, EstadoCita nuevoEstado) {
                Cita citaDb = citaRepositorio.findById(id)
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                "Cita no encontrada con ID: " + id));

                // Aquí podríamos añadir lógica de negocio compleja (ej. prohibir pasar de
                // CANCELADA a COMPLETADA)
                // Por ahora, confiamos en que el frontend enviará transiciones lógicas.
                citaDb.setEstado(nuevoEstado);

                citaRepositorio.save(citaDb);
        }
}