package com.veterinaria.servicios;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.veterinaria.dtos.HorarioVeterinarioRequestDTO;
import com.veterinaria.dtos.HorarioVeterinarioResponseDTO;
import com.veterinaria.modelos.HorarioVeterinario;
import com.veterinaria.modelos.Usuario;
import com.veterinaria.respositorios.HorarioVeterinarioRepositorio;
import com.veterinaria.respositorios.UsuarioRepositorio;

@Service
public class HorarioVeterinarioServicio {

    private final HorarioVeterinarioRepositorio horarioRepositorio;
    private final UsuarioRepositorio usuarioRepositorio;

    public HorarioVeterinarioServicio(HorarioVeterinarioRepositorio horarioRepositorio,
            UsuarioRepositorio usuarioRepositorio) {
        this.horarioRepositorio = horarioRepositorio;
        this.usuarioRepositorio = usuarioRepositorio;
    }

    public HorarioVeterinarioResponseDTO guardar(HorarioVeterinarioRequestDTO dto) {
        // Validar que el doctor exista
        Usuario veterinario = usuarioRepositorio.findById(dto.getVeterinarioId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Veterinario no encontrado"));

        // Validar que no tenga ya un horario asignado para ese mismo día
        if (horarioRepositorio.findByVeterinarioIdAndDiaSemana(dto.getVeterinarioId(), dto.getDiaSemana())
                .isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El veterinario ya tiene un horario asignado para este día");
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
                h.getHoraEntrada(), h.getHoraSalida(), h.getInicioRefrigerio(), h.getFinRefrigerio());
    }
}