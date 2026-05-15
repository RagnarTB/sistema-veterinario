package com.veterinaria.servicios;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.veterinaria.dtos.DiaBloqueadoRequestDTO;
import com.veterinaria.dtos.DiaBloqueadoResponseDTO;
import com.veterinaria.modelos.DiaBloqueado;
import com.veterinaria.modelos.Empleado;
import com.veterinaria.modelos.Enums.EstadoCita;
import com.veterinaria.respositorios.CitaRepositorio;
import com.veterinaria.respositorios.DiaBloqueadoRepositorio;
import com.veterinaria.respositorios.EmpleadoRepositorio;

@Service
public class DiaBloqueadoServicio {

    private final DiaBloqueadoRepositorio diaBloqueadoRepositorio;
    private final EmpleadoRepositorio empleadoRepositorio;
    private final CitaRepositorio citaRepositorio;

    public DiaBloqueadoServicio(DiaBloqueadoRepositorio diaBloqueadoRepositorio,
            EmpleadoRepositorio empleadoRepositorio, CitaRepositorio citaRepositorio) {
        this.diaBloqueadoRepositorio = diaBloqueadoRepositorio;
        this.empleadoRepositorio = empleadoRepositorio;
        this.citaRepositorio = citaRepositorio;
    }

    public DiaBloqueadoResponseDTO guardar(DiaBloqueadoRequestDTO dto) {
        List<EstadoCita> estadosPendientes = Arrays.asList(EstadoCita.AGENDADA, EstadoCita.CONFIRMADA);
        boolean tieneCitas;

        java.time.LocalDate fechaFinCalc = dto.getFechaFin() != null ? dto.getFechaFin() : dto.getFecha();

        // Diferenciamos si es bloqueo de un doctor o de toda la clínica
        if (dto.getVeterinarioId() != null) {
            tieneCitas = citaRepositorio.existenCitasPendientesPorVeterinarioYRangoFechas(
                    dto.getVeterinarioId(), dto.getFecha(), fechaFinCalc, estadosPendientes);
        } else {
            tieneCitas = citaRepositorio.existenCitasPendientesPorRangoFechas(dto.getFecha(), fechaFinCalc, estadosPendientes);
        }

        if (tieneCitas) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "No se puede bloquear el día o rango. Existen citas agendadas o confirmadas en esas fechas. Reprograme a los pacientes primero.");
        }

        DiaBloqueado diaBloqueado = new DiaBloqueado();
        diaBloqueado.setFecha(dto.getFecha());
        diaBloqueado.setFechaFin(dto.getFechaFin());
        diaBloqueado.setTipo(dto.getTipo());
        diaBloqueado.setMotivo(dto.getMotivo());

        if (dto.getVeterinarioId() != null) {
            Empleado veterinario = empleadoRepositorio.findById(dto.getVeterinarioId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Veterinario no encontrado"));
            diaBloqueado.setVeterinario(veterinario);
        }

        DiaBloqueado guardado = diaBloqueadoRepositorio.save(diaBloqueado);
        return mapearAResponse(guardado);
    }   

    public List<DiaBloqueadoResponseDTO> listarTodos() {
        return diaBloqueadoRepositorio.findAll().stream()
                .map(this::mapearAResponse)
                .collect(Collectors.toList());
    }

    public void eliminar(Long id) {
        DiaBloqueado diaBloqueado = diaBloqueadoRepositorio.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Día bloqueado no encontrado"));
        diaBloqueadoRepositorio.delete(diaBloqueado);
    }

    private DiaBloqueadoResponseDTO mapearAResponse(DiaBloqueado d) {
        Long vetId = (d.getVeterinario() != null) ? d.getVeterinario().getId() : null;
        return new DiaBloqueadoResponseDTO(d.getId(), d.getFecha(), d.getFechaFin(), d.getTipo(), d.getMotivo(), vetId);
    }
}