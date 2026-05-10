package com.veterinaria.servicios;

import com.veterinaria.dtos.CirugiaRequestDTO;
import com.veterinaria.dtos.CirugiaResponseDTO;
import com.veterinaria.modelos.Cirugia;
import com.veterinaria.modelos.Empleado;
import com.veterinaria.modelos.Hospitalizacion;
import com.veterinaria.modelos.Paciente;
import com.veterinaria.respositorios.CirugiaRepositorio;
import com.veterinaria.respositorios.EmpleadoRepositorio;
import com.veterinaria.respositorios.HospitalizacionRepositorio;
import com.veterinaria.respositorios.PacienteRepositorio;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CirugiaServicio {

    private final CirugiaRepositorio cirugiaRepositorio;
    private final PacienteRepositorio pacienteRepositorio;
    private final EmpleadoRepositorio empleadoRepositorio;
    private final HospitalizacionRepositorio hospitalizacionRepositorio;

    @Transactional
    public CirugiaResponseDTO guardar(CirugiaRequestDTO requestDTO) {
        Paciente paciente = pacienteRepositorio.findById(requestDTO.getPacienteId())
                .orElseThrow(() -> new EntityNotFoundException("Paciente no encontrado con ID: " + requestDTO.getPacienteId()));

        Empleado cirujano = empleadoRepositorio.findById(requestDTO.getCirujanoId())
                .orElseThrow(() -> new EntityNotFoundException("Cirujano no encontrado con ID: " + requestDTO.getCirujanoId()));

        Cirugia cirugia = new Cirugia();
        cirugia.setTipoCirugia(requestDTO.getTipoCirugia());
        cirugia.setFechaHoraFijada(requestDTO.getFechaHoraFijada());
        cirugia.setRiesgoOperatorio(requestDTO.getRiesgoOperatorio());
        cirugia.setEstado(requestDTO.getEstado());
        cirugia.setNotasPostOperatorias(requestDTO.getNotasPostOperatorias());
        cirugia.setPaciente(paciente);
        cirugia.setCirujano(cirujano);

        if (requestDTO.getHospitalizacionId() != null) {
            Hospitalizacion hospitalizacion = hospitalizacionRepositorio.findById(requestDTO.getHospitalizacionId())
                    .orElseThrow(() -> new EntityNotFoundException("Hospitalización no encontrada con ID: " + requestDTO.getHospitalizacionId()));
            cirugia.setHospitalizacion(hospitalizacion);
        }

        Cirugia guardada = cirugiaRepositorio.save(cirugia);
        return mapearADTO(guardada);
    }

    @Transactional
    public CirugiaResponseDTO actualizar(Long id, CirugiaRequestDTO requestDTO) {
        Cirugia cirugia = cirugiaRepositorio.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cirugía no encontrada con ID: " + id));

        Paciente paciente = pacienteRepositorio.findById(requestDTO.getPacienteId())
                .orElseThrow(() -> new EntityNotFoundException("Paciente no encontrado con ID: " + requestDTO.getPacienteId()));

        Empleado cirujano = empleadoRepositorio.findById(requestDTO.getCirujanoId())
                .orElseThrow(() -> new EntityNotFoundException("Cirujano no encontrado con ID: " + requestDTO.getCirujanoId()));

        cirugia.setTipoCirugia(requestDTO.getTipoCirugia());
        cirugia.setFechaHoraFijada(requestDTO.getFechaHoraFijada());
        cirugia.setRiesgoOperatorio(requestDTO.getRiesgoOperatorio());
        cirugia.setEstado(requestDTO.getEstado());
        cirugia.setNotasPostOperatorias(requestDTO.getNotasPostOperatorias());
        cirugia.setPaciente(paciente);
        cirugia.setCirujano(cirujano);

        if (requestDTO.getHospitalizacionId() != null) {
            Hospitalizacion hospitalizacion = hospitalizacionRepositorio.findById(requestDTO.getHospitalizacionId())
                    .orElseThrow(() -> new EntityNotFoundException("Hospitalización no encontrada con ID: " + requestDTO.getHospitalizacionId()));
            cirugia.setHospitalizacion(hospitalizacion);
        } else {
            cirugia.setHospitalizacion(null);
        }

        Cirugia actualizada = cirugiaRepositorio.save(cirugia);
        return mapearADTO(actualizada);
    }

    @Transactional
    public CirugiaResponseDTO cambiarEstado(Long id, String nuevoEstado) {
        Cirugia cirugia = cirugiaRepositorio.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cirugía no encontrada con ID: " + id));
        cirugia.setEstado(nuevoEstado);
        Cirugia actualizada = cirugiaRepositorio.save(cirugia);
        return mapearADTO(actualizada);
    }

    @Transactional(readOnly = true)
    public CirugiaResponseDTO obtenerPorId(Long id) {
        Cirugia cirugia = cirugiaRepositorio.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cirugía no encontrada con ID: " + id));
        return mapearADTO(cirugia);
    }

    @Transactional(readOnly = true)
    public List<CirugiaResponseDTO> listarTodas() {
        return cirugiaRepositorio.findAll().stream().map(this::mapearADTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CirugiaResponseDTO> listarPorPaciente(Long pacienteId) {
        return cirugiaRepositorio.findByPacienteIdOrderByFechaHoraFijadaDesc(pacienteId).stream()
                .map(this::mapearADTO)
                .collect(Collectors.toList());
    }

    private CirugiaResponseDTO mapearADTO(Cirugia cirugia) {
        CirugiaResponseDTO dto = new CirugiaResponseDTO();
        dto.setId(cirugia.getId());
        dto.setTipoCirugia(cirugia.getTipoCirugia());
        dto.setFechaHoraFijada(cirugia.getFechaHoraFijada());
        dto.setRiesgoOperatorio(cirugia.getRiesgoOperatorio());
        dto.setEstado(cirugia.getEstado());
        dto.setNotasPostOperatorias(cirugia.getNotasPostOperatorias());
        dto.setPacienteId(cirugia.getPaciente().getId());
        dto.setPacienteNombre(cirugia.getPaciente().getNombre());
        dto.setCirujanoId(cirugia.getCirujano().getId());
        dto.setCirujanoNombre(cirugia.getCirujano().getUsuario().getNombre() + " " + cirugia.getCirujano().getUsuario().getApellido());
        
        if (cirugia.getHospitalizacion() != null) {
            dto.setHospitalizacionId(cirugia.getHospitalizacion().getId());
        }
        return dto;
    }
}
