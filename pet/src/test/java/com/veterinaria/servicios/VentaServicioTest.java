package com.veterinaria.servicios;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import com.veterinaria.dtos.VentaRequestDTO;
import com.veterinaria.respositorios.CajaRepositorio;
import com.veterinaria.respositorios.ClienteRepositorio;
import com.veterinaria.respositorios.ProductoRepositorio;
import com.veterinaria.respositorios.VentaRepositorio;

@ExtendWith(MockitoExtension.class)
class VentaServicioTest {

    // Simulamos todas las dependencias que usa una Venta
    @Mock
    private VentaRepositorio ventaRepositorio;
    @Mock
    private ClienteRepositorio clienteRepositorio;
    @Mock
    private ProductoRepositorio productoRepositorio;

    // ¡NUESTRO NUEVO ACTOR!
    @Mock
    private CajaRepositorio cajaRepositorio;

    @InjectMocks
    private VentaServicio ventaServicio;

    @Test
    void debeLanzarErrorAlVenderSiCajaEstaCerrada() {
        VentaRequestDTO request = new VentaRequestDTO();
        request.setClienteId(1L);

        // Simulamos que la base de datos dice: "No hay ninguna caja ABIERTA hoy"
        when(cajaRepositorio.findByEstado("ABIERTA")).thenReturn(Optional.empty());

        // AFIRMAMOS: El sistema DEBE lanzar una excepción y detener la venta
        assertThrows(ResponseStatusException.class, () -> {
            ventaServicio.guardar(request);
        }, "Debería lanzar error porque la caja está cerrada");
    }
}