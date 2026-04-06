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
import com.veterinaria.respositorios.MovimientoCajaRespositorio;
import com.veterinaria.respositorios.ProductoRepositorio;
import com.veterinaria.respositorios.ServicioMedicoRepositorio;
import com.veterinaria.respositorios.VentaRepositorio;

@ExtendWith(MockitoExtension.class)
class VentaServicioTest {

    @Mock
    private VentaRepositorio ventaRepositorio;
    @Mock
    private ClienteRepositorio clienteRepositorio;
    @Mock
    private ProductoRepositorio productoRepositorio;
    @Mock
    private ServicioMedicoRepositorio servicioMedicoRepositorio; // Nuevo repositorio inyectado
    @Mock
    private CajaRepositorio cajaRepositorio;
    @Mock
    private MovimientoCajaRespositorio movimientoCajaRespositorio;

    @InjectMocks
    private VentaServicio ventaServicio;

    @Test
    void debeLanzarErrorAlVenderSiCajaEstaCerrada() {
        VentaRequestDTO request = new VentaRequestDTO();
        request.setClienteId(1L);
        request.setSedeId(1L);

        com.veterinaria.modelos.Empleado empleado = new com.veterinaria.modelos.Empleado();
        empleado.setId(1L);

        // Simulamos que no hay caja ABIERTA personal para este empleado en esta sede
        when(cajaRepositorio.findByEmpleadoIdAndSedeIdAndEstado(1L, 1L, "ABIERTA")).thenReturn(Optional.empty());

        // El sistema DEBE lanzar excepción si la caja está cerrada
        assertThrows(ResponseStatusException.class, () -> {
            ventaServicio.guardar(request, empleado);
        }, "Debería lanzar error porque la caja está cerrada");
    }
}