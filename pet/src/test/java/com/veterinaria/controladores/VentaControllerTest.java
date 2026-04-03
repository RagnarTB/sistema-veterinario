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

    @Test
    void debeCrearVentaYRetornarEstadoCreated() throws Exception {
        String ventaJson = """
                {
                    "clienteId": 1,
                    "detalles": [
                        {
                            "productoId": 1,
                            "cantidad": 2
                        },
                        {
                            "productoId": 3,
                            "cantidad": 1
                        }
                    ]
                }
                """;

        // 1. Preparamos los detalles de respuesta simulados
        DetalleVentaResponseDTO detalle1 = new DetalleVentaResponseDTO(1L, "Shampoo", 2, 20.0, 40.0);
        DetalleVentaResponseDTO detalle2 = new DetalleVentaResponseDTO(3L, "Correa", 1, 15.0, 15.0);

        // 2. Preparamos la respuesta de la venta simulada
        VentaResponseDTO respuestaMock = new VentaResponseDTO(
                1L,
                1L,
                LocalDateTime.now(),
                55.0,
                List.of(detalle1, detalle2));

        when(ventaServicio.guardar(any(VentaRequestDTO.class))).thenReturn(respuestaMock);

        // 3. Ejecutamos y verificamos los datos
        mockMvc.perform(post("/api/ventas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(ventaJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.total").value(55.0))
                .andExpect(jsonPath("$.detalles").isArray());
    }
}