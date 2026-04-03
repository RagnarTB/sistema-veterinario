package com.veterinaria.servicios;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.veterinaria.dtos.ServicioMedicoRequestDTO;
import com.veterinaria.dtos.ServicioMedicoResponseDTO;
import com.veterinaria.modelos.ServicioMedico;
import com.veterinaria.respositorios.ServicioMedicoRepositorio;

@Service
public class ServicioMedicoServicio {

    private final ServicioMedicoRepositorio servicioRepositorio;

    public ServicioMedicoServicio(ServicioMedicoRepositorio servicioRepositorio) {
        this.servicioRepositorio = servicioRepositorio;
    }

    public ServicioMedicoResponseDTO guardar(ServicioMedicoRequestDTO dto) {
        ServicioMedico servicio = new ServicioMedico();
        servicio.setNombre(dto.getNombre());
        servicio.setDescripcion(dto.getDescripcion());
        servicio.setPrecio(dto.getPrecio());
        servicio.setDuracionMinutos(dto.getDuracionMinutos());
        servicio.setBufferMinutos(dto.getBufferMinutos());
        // 'activo' ya viene en true por defecto desde el modelo

        ServicioMedico guardado = servicioRepositorio.save(servicio);
        return mapearAResponse(guardado);
    }

    public List<ServicioMedicoResponseDTO> listarTodos() {
        return servicioRepositorio.findAll().stream()
                .map(this::mapearAResponse)
                .collect(Collectors.toList());
    }

    public ServicioMedicoResponseDTO buscarPorId(Long id) {
        return servicioRepositorio.findById(id)
                .map(this::mapearAResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Servicio no encontrado"));
    }

    public ServicioMedicoResponseDTO actualizar(Long id, ServicioMedicoRequestDTO dto) {
        ServicioMedico servicio = servicioRepositorio.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Servicio no encontrado"));

        servicio.setNombre(dto.getNombre());
        servicio.setDescripcion(dto.getDescripcion());
        servicio.setPrecio(dto.getPrecio());
        servicio.setDuracionMinutos(dto.getDuracionMinutos());
        servicio.setBufferMinutos(dto.getBufferMinutos());

        ServicioMedico actualizado = servicioRepositorio.save(servicio);
        return mapearAResponse(actualizado);
    }

    // Soft Delete: No borramos el servicio de la BD para no romper el historial de
    // citas antiguas. Solo lo desactivamos.
    public void cambiarEstado(Long id, Boolean estado) {
        ServicioMedico servicio = servicioRepositorio.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Servicio no encontrado"));
        servicio.setActivo(estado);
        servicioRepositorio.save(servicio);
    }

    private ServicioMedicoResponseDTO mapearAResponse(ServicioMedico s) {
        return new ServicioMedicoResponseDTO(
                s.getId(), s.getNombre(), s.getDescripcion(),
                s.getPrecio(), s.getDuracionMinutos(), s.getBufferMinutos(), s.getActivo());
    }
}