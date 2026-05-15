package com.veterinaria.servicios;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import com.veterinaria.excepciones.ResourceNotFoundException;
import com.veterinaria.excepciones.BusinessLogicException;

import com.veterinaria.dtos.CitaRequestDTO;
import com.veterinaria.dtos.CitaResponseDTO;
import com.veterinaria.dtos.SlotDisponibilidadDTO;
import com.veterinaria.modelos.Cita;
import com.veterinaria.modelos.Cliente;
import com.veterinaria.modelos.HorarioVeterinario;
import com.veterinaria.modelos.Paciente;
import com.veterinaria.modelos.ServicioMedico;
import com.veterinaria.modelos.Empleado;
import com.veterinaria.modelos.Enums.EstadoCita;
import com.veterinaria.respositorios.CitaRepositorio;
import com.veterinaria.respositorios.DiaBloqueadoRepositorio;
import com.veterinaria.respositorios.HorarioVeterinarioRepositorio;
import com.veterinaria.respositorios.PacienteRepositorio;
import com.veterinaria.respositorios.ServicioMedicoRepositorio;
import com.veterinaria.respositorios.EmpleadoRepositorio;
import com.veterinaria.modelos.Sede;
import com.veterinaria.respositorios.SedeRepositorio;

@Service
public class CitaServicio {

        private final CitaRepositorio citaRepositorio;
        private final PacienteRepositorio pacienteRepositorio;
        private final ServicioMedicoRepositorio servicioRepositorio;
        private final EmpleadoRepositorio empleadoRepositorio;
        private final List<EstadoCita> ESTADOS_IGNORADOS = List.of(EstadoCita.CANCELADA, EstadoCita.NO_ASISTIO);

        private final HorarioVeterinarioRepositorio horarioRepositorio;
        private final DiaBloqueadoRepositorio diaBloqueadoRepositorio;
        private final SedeRepositorio sedeRepositorio;

        public CitaServicio(CitaRepositorio citaRepositorio, PacienteRepositorio pacienteRepositorio,
                        ServicioMedicoRepositorio servicioRepositorio, EmpleadoRepositorio empleadoRepositorio,
                        HorarioVeterinarioRepositorio horarioRepositorio,
                        DiaBloqueadoRepositorio diaBloqueadoRepositorio,
                        SedeRepositorio sedeRepositorio) {
                this.citaRepositorio = citaRepositorio;
                this.pacienteRepositorio = pacienteRepositorio;
                this.servicioRepositorio = servicioRepositorio;
                this.empleadoRepositorio = empleadoRepositorio;
                this.horarioRepositorio = horarioRepositorio;
                this.diaBloqueadoRepositorio = diaBloqueadoRepositorio;
                this.sedeRepositorio = sedeRepositorio;
        }

        @Transactional
        public CitaResponseDTO guardar(CitaRequestDTO dto) {
                validarFechaHoraNoPasado(dto.getFecha(), dto.getHoraInicio());
                ServicioMedico servicio = servicioRepositorio.findById(dto.getServicioId())
                                .orElseThrow(() -> new ResourceNotFoundException("Servicio no encontrado"));

                Empleado veterinario = empleadoRepositorio.findById(dto.getVeterinarioId())
                                .orElseThrow(() -> new ResourceNotFoundException("Veterinario no encontrado"));

                Sede sede = sedeRepositorio.findById(dto.getSedeId())
                                .orElseThrow(() -> new ResourceNotFoundException("Sede no encontrada"));

                List<Paciente> pacientes = pacienteRepositorio.findAllById(dto.getPacienteIds());
                if (pacientes.isEmpty()) {
                        throw new ResourceNotFoundException("No se encontraron los pacientes");
                }

                // Validación: todos los pacientes deben pertenecer al mismo cliente
                Cliente clienteBase = pacientes.get(0).getCliente();
                if (pacientes.stream().anyMatch(p -> !p.getCliente().equals(clienteBase))) {
                        throw new BusinessLogicException("Todos los pacientes de la cita deben pertenecer al mismo cliente. No se permiten mascotas de distintos dueños en una misma cita.");
                }

                int cantidadMascotas = dto.getPacienteIds().size();
                int tiempoTotalOcupado = (servicio.getDuracionMinutos() + servicio.getBufferMinutos())
                                * cantidadMascotas;
                LocalTime horaFinCalculada = dto.getHoraInicio().plusMinutes(tiempoTotalOcupado);

                // Mitigación de carrera: tomamos lock sobre las citas del día antes de chequear y guardar.
                citaRepositorio.buscarCitasAgendadasDelDiaConLock(veterinario.getId(), dto.getFecha(), ESTADOS_IGNORADOS);

                // Pasamos -1L porque al ser una cita NUEVA, no hay ningún ID real que ignorar
                boolean existeCruce = citaRepositorio.existeCruceDeHorario(
                                veterinario.getId(),
                                dto.getFecha(),
                                dto.getHoraInicio(),
                                horaFinCalculada,
                                -1L,
                                ESTADOS_IGNORADOS);

                if (existeCruce) {
                        throw new BusinessLogicException("El veterinario ya tiene una cita ocupando este horario.");
                }

                Cita cita = new Cita();
                cita.setEstado(EstadoCita.AGENDADA);
                cita.setFecha(dto.getFecha());
                cita.setHoraInicio(dto.getHoraInicio());
                cita.setHoraFin(horaFinCalculada);
                cita.setMotivo(dto.getMotivo());
                cita.setServicio(servicio);
                cita.setVeterinario(veterinario);
                cita.setSede(sede);
                cita.setPacientes(pacientes);

                Cita citaGuardada = citaRepositorio.save(cita);
                return mapearAResponse(citaGuardada);
        }

