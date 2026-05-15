package com.veterinaria.servicios;

import com.veterinaria.dtos.AtencionMedicaRequestDTO;
import com.veterinaria.dtos.AtencionMedicaResponseDTO;
import com.veterinaria.modelos.AtencionMedica;
import com.veterinaria.modelos.Cita;
import com.veterinaria.modelos.Empleado;
import com.veterinaria.modelos.Paciente;
import com.veterinaria.modelos.Enums.EstadoCita;
import com.veterinaria.respositorios.AtencionMedicaRepositorio;
import com.veterinaria.respositorios.CitaRepositorio;
import com.veterinaria.respositorios.EmpleadoRepositorio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AtencionMedicaServicioTest {

    @Mock
    private AtencionMedicaRepositorio atencionMedicaRepositorio;

    @Mock
    private CitaRepositorio citaRepositorio;

    @Mock
    private EmpleadoRepositorio empleadoRepositorio;

    @InjectMocks
    private AtencionMedicaServicio atencionMedicaServicio;

    private Cita cita;
    private Empleado doctor;
    private Paciente paciente;
    private AtencionMedicaRequestDTO requestDTO;
    private AtencionMedica atencionMedica;

    @BeforeEach
    void setUp() {
        com.veterinaria.modelos.Usuario doctorUsuario = new com.veterinaria.modelos.Usuario();
        doctorUsuario.setNombre("Dr. House");
        doctor = new Empleado();
        doctor.setId(10L);
        doctor.setUsuario(doctorUsuario);

        com.veterinaria.modelos.Usuario pacienteUsuario = new com.veterinaria.modelos.Usuario();
        pacienteUsuario.setNombre("Juan");
        paciente.setId(100L);
        paciente.setNombre("Boby");

        cita = new Cita();
        cita.setId(1L);
        cita.setEstado(EstadoCita.EN_CONSULTORIO);
        cita.setVeterinario(doctor);
        cita.setPacientes(List.of(paciente));

        requestDTO = new AtencionMedicaRequestDTO();
        requestDTO.setCitaId(1L);
        requestDTO.setPacienteId(100L);
        requestDTO.setDiagnostico("Saludable");
        requestDTO.setFrecuenciaCardiaca(80);
        requestDTO.setSintomas("Ninguno");

        atencionMedica = new AtencionMedica();
        atencionMedica.setId(50L);
        atencionMedica.setCita(cita);
        atencionMedica.setPaciente(paciente);
        atencionMedica.setVeterinario(doctor);
        atencionMedica.setDiagnostico("Saludable");
        atencionMedica.setFechaCreacion(LocalDateTime.now());
        
        // Mock Security Context (lenient because not all tests call security context)
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void guardar_Exito() {
        when(SecurityContextHolder.getContext().getAuthentication().getName()).thenReturn("doctor@vet.com");
        when(citaRepositorio.findById(1L)).thenReturn(Optional.of(cita));
        when(atencionMedicaRepositorio.existsByCitaIdAndPacienteId(1L, 100L)).thenReturn(false);
        when(empleadoRepositorio.findByUsuarioEmail("doctor@vet.com")).thenReturn(Optional.of(doctor));
        when(atencionMedicaRepositorio.countByCitaId(1L)).thenReturn(0);
        when(atencionMedicaRepositorio.save(any(AtencionMedica.class))).thenReturn(atencionMedica);

        AtencionMedicaResponseDTO response = atencionMedicaServicio.guardar(requestDTO);

        assertNotNull(response);
        assertEquals(50L, response.getId());
        assertEquals("Saludable", response.getDiagnostico());
        // Verify that cita state was changed to COMPLETADA since it was the only patient
        assertEquals(EstadoCita.COMPLETADA, cita.getEstado());
        verify(citaRepositorio, times(1)).save(cita);
    }

    @Test
    void guardar_FallaCitaCancelada() {
        cita.setEstado(EstadoCita.CANCELADA);
        when(citaRepositorio.findById(1L)).thenReturn(Optional.of(cita));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> atencionMedicaServicio.guardar(requestDTO));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertTrue(exception.getReason().contains("No se puede atender a un paciente cuya cita fue cancelada"));
    }

    @Test
    void guardar_FallaPacienteYaAtendido() {
        when(citaRepositorio.findById(1L)).thenReturn(Optional.of(cita));
        when(atencionMedicaRepositorio.existsByCitaIdAndPacienteId(1L, 100L)).thenReturn(true);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> atencionMedicaServicio.guardar(requestDTO));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertTrue(exception.getReason().contains("ya tiene una historia clínica registrada"));
    }

    @Test
    void guardar_FallaDoctorDiferente() {
        when(SecurityContextHolder.getContext().getAuthentication().getName()).thenReturn("otrodoctor@vet.com");
        when(citaRepositorio.findById(1L)).thenReturn(Optional.of(cita));
        when(atencionMedicaRepositorio.existsByCitaIdAndPacienteId(1L, 100L)).thenReturn(false);
        
        Empleado otroDoctor = new Empleado();
        otroDoctor.setId(20L); // different ID
        when(empleadoRepositorio.findByUsuarioEmail("otrodoctor@vet.com")).thenReturn(Optional.of(otroDoctor));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> atencionMedicaServicio.guardar(requestDTO));
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        assertTrue(exception.getReason().contains("No puedes registrar la atención médica de un paciente asignado a otro veterinario"));
    }
    
    @Test
    void actualizar_Excede24Horas() {
        atencionMedica.setFechaCreacion(LocalDateTime.now().minusHours(25));
        when(atencionMedicaRepositorio.findById(50L)).thenReturn(Optional.of(atencionMedica));
        
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> atencionMedicaServicio.actualizar(50L, requestDTO));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertTrue(exception.getReason().contains("No se puede modificar una historia clínica pasadas las 24 horas"));
    }
}
