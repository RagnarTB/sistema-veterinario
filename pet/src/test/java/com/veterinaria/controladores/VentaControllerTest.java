package com.veterinaria.controladores;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.veterinaria.servicios.VentaServicio; // Importante para el MockBean

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = VentaController.class, excludeAutoConfiguration = { SecurityAutoConfiguration.class })
class VentaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private VentaServicio ventaServicio;

    @Test
    void debeCrearVentaYRetornarEstadoCreated() throws Exception {
        // Mira cómo Angular envía solo lo estrictamente necesario.
        // Cero precios, cero totales. El Backend manda aquí.
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

        mockMvc.perform(post("/api/ventas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(ventaJson))
                .andExpect(status().isCreated());
    }
}