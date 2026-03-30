package com.veterinaria.servicios;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.veterinaria.dtos.AtencionMedicaRequestDTO;
import com.veterinaria.dtos.AtencionMedicaResponseDTO;
import com.veterinaria.modelos.AtencionMedica;
import com.veterinaria.modelos.Cita;
import com.veterinaria.modelos.EstadoCita;
import com.veterinaria.respositorios.AtencionMedicaRepositorio;
import com.veterinaria.respositorios.CitaRepositorio;

@Service
public class AtencionMedicaServicio {

    private AtencionMedicaRepositorio atencionMedicaRepositorio;
    private CitaRepositorio citaRepositorio;

    public AtencionMedicaServicio(AtencionMedicaRepositorio atencionMedicaRepositorio,
            CitaRepositorio citaRepositorio) {
        this.atencionMedicaRepositorio = atencionMedicaRepositorio;
        this.citaRepositorio = citaRepositorio;
    }

    // CREATE
    public AtencionMedicaResponseDTO guardar(AtencionMedicaRequestDTO dto) {
        Cita cita = citaRepositorio.findById(dto.getCitaId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No se puede crear la atencion medica, cita no encontrada: " + dto.getCitaId()));

        AtencionMedica atencionMedica = new AtencionMedica();

        // CORRECCIÓN 1: Pasamos el objeto, no el ID
        atencionMedica.setCita(cita);

        atencionMedica.setDiagnostico(dto.getDiagnostico());
        atencionMedica.setFrecuenciaCardiaca(dto.getFrecuenciaCardiaca());
        atencionMedica.setPeso(dto.getPeso());
        atencionMedica.setSintomas(dto.getSintomas());
        atencionMedica.setTemperatura(dto.getTemperatura());
        atencionMedica.setTratamiento(dto.getTratamiento());

        // REGLA DE NEGOCIO: La cita ya fue atendida, cambia su estado
        cita.setEstado(EstadoCita.COMPLETADA);

        AtencionMedica atencionGuardada = atencionMedicaRepositorio.save(atencionMedica);

        return mapearADTO(atencionGuardada); // Uso un método privado para no repetir código
    }

    // READ (Listar Todos)
    public List<AtencionMedicaResponseDTO> listarTodos() {
        List<AtencionMedica> atenciones = atencionMedicaRepositorio.findAll();
        return atenciones.stream()
                .map(this::mapearADTO)
                .collect(Collectors.toList());
    }

    // READ (Buscar por ID)
    public AtencionMedicaResponseDTO buscarPorId(Long id) {
        return atencionMedicaRepositorio.findById(id)
                .map(this::mapearADTO)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Atención médica no encontrada con ID: " + id));
    }

    // UPDATE
    public AtencionMedicaResponseDTO actualizar(Long id, AtencionMedicaRequestDTO dto) {
        AtencionMedica atencionDb = atencionMedicaRepositorio.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Atención médica no encontrada con ID: " + id));

        // Actualizamos solo los datos médicos (generalmente la Cita ID no cambia una
        // vez atendida)
        atencionDb.setDiagnostico(dto.getDiagnostico());
        atencionDb.setFrecuenciaCardiaca(dto.getFrecuenciaCardiaca());
        atencionDb.setPeso(dto.getPeso());
        atencionDb.setSintomas(dto.getSintomas());
        atencionDb.setTemperatura(dto.getTemperatura());
        atencionDb.setTratamiento(dto.getTratamiento());

        AtencionMedica atencionGuardada = atencionMedicaRepositorio.save(atencionDb);
        return mapearADTO(atencionGuardada);
    }

    // DELETE
    public void eliminar(Long id) {
        AtencionMedica atencionDb = atencionMedicaRepositorio.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Atención médica no encontrada con ID: " + id));
        atencionMedicaRepositorio.delete(atencionDb);
    }

    // Método Auxiliar (DRY - Don't Repeat Yourself) para transformar Entidad a DTO
    private AtencionMedicaResponseDTO mapearADTO(AtencionMedica entidad) {
        AtencionMedicaResponseDTO dto = new AtencionMedicaResponseDTO();
        dto.setId(entidad.getId());
        dto.setDiagnostico(entidad.getDiagnostico());
        dto.setFrecuenciaCardiaca(entidad.getFrecuenciaCardiaca());
        dto.setPeso(entidad.getPeso());
        dto.setSintomas(entidad.getSintomas());
        dto.setTemperatura(entidad.getTemperatura());
        dto.setTratamiento(entidad.getTratamiento());
        dto.setResumenIaCliente(entidad.getResumenIaCliente());
        // CORRECCIÓN 2: Incluimos el ID de la cita en la respuesta
        dto.setCitaId(entidad.getCita().getId());
        return dto;
    }
}