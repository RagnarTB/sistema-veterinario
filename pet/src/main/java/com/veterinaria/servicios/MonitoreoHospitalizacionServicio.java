package com.veterinaria.servicios;

import com.veterinaria.dtos.MonitoreoHospitalizacionRequestDTO;
import com.veterinaria.dtos.MonitoreoHospitalizacionResponseDTO;
import com.veterinaria.modelos.Empleado;
import com.veterinaria.modelos.Hospitalizacion;
import com.veterinaria.modelos.MonitoreoHospitalizacion;
import com.veterinaria.respositorios.EmpleadoRepositorio;
import com.veterinaria.respositorios.HospitalizacionRepositorio;
import com.veterinaria.respositorios.MonitoreoHospitalizacionRepositorio;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MonitoreoHospitalizacionServicio {

    private final MonitoreoHospitalizacionRepositorio monitoreoRepositorio;
    private final HospitalizacionRepositorio hospitalizacionRepositorio;
    private final EmpleadoRepositorio empleadoRepositorio;

    @Transactional
    public MonitoreoHospitalizacionResponseDTO agregarMonitoreo(MonitoreoHospitalizacionRequestDTO requestDTO) {
        Hospitalizacion hospitalizacion = hospitalizacionRepositorio.findById(requestDTO.getHospitalizacionId())
                .orElseThrow(() -> new EntityNotFoundException("Hospitalización no encontrada con ID: " + requestDTO.getHospitalizacionId()));

        if (!"ACTIVA".equalsIgnoreCase(hospitalizacion.getEstado())) {
            throw new IllegalStateException("No se pueden agregar monitoreos a una hospitalización que no esté ACTIVA.");
        }

        Empleado empleado = empleadoRepositorio.findById(requestDTO.getEmpleadoId())
                .orElseThrow(() -> new EntityNotFoundException("Empleado (técnico/veterinario) no encontrado con ID: " + requestDTO.getEmpleadoId()));

        MonitoreoHospitalizacion monitoreo = new MonitoreoHospitalizacion();
        monitoreo.setFechaHora(requestDTO.getFechaHora());
        monitoreo.setTemperatura(requestDTO.getTemperatura());
        monitoreo.setFrecuenciaCardiaca(requestDTO.getFrecuenciaCardiaca());
        monitoreo.setApetito(requestDTO.getApetito());
        monitoreo.setObservaciones(requestDTO.getObservaciones());
        monitoreo.setHospitalizacion(hospitalizacion);
        monitoreo.setEmpleado(empleado);

        MonitoreoHospitalizacion monitoreoGuardado = monitoreoRepositorio.save(monitoreo);
        return mapearADTO(monitoreoGuardado);
    }

    @Transactional(readOnly = true)
    public List<MonitoreoHospitalizacionResponseDTO> listarPorHospitalizacion(Long hospitalizacionId) {
        if (!hospitalizacionRepositorio.existsById(hospitalizacionId)) {
            throw new EntityNotFoundException("Hospitalización no encontrada con ID: " + hospitalizacionId);
        }

        List<MonitoreoHospitalizacion> monitoreos = monitoreoRepositorio.findByHospitalizacionIdOrderByFechaHoraDesc(hospitalizacionId);
        return monitoreos.stream().map(this::mapearADTO).collect(Collectors.toList());
    }

    private MonitoreoHospitalizacionResponseDTO mapearADTO(MonitoreoHospitalizacion monitoreo) {
        MonitoreoHospitalizacionResponseDTO dto = new MonitoreoHospitalizacionResponseDTO();
        dto.setId(monitoreo.getId());
        dto.setFechaHora(monitoreo.getFechaHora());
        dto.setTemperatura(monitoreo.getTemperatura());
        dto.setFrecuenciaCardiaca(monitoreo.getFrecuenciaCardiaca());
        dto.setApetito(monitoreo.getApetito());
        dto.setObservaciones(monitoreo.getObservaciones());
        dto.setHospitalizacionId(monitoreo.getHospitalizacion().getId());
        dto.setEmpleadoId(monitoreo.getEmpleado().getId());
        dto.setEmpleadoNombre(monitoreo.getEmpleado().getUsuario().getNombre() + " " + monitoreo.getEmpleado().getUsuario().getApellido());
        return dto;
    }
}
