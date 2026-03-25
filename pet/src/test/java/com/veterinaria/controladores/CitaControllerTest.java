package com.veterinaria.controladores;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.http.MediaType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import com.veterinaria.servicios.CitaServicio;

@WebMvcTest(controllers = CitaController.class, excludeAutoConfiguration = { SecurityAutoConfiguration.class })
public class CitaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CitaServicio citaServicio;

    @Test // segun la iso 8601 se debe poner la fecha y hora en el siguiente formato
    void debeCrearCitaYRetornarEstadoCorrecto() throws Exception {
        String citaJson = """
                {
                    "fecha" : "2025-03-25",
                    "hora" : "14:30:00",
                    "motivo": "tiene temperatura alta",
                    "pacienteId": 1
                    }
                """;
        mockMvc.perform(post("/api/citas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(citaJson))
                .andExpect(status().isCreated());
    }

}