        public Page<CitaResponseDTO> listar(Long sedeId, String buscar, Pageable pageable) {
                Page<Cita> pagina;
                if (buscar != null && !buscar.trim().isEmpty()) {
                        pagina = citaRepositorio.buscarEnSede(sedeId, buscar, pageable);
                } else {
                        pagina = citaRepositorio.findBySedeId(sedeId, pageable);
                }
                return pagina.map(this::mapearAResponse);
        }

        public CitaResponseDTO buscarPorId(Long id) {
                return citaRepositorio.findById(id)
                                .map(this::mapearAResponse)
                                .orElseThrow(() -> new ResourceNotFoundException("Cita no encontrada con ID: " + id));
        }

        @Transactional
        public CitaResponseDTO actualizar(Long id, CitaRequestDTO dto) {
                validarFechaHoraNoPasado(dto.getFecha(), dto.getHoraInicio());
                Cita citaDb = citaRepositorio.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("Cita no encontrada con ID: " + id));

                ServicioMedico servicio = servicioRepositorio.findById(dto.getServicioId())
                                .orElseThrow(() -> new ResourceNotFoundException("Servicio no encontrado"));

                Empleado veterinario = empleadoRepositorio.findById(dto.getVeterinarioId())
                                .orElseThrow(() -> new ResourceNotFoundException("Veterinario no encontrado"));

                Sede sede = sedeRepositorio.findById(dto.getSedeId())
                                .orElseThrow(() -> new ResourceNotFoundException("Sede no encontrada"));

                List<Paciente> pacientes = pacienteRepositorio.findAllById(dto.getPacienteIds());
                if (pacientes.isEmpty()) {
                        throw new ResourceNotFoundException("No se encontraron los pacientes");
                }

                Cliente clienteBase = pacientes.get(0).getCliente();
                if (pacientes.stream().anyMatch(p -> !p.getCliente().equals(clienteBase))) {
                        throw new BusinessLogicException("Todos los pacientes de la cita deben pertenecer al mismo cliente.");
                }

                // Recalculamos tiempos por si cambió de servicio (ej. de Consulta a Cirugía)
                int cantidadMascotas = dto.getPacienteIds().size();
                int tiempoTotalOcupado = (servicio.getDuracionMinutos() + servicio.getBufferMinutos())
                                * cantidadMascotas;
                LocalTime horaFinCalculada = dto.getHoraInicio().plusMinutes(tiempoTotalOcupado);

