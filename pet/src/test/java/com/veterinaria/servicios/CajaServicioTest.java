package com.veterinaria.servicios;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.veterinaria.dtos.CajaRequestDTO;
import com.veterinaria.dtos.CierreCajaResponseDTO;
import com.veterinaria.modelos.CajaDiaria;
import com.veterinaria.modelos.Empleado;
import com.veterinaria.modelos.Sede;
import com.veterinaria.respositorios.CajaRepositorio;
import com.veterinaria.respositorios.SedeRepositorio;
import com.veterinaria.respositorios.VentaRepositorio;

@ExtendWith(MockitoExtension.class)
class CajaServicioTest {

    @Mock
    private CajaRepositorio cajaRepositorio;

    @Mock
    private VentaRepositorio ventaRepositorio;

    @Mock
    private SedeRepositorio sedeRepositorio;

    @InjectMocks
    private CajaServicio cajaServicio;

    @Test
    void debeAbrirCajaYGuardarEnBaseDeDatos() {
        CajaRequestDTO request = new CajaRequestDTO();
        request.setSaldoInicial(new BigDecimal("150.00"));
        request.setSedeId(1L);

        Sede sede = new Sede();
        sede.setId(1L);

        Empleado empleado = new Empleado();
        empleado.setId(1L);
        empleado.setSedes(new HashSet<>(Set.of(sede)));

        when(sedeRepositorio.findById(1L)).thenReturn(Optional.of(sede));
        when(cajaRepositorio.findByEmpleadoIdAndEstado(1L, "ABIERTA"))
                .thenReturn(Optional.empty());

        cajaServicio.abrirCaja(request, empleado);

        ArgumentCaptor<CajaDiaria> cajaCaptor = ArgumentCaptor.forClass(CajaDiaria.class);

        verify(cajaRepositorio).save(cajaCaptor.capture());

        CajaDiaria cajaGuardada = cajaCaptor.getValue();

        assertEquals(new BigDecimal("150.00"), cajaGuardada.getSaldoInicial());
        assertEquals("ABIERTA", cajaGuardada.getEstado());
        assertNotNull(cajaGuardada.getFechaApertura());
        assertEquals(sede, cajaGuardada.getSede());
        assertEquals(empleado, cajaGuardada.getEmpleado());
    }

    @Test
    void debeCerrarCajaYCalcularSaldoFinal() {
        Sede sede = new Sede();
        sede.setId(1L);

        Empleado empleado = new Empleado();
        empleado.setId(1L);
        empleado.setSedes(new HashSet<>(Set.of(sede)));

        CajaDiaria cajaAbierta = new CajaDiaria();
        cajaAbierta.setId(1L);
        cajaAbierta.setSaldoInicial(new BigDecimal("100.00"));
        cajaAbierta.setEstado("ABIERTA");
        cajaAbierta.setFechaApertura(LocalDateTime.now().minusHours(8));
        cajaAbierta.setMovimientos(new ArrayList<>());
        cajaAbierta.setSede(sede);
        cajaAbierta.setEmpleado(empleado);

        when(sedeRepositorio.findById(1L)).thenReturn(Optional.of(sede));
        when(cajaRepositorio.findByEmpleadoIdAndSedeIdAndEstado(1L, 1L, "ABIERTA"))
                .thenReturn(Optional.of(cajaAbierta));

        when(ventaRepositorio.sumarVentasPorCaja(1L))
                .thenReturn(new BigDecimal("250.00"));

        CierreCajaResponseDTO response = cajaServicio.cerrarCaja(1L, empleado);

        ArgumentCaptor<CajaDiaria> cajaCaptor = ArgumentCaptor.forClass(CajaDiaria.class);

        verify(cajaRepositorio).save(cajaCaptor.capture());

        CajaDiaria cajaCerrada = cajaCaptor.getValue();

        assertEquals("CERRADA", cajaCerrada.getEstado());
        assertNotNull(cajaCerrada.getFechaCierre());
        assertEquals(new BigDecimal("350.00"), cajaCerrada.getSaldoFinal());

        // Verificamos también el DTO de respuesta
        assertNotNull(response);
        assertEquals(new BigDecimal("350.00"), response.getSaldoFinal());
        assertEquals(new BigDecimal("250.00"), response.getTotalVentas());
    }
}