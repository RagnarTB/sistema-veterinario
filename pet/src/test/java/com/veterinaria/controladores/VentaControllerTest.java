package com.veterinaria.controladores;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.veterinaria.servicios.VentaServicio;
import com.veterinaria.dtos.VentaRequestDTO;
import com.veterinaria.dtos.VentaResponseDTO;
import com.veterinaria.dtos.DetalleVentaResponseDTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class VentaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private VentaServicio ventaServicio;

    @MockBean
    private com.veterinaria.servicios.EmpleadoAutenticadoService empleadoAutenticadoService;

    @Test
    void debeCrearVentaYRetornarEstadoCreated() throws Exception {
        // Mock del empleado autenticado
        com.veterinaria.modelos.Empleado empleadoMock = new com.veterinaria.modelos.Empleado();
        empleadoMock.setId(1L);
        when(empleadoAutenticadoService.obtenerEmpleadoActual()).thenReturn(empleadoMock);
        // JSON de ejemplo: una venta con un producto y un servicio médico
        // cantidad ahora acepta decimales (ej. 1.5 kg de alimento)
        String ventaJson = """
                {
                    "clienteId": 1,
                    "sedeId": 1,
                    "detalles": [
                        {
                            "productoId": 1,
                            "cantidad": 2.0
                        },
                        {
                            "servicioId": 5,
                            "cantidad": 1.0
                        }
                    ]
                }
                """;

        // cantidad es BigDecimal — usar new BigDecimal("2.00") en vez de literal int
        DetalleVentaResponseDTO detalle1 = new DetalleVentaResponseDTO(
                1L,
                null,
                "Shampoo Antipulgas",
                new BigDecimal("2.00"),   // cantidad BigDecimal
                new BigDecimal("20.00"),
                new BigDecimal("40.00"));

        DetalleVentaResponseDTO detalle2 = new DetalleVentaResponseDTO(
                null,
                5L,
                "Consulta Veterinaria",
                new BigDecimal("1.00"),   // cantidad BigDecimal
                new BigDecimal("35.00"),
                new BigDecimal("35.00"));

        VentaResponseDTO respuestaMock = new VentaResponseDTO(
                1L,
                1L,
                LocalDateTime.now(),
                new BigDecimal("75.00"),
                List.of(detalle1, detalle2));

        when(ventaServicio.guardar(any(VentaRequestDTO.class), any(com.veterinaria.modelos.Empleado.class))).thenReturn(respuestaMock);

        mockMvc.perform(post("/api/ventas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(ventaJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.total").value(75.00))
                .andExpect(jsonPath("$.detalles").isArray())
                .andExpect(jsonPath("$.detalles[0].nombreItem").value("Shampoo Antipulgas"))
                .andExpect(jsonPath("$.detalles[1].nombreItem").value("Consulta Veterinaria"));
    }
}