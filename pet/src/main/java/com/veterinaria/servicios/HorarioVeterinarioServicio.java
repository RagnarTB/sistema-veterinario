package com.veterinaria.servicios;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.veterinaria.dtos.HorarioVeterinarioRequestDTO;
import com.veterinaria.dtos.HorarioVeterinarioResponseDTO;
import com.veterinaria.modelos.HorarioVeterinario;
import com.veterinaria.modelos.Empleado;
import com.veterinaria.respositorios.HorarioVeterinarioRepositorio;
import com.veterinaria.respositorios.EmpleadoRepositorio;
import com.veterinaria.modelos.Sede;
import com.veterinaria.respositorios.SedeRepositorio;

@Service
public class HorarioVeterinarioServicio {

    private final HorarioVeterinarioRepositorio horarioRepositorio;
    private final EmpleadoRepositorio empleadoRepositorio;
    private final SedeRepositorio sedeRepositorio;

    public HorarioVeterinarioServicio(HorarioVeterinarioRepositorio horarioRepositorio,
            EmpleadoRepositorio empleadoRepositorio, SedeRepositorio sedeRepositorio) {
        this.horarioRepositorio = horarioRepositorio;
        this.empleadoRepositorio = empleadoRepositorio;
        this.sedeRepositorio = sedeRepositorio;
    }

    public HorarioVeterinarioResponseDTO guardar(HorarioVeterinarioRequestDTO dto) {
        // Validar que el doctor exista, esté activo y sea veterinario
        Empleado veterinario = empleadoRepositorio.findById(dto.getVeterinarioId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Veterinario no encontrado"));

        if (!veterinario.getActivo()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El veterinario no se encuentra activo");
        }

        boolean esVeterinario = veterinario.getUsuario().getRoles().stream()
                .anyMatch(r -> r.getNombre().equals("ROLE_VETERINARIO"));
        if (!esVeterinario) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El empleado seleccionado no es un veterinario");
        }

        Sede sede = sedeRepositorio.findById(dto.getSedeId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sede no encontrada"));

        // Validar que el veterinario pertenezca a la sede
        boolean perteneceSede = veterinario.getSedes().stream().anyMatch(s -> s.getId().equals(sede.getId()));
        if (!perteneceSede) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El veterinario no pertenece a la sede seleccionada");
        }

        // Validar que no tenga ya un horario asignado para ese mismo día EN ESA SEDE
        if (horarioRepositorio.findByVeterinarioIdAndDiaSemanaAndSedeId(dto.getVeterinarioId(), dto.getDiaSemana(), dto.getSedeId())
                .isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El veterinario ya tiene un horario asignado para este día en esta sede");
        }

        if (dto.getHoraEntrada().isAfter(dto.getHoraSalida())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La hora de entrada no puede ser después de la salida");
        }
        if (dto.getInicioRefrigerio() != null && (dto.getInicioRefrigerio().isBefore(dto.getHoraEntrada())
                || dto.getFinRefrigerio().isAfter(dto.getHoraSalida()))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El refrigerio debe estar dentro del horario de trabajo");
        }

        HorarioVeterinario horario = new HorarioVeterinario();
        horario.setVeterinario(veterinario);
        horario.setSede(sede);
        horario.setDiaSemana(dto.getDiaSemana());
        horario.setHoraEntrada(dto.getHoraEntrada());
        horario.setHoraSalida(dto.getHoraSalida());
        horario.setInicioRefrigerio(dto.getInicioRefrigerio());
        horario.setFinRefrigerio(dto.getFinRefrigerio());

        HorarioVeterinario guardado = horarioRepositorio.save(horario);
        return mapearAResponse(guardado);
    }

    public List<HorarioVeterinarioResponseDTO> listarTodos() {
        return horarioRepositorio.findAll().stream()
                .map(this::mapearAResponse)
                .collect(Collectors.toList());
    }

    public void eliminar(Long id) {
        HorarioVeterinario horario = horarioRepositorio.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Horario no encontrado"));
        horarioRepositorio.delete(horario);
    }

    private HorarioVeterinarioResponseDTO mapearAResponse(HorarioVeterinario h) {
        return new HorarioVeterinarioResponseDTO(
                h.getId(), h.getVeterinario().getId(), h.getDiaSemana(),
                h.getHoraEntrada(), h.getHoraSalida(), h.getInicioRefrigerio(), h.getFinRefrigerio(),
                h.getSede().getId());
    }
}