                citaRepositorio.buscarCitasAgendadasDelDiaConLock(veterinario.getId(), dto.getFecha(), ESTADOS_IGNORADOS);

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
                        throw new BusinessLogicException("No se puede reprogramar: El veterinario ya tiene otro compromiso en ese horario.");
                }

                citaDb.setFecha(dto.getFecha());
                citaDb.setHoraInicio(dto.getHoraInicio());
                citaDb.setHoraFin(horaFinCalculada);
                citaDb.setMotivo(dto.getMotivo());
                citaDb.setServicio(servicio);
                citaDb.setVeterinario(veterinario);
                citaDb.setSede(sede);
                citaDb.setPacientes(pacientes);

                Cita citaGuardada = citaRepositorio.save(citaDb);
                return mapearAResponse(citaGuardada);
        }

        @Transactional
        public void eliminar(Long id) {
                Cita citaDb = citaRepositorio.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("Cita no encontrada con ID: " + id));

                // solo se puede borrar si está AGENDADA
                if (citaDb.getEstado() != EstadoCita.AGENDADA) {
                        throw new BusinessLogicException("No se puede eliminar una cita que ya fue " + citaDb.getEstado()
                                                        + ". Estado actual: " + citaDb.getEstado());
                }

                citaDb.setEstado(EstadoCita.CANCELADA);
                citaRepositorio.save(citaDb);
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
                                pacientesIds, // (9)
                                cita.getSede().getId());
        }

        // EL MOTOR DE DISPONIBILIDAD
        public List<SlotDisponibilidadDTO> obtenerDisponibilidad(Long veterinarioId, LocalDate fecha, Long servicioId,
                        Long sedeId, int cantidadPacientes) {

                if (cantidadPacientes < 1) {
                        throw new BusinessLogicException("cantidadPacientes debe ser >= 1");
                }

                // 1. Si el día es feriado o el doctor pidió permiso, devolvemos lista vacía
                // inmediatamente
                if (diaBloqueadoRepositorio.estaBloqueadoElDia(fecha, veterinarioId)) {
                        return List.of();
                }

                // 2. Buscamos el horario de trabajo del doctor para ese día (ej. LUNES) en esa
                // sede
                HorarioVeterinario horario = horarioRepositorio
                                .findByVeterinarioIdAndDiaSemanaAndSedeId(veterinarioId, fecha.getDayOfWeek(), sedeId)
                                .orElse(null);

                if (horario == null) {
                        return List.of(); // Ese día no trabaja
                }

                // 3. Calculamos cuánto dura la atención completa
                ServicioMedico servicio = servicioRepositorio.findById(servicioId)
                                .orElseThrow(() -> new ResourceNotFoundException("Servicio no encontrado"));
                int duracionPorPaciente = servicio.getDuracionMinutos() + servicio.getBufferMinutos();
                int duracionTotal = duracionPorPaciente * cantidadPacientes;

                // 4. Traemos todas las citas que ya tiene el doctor ese día
                List<Cita> citasDelDia = citaRepositorio.buscarCitasAgendadasDelDia(veterinarioId, fecha,
                                ESTADOS_IGNORADOS);

                List<SlotDisponibilidadDTO> slotsDisponibles = new java.util.ArrayList<>();
                LocalTime horaActual = horario.getHoraEntrada();
                if (fecha.equals(LocalDate.now()) && LocalTime.now().isAfter(horaActual)) {
                        horaActual = LocalTime.now().withSecond(0).withNano(0);
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
                                horaActual = horaActual.plusMinutes(duracionTotal);
                        }
                }

                return slotsDisponibles;
        }

        // MÉTODO PARA EL TABLERO DE RECEPCIÓN
        @Transactional
        public void cambiarEstado(Long id, EstadoCita nuevoEstado) {
                Cita citaDb = citaRepositorio.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("Cita no encontrada con ID: " + id));

                validarTransicionEstado(citaDb.getEstado(), nuevoEstado);
                citaDb.setEstado(nuevoEstado);

                citaRepositorio.save(citaDb);
        }

        private static void validarFechaHoraNoPasado(LocalDate fecha, LocalTime horaInicio) {
                if (fecha == null || horaInicio == null) {
                        return; // Bean Validation se encarga; aquí evitamos NPE.
                }

                if (fecha.isBefore(LocalDate.now())) {
                        throw new BusinessLogicException("No se permiten citas en el pasado");
                }
                if (fecha.equals(LocalDate.now()) && horaInicio.isBefore(LocalTime.now().withSecond(0).withNano(0))) {
                        throw new BusinessLogicException("No se permiten citas en el pasado");
                }
        }

        private static void validarTransicionEstado(EstadoCita actual, EstadoCita nuevo) {
                if (actual == null || nuevo == null) {
                        throw new BusinessLogicException("Estado inválido");
                }

                if (!actual.puedeTransitarA(nuevo)) {
                        throw new BusinessLogicException("Transición de estado inválida: " + actual + " -> " + nuevo);
                }
        }
}