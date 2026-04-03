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
import com.veterinaria.dtos.CitaRequestDTO;
import com.veterinaria.dtos.CitaResponseDTO; // Importante
import com.veterinaria.modelos.Enums.EstadoCita; // Importante

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class CitaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CitaServicio citaServicio;

    @Test
    @WithMockUser(roles = "RECEPCIONISTA")
    void debeCrearCitaGrupalYRetornarEstadoCorrecto() throws Exception {

        String citaGrupalJson = """
                {
                    "fecha" : "2026-10-15",
                    "horaInicio" : "10:00:00",
                    "servicioId": 1,
                    "veterinarioId": 2,
                    "motivo": "Primera vacuna para la camada de gatitos",
                    "pacienteIds": [1, 2, 3, 4]
                }
                """;

        // 1. Configuramos el Mock para que devuelva un DTO válido
        CitaResponseDTO respuestaMock = new CitaResponseDTO(
                1L,
                LocalDate.of(2026, 10, 15),
                LocalTime.of(10, 0),
                LocalTime.of(10, 30),
                "Vacunación",
                2L,
                "Primera vacuna para la camada de gatitos",
                EstadoCita.AGENDADA,
                List.of(1L, 2L, 3L, 4L));

        when(citaServicio.guardar(any(CitaRequestDTO.class))).thenReturn(respuestaMock);

        // 2. Ejecutamos y verificamos no solo el estado, sino también el contenido
        mockMvc.perform(post("/api/citas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(citaGrupalJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.servicioNombre").value("Vacunación"))
                .andExpect(jsonPath("$.estado").value("AGENDADA"))
                .andExpect(jsonPath("$.pacienteIds").isArray())
                .andExpect(jsonPath("$.pacienteIds[0]").value(1));
    }
}