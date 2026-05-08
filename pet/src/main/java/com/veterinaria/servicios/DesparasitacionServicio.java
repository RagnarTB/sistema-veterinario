package com.veterinaria.servicios;

import com.veterinaria.dtos.DesparasitacionRequestDTO;
import com.veterinaria.dtos.DesparasitacionResponseDTO;
import com.veterinaria.modelos.Desparasitacion;
import com.veterinaria.modelos.Empleado;
import com.veterinaria.modelos.Paciente;
import com.veterinaria.respositorios.DesparasitacionRepositorio;
import com.veterinaria.respositorios.EmpleadoRepositorio;
import com.veterinaria.respositorios.PacienteRepositorio;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DesparasitacionServicio {

    private final DesparasitacionRepositorio desparasitacionRepositorio;
    private final PacienteRepositorio pacienteRepositorio;
    private final EmpleadoRepositorio empleadoRepositorio;

    @Transactional
    public DesparasitacionResponseDTO guardar(DesparasitacionRequestDTO requestDTO) {
        Paciente paciente = pacienteRepositorio.findById(requestDTO.getPacienteId())
                .orElseThrow(() -> new EntityNotFoundException("Paciente no encontrado con ID: " + requestDTO.getPacienteId()));

        Empleado empleado = empleadoRepositorio.findById(requestDTO.getEmpleadoId())
                .orElseThrow(() -> new EntityNotFoundException("Empleado no encontrado con ID: " + requestDTO.getEmpleadoId()));

        Desparasitacion desparasitacion = new Desparasitacion();
        desparasitacion.setTipo(requestDTO.getTipo());
        desparasitacion.setProductoUtilizado(requestDTO.getProductoUtilizado());
        desparasitacion.setPesoAlMomento(requestDTO.getPesoAlMomento());
        desparasitacion.setFechaAplicacion(requestDTO.getFechaAplicacion());
        desparasitacion.setFechaProximaDosis(requestDTO.getFechaProximaDosis());
        desparasitacion.setObservaciones(requestDTO.getObservaciones());
        desparasitacion.setPaciente(paciente);
        desparasitacion.setEmpleado(empleado);

        Desparasitacion desparasitacionGuardada = desparasitacionRepositorio.save(desparasitacion);
        return mapearADTO(desparasitacionGuardada);
    }

    @Transactional(readOnly = true)
    public List<DesparasitacionResponseDTO> listarPorPaciente(Long pacienteId) {
        if (!pacienteRepositorio.existsById(pacienteId)) {
            throw new EntityNotFoundException("Paciente no encontrado con ID: " + pacienteId);
        }
        List<Desparasitacion> desparasitaciones = desparasitacionRepositorio.findByPacienteIdOrderByFechaAplicacionDesc(pacienteId);
        return desparasitaciones.stream().map(this::mapearADTO).collect(Collectors.toList());
    }

    private DesparasitacionResponseDTO mapearADTO(Desparasitacion desparasitacion) {
        DesparasitacionResponseDTO dto = new DesparasitacionResponseDTO();
        dto.setId(desparasitacion.getId());
        dto.setTipo(desparasitacion.getTipo());
        dto.setProductoUtilizado(desparasitacion.getProductoUtilizado());
        dto.setPesoAlMomento(desparasitacion.getPesoAlMomento());
        dto.setFechaAplicacion(desparasitacion.getFechaAplicacion());
        dto.setFechaProximaDosis(desparasitacion.getFechaProximaDosis());
        dto.setObservaciones(desparasitacion.getObservaciones());
        dto.setPacienteId(desparasitacion.getPaciente().getId()); // Note: Paciente entity has private Long Id; getter might be getId() or getId(), assuming getId() because of lombok @Data.
        dto.setPacienteNombre(desparasitacion.getPaciente().getNombre());
        dto.setEmpleadoId(desparasitacion.getEmpleado().getId());
        dto.setEmpleadoNombre(desparasitacion.getEmpleado().getUsuario().getNombre() + " " + desparasitacion.getEmpleado().getUsuario().getApellido());
        return dto;
    }
}
