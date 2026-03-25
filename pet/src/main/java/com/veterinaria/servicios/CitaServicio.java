package com.veterinaria.servicios;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.server.ResponseStatusException;

import com.veterinaria.dtos.CitaRequestDTO;
import com.veterinaria.dtos.CitaResponseDTO;
import com.veterinaria.modelos.Cita;
import com.veterinaria.modelos.EstadoCita;
import com.veterinaria.modelos.Paciente;
import com.veterinaria.respositorios.CitaRepositorio;
import com.veterinaria.respositorios.PacienteRepositorio;

@Service
public class CitaServicio {

    private CitaRepositorio citaRepositorio;
    private final PacienteRepositorio pacienteRepositorio;

    public CitaServicio(CitaRepositorio citaRepositorio, PacienteRepositorio pacienteRepositorio) {
        this.citaRepositorio = citaRepositorio;
        this.pacienteRepositorio = pacienteRepositorio;
    }

    public CitaResponseDTO guardar(CitaRequestDTO dto) {
        Paciente paciente = pacienteRepositorio.findById(dto.getPacienteId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No se puede crear la cita, paciente no encontrado : " + dto.getPacienteId())

                );

        Cita cita = new Cita();
        cita.setEstado(EstadoCita.PENDIENTE);
        cita.setFecha(dto.getFecha());
        cita.setHora(dto.getHora());
        cita.setMotivo(dto.getMotivo());
        cita.setPaciente(paciente);

        Cita citaGuardada = citaRepositorio.save(cita);

        CitaResponseDTO respuesta = new CitaResponseDTO();
        respuesta.setId(citaGuardada.getId());
        respuesta.setEstado(citaGuardada.getEstado());
        respuesta.setFecha(citaGuardada.getFecha());
        respuesta.setHora(citaGuardada.getHora());
        respuesta.setMotivo(citaGuardada.getMotivo());
        respuesta.setPacienteId(paciente.getId());
        return respuesta;

    }

}
