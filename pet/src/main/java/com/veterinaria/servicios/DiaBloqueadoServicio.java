package com.veterinaria.servicios;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.veterinaria.dtos.DiaBloqueadoRequestDTO;
import com.veterinaria.dtos.DiaBloqueadoResponseDTO;
import com.veterinaria.modelos.DiaBloqueado;
import com.veterinaria.modelos.Usuario;
import com.veterinaria.respositorios.DiaBloqueadoRepositorio;
import com.veterinaria.respositorios.UsuarioRepositorio;

@Service
public class DiaBloqueadoServicio {

    private final DiaBloqueadoRepositorio diaBloqueadoRepositorio;
    private final UsuarioRepositorio usuarioRepositorio;

    public DiaBloqueadoServicio(DiaBloqueadoRepositorio diaBloqueadoRepositorio,
            UsuarioRepositorio usuarioRepositorio) {
        this.diaBloqueadoRepositorio = diaBloqueadoRepositorio;
        this.usuarioRepositorio = usuarioRepositorio;
    }

    public DiaBloqueadoResponseDTO guardar(DiaBloqueadoRequestDTO dto) {
        DiaBloqueado diaBloqueado = new DiaBloqueado();
        diaBloqueado.setFecha(dto.getFecha());
        diaBloqueado.setMotivo(dto.getMotivo());

        // Validamos si es un permiso para un doctor específico o un cierre general
        if (dto.getVeterinarioId() != null) {
            Usuario veterinario = usuarioRepositorio.findById(dto.getVeterinarioId())
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
        return new DiaBloqueadoResponseDTO(d.getId(), d.getFecha(), d.getMotivo(), vetId);
    }
}