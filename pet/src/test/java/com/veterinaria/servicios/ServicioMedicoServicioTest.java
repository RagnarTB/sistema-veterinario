package com.veterinaria.servicios;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import com.veterinaria.dtos.ServicioMedicoRequestDTO;
import com.veterinaria.dtos.ServicioMedicoResponseDTO;
import com.veterinaria.modelos.ServicioMedico;
import com.veterinaria.modelos.TipoServicio;
import com.veterinaria.respositorios.ServicioMedicoRepositorio;

@ExtendWith(MockitoExtension.class)
class ServicioMedicoServicioTest {

    @Mock
    private ServicioMedicoRepositorio servicioRepositorio;

    @InjectMocks
    private ServicioMedicoServicio servicioMedicoServicio;

    private ServicioMedicoRequestDTO requestDTO;
    private ServicioMedico servicio;

    @BeforeEach
    void setUp() {
        requestDTO = new ServicioMedicoRequestDTO();
        requestDTO.setNombre("Consulta General");
        requestDTO.setPrecio(new BigDecimal("50.00"));
        requestDTO.setDuracionMinutos(30);
        requestDTO.setBufferMinutos(10);
        requestDTO.setTipoServicio(TipoServicio.CONSULTA);

        servicio = new ServicioMedico();
        servicio.setId(1L);
        servicio.setNombre("Consulta General");
        servicio.setPrecio(new BigDecimal("50.00"));
        servicio.setDuracionMinutos(30);
        servicio.setBufferMinutos(10);
        servicio.setTipoServicio(TipoServicio.CONSULTA);
        servicio.setActivo(true);
    }

    @Test
    void guardar_Exito() {
        when(servicioRepositorio.existsByNombreIgnoreCase(anyString())).thenReturn(false);
        when(servicioRepositorio.save(any(ServicioMedico.class))).thenReturn(servicio);

        ServicioMedicoResponseDTO response = servicioMedicoServicio.guardar(requestDTO);

        assertNotNull(response);
        assertEquals("Consulta General", response.getNombre());
        verify(servicioRepositorio).save(any(ServicioMedico.class));
    }

    @Test
    void guardar_ErrorNombreDuplicado() {
        when(servicioRepositorio.existsByNombreIgnoreCase(anyString())).thenReturn(true);

        assertThrows(ResponseStatusException.class, () -> {
            servicioMedicoServicio.guardar(requestDTO);
        });
        
        verify(servicioRepositorio, never()).save(any(ServicioMedico.class));
    }

    @Test
    void actualizar_Exito() {
        when(servicioRepositorio.findById(1L)).thenReturn(Optional.of(servicio));
        when(servicioRepositorio.existsByNombreIgnoreCaseAndIdNot(anyString(), anyLong())).thenReturn(false);
        when(servicioRepositorio.save(any(ServicioMedico.class))).thenReturn(servicio);

        ServicioMedicoResponseDTO response = servicioMedicoServicio.actualizar(1L, requestDTO);

        assertNotNull(response);
        verify(servicioRepositorio).save(any(ServicioMedico.class));
    }

    @Test
    void cambiarEstado_Exito() {
        when(servicioRepositorio.findById(1L)).thenReturn(Optional.of(servicio));
        
        servicioMedicoServicio.cambiarEstado(1L, false);
        
        assertFalse(servicio.getActivo());
        verify(servicioRepositorio).save(servicio);
    }
}
