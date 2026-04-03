package com.veterinaria.controladores;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.veterinaria.servicios.CitaServicio;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false) // Apagamos la seguridad para enfocarnos en la lógica
class CitaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CitaServicio citaServicio;

    @Test
    @WithMockUser(roles = "RECEPCIONISTA")
    void debeCrearCitaGrupalYRetornarEstadoCorrecto() throws Exception {

        // ¡EL CAMBIO RADICAL!
        // Ya no enviamos "pacienteId": 1
        // Ahora enviamos un arreglo "pacienteIds": [1, 2, 3, 4]
        String citaGrupalJson = """
                {
                    "fecha" : "2026-10-15",
                    "hora" : "10:00:00",
                    "motivo": "Primera vacuna para la camada de gatitos",
                    "pacienteIds": [1, 2, 3, 4]
                }
                """;

        mockMvc.perform(post("/api/citas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(citaGrupalJson))
                .andExpect(status().isCreated());
    }
}