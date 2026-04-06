package com.veterinaria.controladores;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.veterinaria.servicios.ProductoServicio;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class ProductoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductoServicio productoServicio;

    @Test
    void debeCrearProductoYRetornarEstadoCorrecto() throws Exception {
        // stockMinimo es ahora un campo obligatorio en el DTO
        String productoJson = """
                {
                    "nombre": "Shampoo Antipulgas",
                    "descripcion": "Shampoo para perros de todas las razas",
                    "precio": 45.50,
                    "stockActual": 20,
                    "stockMinimo": 5
                }
                """;

        mockMvc.perform(post("/api/productos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(productoJson))
                .andExpect(status().isCreated());
    }

    @Test
    void debeRetornarBadRequestCuandoPrecioEsNegativo() throws Exception {
        String productoInvalidoJson = """
                {
                    "nombre": "Shampoo Antipulgas",
                    "descripcion": "Shampoo",
                    "precio": -10.0,
                    "stockActual": 20,
                    "stockMinimo": 5
                }
                """;

        mockMvc.perform(post("/api/productos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(productoInvalidoJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void debeRetornarBadRequestCuandoStockMinimoEsNegativo() throws Exception {
        String productoInvalidoJson = """
                {
                    "nombre": "Shampoo Antipulgas",
                    "descripcion": "Shampoo",
                    "precio": 45.50,
                    "stockActual": 20,
                    "stockMinimo": -1
                }
                """;

        mockMvc.perform(post("/api/productos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(productoInvalidoJson))
                .andExpect(status().isBadRequest());
    }
}