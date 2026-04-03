package com.veterinaria.servicios;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
// IMPORTANTE: Importamos el Contexto de Seguridad
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.veterinaria.dtos.AtencionMedicaRequestDTO;
import com.veterinaria.dtos.AtencionMedicaResponseDTO;
import com.veterinaria.modelos.AtencionMedica;
import com.veterinaria.modelos.Cita;
import com.veterinaria.modelos.Usuario;
import com.veterinaria.modelos.Enums.EstadoCita;
import com.veterinaria.respositorios.AtencionMedicaRepositorio;
import com.veterinaria.respositorios.CitaRepositorio;
import com.veterinaria.respositorios.UsuarioRepositorio;

@Service
public class AtencionMedicaServicio {

    private AtencionMedicaRepositorio atencionMedicaRepositorio;
    private CitaRepositorio citaRepositorio;
    private final UsuarioRepositorio usuarioRepositorio;

    public AtencionMedicaServicio(AtencionMedicaRepositorio atencionMedicaRepositorio,
            CitaRepositorio citaRepositorio, UsuarioRepositorio usuarioRepositorio) {
        this.atencionMedicaRepositorio = atencionMedicaRepositorio;
        this.citaRepositorio = citaRepositorio;
        this.usuarioRepositorio = usuarioRepositorio;
    }

    // CREATE
    public AtencionMedicaResponseDTO guardar(AtencionMedicaRequestDTO dto) {
        Cita cita = citaRepositorio.findById(dto.getCitaId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No se puede crear la atencion medica, cita no encontrada: " + dto.getCitaId()));

        if (cita.getEstado() == EstadoCita.COMPLETADA) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Esta cita ya tiene una historia clínica registrada.");
        }
        if (cita.getEstado() == EstadoCita.CANCELADA || cita.getEstado() == EstadoCita.NO_ASISTIO) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "No se puede atender a un paciente cuya cita fue cancelada o no asistió.");
        }

        // Obtenemos el email del doctor directamente del Token JWT que
        // usó para entrar
        String emailDoctorAutenticado = SecurityContextHolder.getContext().getAuthentication().getName();

        // Buscamos a ese doctor en la base de datos
        Usuario doctor = usuarioRepositorio.findByEmail(emailDoctorAutenticado)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no válido"));

        if (!cita.getVeterinario().getId().equals(doctor.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "No puedes registrar la atención médica de un paciente asignado a otro veterinario.");
        }

        AtencionMedica atencionMedica = new AtencionMedica();

        // CORRECCIÓN 1: Pasamos el objeto, no el ID
        atencionMedica.setCita(cita);
        atencionMedica.setVeterinario(doctor);
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
    public Page<AtencionMedicaResponseDTO> listarTodos(Pageable pageable) {
        return atencionMedicaRepositorio.findAll(pageable)
                .map(this::mapearADTO);
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
        // No actualizamos al doctor, porque el que creó la primera vez es el
        // responsable
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
        // AQUI: Enviamos el ID del doctor al Frontend
        if (entidad.getVeterinario() != null) {
            dto.setVeterinarioId(entidad.getVeterinario().getId());
        }
        return dto;
    }
}