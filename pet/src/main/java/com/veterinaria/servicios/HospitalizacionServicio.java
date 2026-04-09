package com.veterinaria.servicios;

import com.veterinaria.dtos.HospitalizacionRequestDTO;
import com.veterinaria.dtos.HospitalizacionResponseDTO;
import com.veterinaria.modelos.Empleado;
import com.veterinaria.modelos.Hospitalizacion;
import com.veterinaria.modelos.Jaula;
import com.veterinaria.modelos.Paciente;
import com.veterinaria.respositorios.EmpleadoRepositorio;
import com.veterinaria.respositorios.HospitalizacionRepositorio;
import com.veterinaria.respositorios.JaulaRepositorio;
import com.veterinaria.respositorios.PacienteRepositorio;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class HospitalizacionServicio {

    private final HospitalizacionRepositorio hospitalizacionRepositorio;
    private final PacienteRepositorio pacienteRepositorio;
    private final JaulaRepositorio jaulaRepositorio;
    private final EmpleadoRepositorio empleadoRepositorio;

    @Transactional
    public HospitalizacionResponseDTO ingresarPaciente(HospitalizacionRequestDTO requestDTO) {
        // Verificar si el paciente ya tiene una hospitalización activa
        hospitalizacionRepositorio.findByPacienteIdAndEstado(requestDTO.getPacienteId(), "ACTIVA")
                .ifPresent(h -> {
                    throw new IllegalStateException("El paciente ya se encuentra hospitalizado (Hospitalización ACTIVA).");
                });

        Paciente paciente = pacienteRepositorio.findById(requestDTO.getPacienteId())
                .orElseThrow(() -> new EntityNotFoundException("Paciente no encontrado con ID: " + requestDTO.getPacienteId()));

        Jaula jaula = jaulaRepositorio.findById(requestDTO.getJaulaId())
                .orElseThrow(() -> new EntityNotFoundException("Jaula no encontrada con ID: " + requestDTO.getJaulaId()));

        if (!"DISPONIBLE".equalsIgnoreCase(jaula.getEstado())) {
            throw new IllegalStateException("La jaula solicitada no está DISPONIBLE.");
        }

        Empleado empleado = empleadoRepositorio.findById(requestDTO.getEmpleadoId())
                .orElseThrow(() -> new EntityNotFoundException("Empleado (veterinario) no encontrado con ID: " + requestDTO.getEmpleadoId()));

        Hospitalizacion hospitalizacion = new Hospitalizacion();
        hospitalizacion.setMotivoIngreso(requestDTO.getMotivoIngreso());
        hospitalizacion.setFechaIngreso(requestDTO.getFechaIngreso());
        hospitalizacion.setEstado("ACTIVA");
        hospitalizacion.setPaciente(paciente);
        hospitalizacion.setJaula(jaula);
        hospitalizacion.setEmpleado(empleado);

        // Cambiar estado de la jaula
        jaula.setEstado("OCUPADA");
        jaulaRepositorio.save(jaula);

        Hospitalizacion guardada = hospitalizacionRepositorio.save(hospitalizacion);
        return mapearADTO(guardada);
    }

    @Transactional
    public HospitalizacionResponseDTO darDeAlta(Long hospitalizacionId) {
        Hospitalizacion hospitalizacion = hospitalizacionRepositorio.findById(hospitalizacionId)
                .orElseThrow(() -> new EntityNotFoundException("Hospitalización no encontrada con ID: " + hospitalizacionId));

        if (!"ACTIVA".equalsIgnoreCase(hospitalizacion.getEstado())) {
            throw new IllegalStateException("La hospitalización no está ACTIVA.");
        }

        hospitalizacion.setFechaAlta(LocalDateTime.now());
        hospitalizacion.setEstado("DADA_DE_ALTA");

        Jaula jaula = hospitalizacion.getJaula();
        jaula.setEstado("DISPONIBLE");
        jaulaRepositorio.save(jaula);

        Hospitalizacion actualizada = hospitalizacionRepositorio.save(hospitalizacion);
        return mapearADTO(actualizada);
    }

    @Transactional
    public HospitalizacionResponseDTO trasladarPaciente(Long hospitalizacionId, Long nuevaJaulaId) {
        Hospitalizacion hospitalizacion = hospitalizacionRepositorio.findById(hospitalizacionId)
                .orElseThrow(() -> new EntityNotFoundException("Hospitalización no encontrada con ID: " + hospitalizacionId));

        if (!"ACTIVA".equalsIgnoreCase(hospitalizacion.getEstado())) {
            throw new IllegalStateException("Solo puede trasladar a pacientes con hospitalización ACTIVA.");
        }

        Jaula nuevaJaula = jaulaRepositorio.findById(nuevaJaulaId)
                .orElseThrow(() -> new EntityNotFoundException("Jaula no encontrada con ID: " + nuevaJaulaId));

        if (!"DISPONIBLE".equalsIgnoreCase(nuevaJaula.getEstado())) {
            throw new IllegalStateException("La nueva jaula solicitada no está DISPONIBLE.");
        }

        // Liberar jaula actual
        Jaula jaulaActual = hospitalizacion.getJaula();
        jaulaActual.setEstado("DISPONIBLE");
        jaulaRepositorio.save(jaulaActual);

        // Ocupar nueva jaula
        nuevaJaula.setEstado("OCUPADA");
        jaulaRepositorio.save(nuevaJaula);

        // Actualizar hospitalización
        hospitalizacion.setJaula(nuevaJaula);
        Hospitalizacion actualizada = hospitalizacionRepositorio.save(hospitalizacion);
        return mapearADTO(actualizada);
    }

    @Transactional
    public HospitalizacionResponseDTO registrarFallecimiento(Long hospitalizacionId) {
        Hospitalizacion hospitalizacion = hospitalizacionRepositorio.findById(hospitalizacionId)
                .orElseThrow(() -> new EntityNotFoundException("Hospitalización no encontrada con ID: " + hospitalizacionId));

        if (!"ACTIVA".equalsIgnoreCase(hospitalizacion.getEstado())) {
            throw new IllegalStateException("La hospitalización no está ACTIVA.");
        }

        hospitalizacion.setFechaAlta(LocalDateTime.now());
        hospitalizacion.setEstado("FALLECIDO");

        Jaula jaula = hospitalizacion.getJaula();
        jaula.setEstado("DISPONIBLE");
        jaulaRepositorio.save(jaula);

        Hospitalizacion actualizada = hospitalizacionRepositorio.save(hospitalizacion);
        return mapearADTO(actualizada);
    }

    private HospitalizacionResponseDTO mapearADTO(Hospitalizacion hospitalizacion) {
        HospitalizacionResponseDTO dto = new HospitalizacionResponseDTO();
        dto.setId(hospitalizacion.getId());
        dto.setMotivoIngreso(hospitalizacion.getMotivoIngreso());
        dto.setFechaIngreso(hospitalizacion.getFechaIngreso());
        dto.setFechaAlta(hospitalizacion.getFechaAlta());
        dto.setEstado(hospitalizacion.getEstado());
        dto.setPacienteId(hospitalizacion.getPaciente().getId());
        dto.setPacienteNombre(hospitalizacion.getPaciente().getNombre());
        dto.setJaulaId(hospitalizacion.getJaula().getId());
        dto.setJaulaNumero(hospitalizacion.getJaula().getNumero());
        dto.setEmpleadoId(hospitalizacion.getEmpleado().getId());
        dto.setEmpleadoNombre(hospitalizacion.getEmpleado().getNombre() + " " + hospitalizacion.getEmpleado().getApellido());
        return dto;
    }
}
