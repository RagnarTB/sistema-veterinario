package com.veterinaria.controladores;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.veterinaria.servicios.EspecieServicio;
import com.veterinaria.dtos.EspecieRequestDTO;
// ¡ALERTA DE SPOILER! Este import va a fallar porque aún no lo creamos:
import com.veterinaria.dtos.EspecieResponseDTO;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath; // Para leer el JSON de respuesta

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class EspecieControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EspecieServicio especieServicio;

    @Test
    @WithMockUser(roles = "ADMIN")
    void debeCrearEspecieYRetornarDataConId() throws Exception { // Cambiamos el nombre del test
        String jsonRequest = """
                {
                    "nombre": "Canino"
                }
                """;

        // 1. Le decimos al simulador del servicio que devuelva un objeto con ID = 1
        EspecieResponseDTO respuestaMock = new EspecieResponseDTO(1L, "Canino");
        when(especieServicio.guardar(any(EspecieRequestDTO.class))).thenReturn(respuestaMock);

        // 2. Ejecutamos la petición y verificamos TODO
        mockMvc.perform(post("/api/especies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isCreated())
                // ¡NUEVAS EXIGENCIAS! Ahora queremos ver la data real:
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Canino"));
    }
}