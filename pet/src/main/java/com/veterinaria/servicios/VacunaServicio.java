package com.veterinaria.servicios;

import com.veterinaria.dtos.VacunaRequestDTO;
import com.veterinaria.dtos.VacunaResponseDTO;
import com.veterinaria.modelos.Empleado;
import com.veterinaria.modelos.Paciente;
import com.veterinaria.modelos.Vacuna;
import com.veterinaria.respositorios.EmpleadoRepositorio;
import com.veterinaria.respositorios.PacienteRepositorio;
import com.veterinaria.respositorios.VacunaRepositorio;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VacunaServicio {

    private final VacunaRepositorio vacunaRepositorio;
    private final PacienteRepositorio pacienteRepositorio;
    private final EmpleadoRepositorio empleadoRepositorio;

    @Transactional
    public VacunaResponseDTO guardar(VacunaRequestDTO requestDTO) {
        Paciente paciente = pacienteRepositorio.findById(requestDTO.getPacienteId())
                .orElseThrow(() -> new EntityNotFoundException("Paciente no encontrado con ID: " + requestDTO.getPacienteId()));

        Empleado empleado = empleadoRepositorio.findById(requestDTO.getEmpleadoId())
                .orElseThrow(() -> new EntityNotFoundException("Empleado no encontrado con ID: " + requestDTO.getEmpleadoId()));

        Vacuna vacuna = new Vacuna();
        vacuna.setNombreVacuna(requestDTO.getNombreVacuna());
        vacuna.setFechaAplicacion(requestDTO.getFechaAplicacion());
        vacuna.setFechaProximaDosis(requestDTO.getFechaProximaDosis());
        vacuna.setObservaciones(requestDTO.getObservaciones());
        vacuna.setPaciente(paciente);
        vacuna.setEmpleado(empleado);

        Vacuna vacunaGuardada = vacunaRepositorio.save(vacuna);
        return mapearADTO(vacunaGuardada);
    }

    @Transactional(readOnly = true)
    public List<VacunaResponseDTO> listarPorPaciente(Long pacienteId) {
        if (!pacienteRepositorio.existsById(pacienteId)) {
            throw new EntityNotFoundException("Paciente no encontrado con ID: " + pacienteId);
        }
        List<Vacuna> vacunas = vacunaRepositorio.findByPacienteIdOrderByFechaAplicacionDesc(pacienteId);
        return vacunas.stream().map(this::mapearADTO).collect(Collectors.toList());
    }

    private VacunaResponseDTO mapearADTO(Vacuna vacuna) {
        VacunaResponseDTO dto = new VacunaResponseDTO();
        dto.setId(vacuna.getId());
        dto.setNombreVacuna(vacuna.getNombreVacuna());
        dto.setFechaAplicacion(vacuna.getFechaAplicacion());
        dto.setFechaProximaDosis(vacuna.getFechaProximaDosis());
        dto.setObservaciones(vacuna.getObservaciones());
        dto.setPacienteId(vacuna.getPaciente().getId());
        dto.setPacienteNombre(vacuna.getPaciente().getNombre());
        dto.setEmpleadoId(vacuna.getEmpleado().getId());
        dto.setEmpleadoNombre(vacuna.getEmpleado().getUsuario().getNombre() + " " + vacuna.getEmpleado().getUsuario().getApellido());
        return dto;
    }
}
