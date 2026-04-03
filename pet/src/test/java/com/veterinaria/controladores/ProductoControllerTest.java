package com.veterinaria.controladores;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.veterinaria.servicios.ProductoServicio;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Por eso, algunas líneas de aquí abajo te marcarán error en tu IDE (letras rojas). ¡Eso es TDD!
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class ProductoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductoServicio productoServicio;

    @Test
    void debeCrearProductoYRetornarEstadoCorrecto() throws Exception {
        String productoJson = """
                {
                    "nombre": "Shampoo Antipulgas",
                    "descripcion": "Shampoo para perros de todas las razas",
                    "precio": 45.50,
                    "stockActual": 20
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
                    "stockActual": 20
                }
                """;

        mockMvc.perform(post("/api/productos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(productoInvalidoJson))
                .andExpect(status().isBadRequest());
    }
}