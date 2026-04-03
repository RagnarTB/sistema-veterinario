package com.veterinaria.servicios;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.veterinaria.dtos.CajaRequestDTO;
import com.veterinaria.modelos.CajaDiaria;
import com.veterinaria.respositorios.CajaRepositorio;

// ¡SPOILER! Este import va a fallar porque el repositorio de ventas aún no tiene la sumatoria
import com.veterinaria.respositorios.VentaRepositorio;

@ExtendWith(MockitoExtension.class)
class CajaServicioTest {

    @Mock
    private CajaRepositorio cajaRepositorio;

    @Mock
    private VentaRepositorio ventaRepositorio; // ¡NUEVO! Necesitamos preguntar por las ventas

    @InjectMocks
    private CajaServicio cajaServicio;

    @Test
    void debeAbrirCajaYGuardarEnBaseDeDatos() {
        CajaRequestDTO request = new CajaRequestDTO();
        request.setSaldoInicial(150.0);

        cajaServicio.abrirCaja(request);

        ArgumentCaptor<CajaDiaria> cajaCaptor = ArgumentCaptor.forClass(CajaDiaria.class);
        verify(cajaRepositorio).save(cajaCaptor.capture());

        CajaDiaria cajaGuardada = cajaCaptor.getValue();

        assertEquals(150.0, cajaGuardada.getSaldoInicial());
        assertEquals("ABIERTA", cajaGuardada.getEstado());
        assertNotNull(cajaGuardada.getFechaApertura());
    }

    // --- ¡NUESTRO NUEVO TEST ESTRELLA! ---
    @Test
    void debeCerrarCajaYCalcularSaldoFinal() {
        // 1. Preparamos una caja ficticia que se abrió hace 8 horas con 100 dólares
        CajaDiaria cajaAbierta = new CajaDiaria();
        cajaAbierta.setId(1L);
        cajaAbierta.setSaldoInicial(100.0);
        cajaAbierta.setEstado("ABIERTA");
        cajaAbierta.setFechaApertura(LocalDateTime.now().minusHours(8));

        cajaAbierta.setMovimientos(new java.util.ArrayList<>());

        when(cajaRepositorio.findByEstado("ABIERTA")).thenReturn(Optional.of(cajaAbierta));

        // 2. Simulamos que el sistema calculó que hoy se vendieron 250 dólares
        when(ventaRepositorio.sumarVentasPorCaja(any())).thenReturn(250.0);

        // 3. El administrador oprime el botón de "Cerrar Caja"
        cajaServicio.cerrarCaja();

        // 4. Capturamos qué se guardó en la base de datos
        ArgumentCaptor<CajaDiaria> cajaCaptor = ArgumentCaptor.forClass(CajaDiaria.class);
        verify(cajaRepositorio).save(cajaCaptor.capture());

        CajaDiaria cajaCerrada = cajaCaptor.getValue();

        // 5. ¡LA VERDADERA PRUEBA!
        assertEquals("CERRADA", cajaCerrada.getEstado());
        assertNotNull(cajaCerrada.getFechaCierre());
        // Matemáticas: 100 (Inicial) + 250 (Ventas) = 350
        assertEquals(350.0, cajaCerrada.getSaldoFinal(),
                "El saldo final debe ser la suma exacta del inicial más las ventas");
    }
}