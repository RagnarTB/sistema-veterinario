package com.veterinaria.controladores;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.test.context.support.WithMockUser; // ¡La llave maestra!

import com.veterinaria.servicios.PacienteServicio;
import com.veterinaria.dtos.PacienteResponseDTO;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest // Levantamos TODA la aplicación (adiós errores de beans faltantes)
@AutoConfigureMockMvc // Encendemos el simulador de peticiones HTTP
class PacienteControllerTest {

        @Autowired
        private MockMvc mockMvc;

        // Solo "simulamos" el servicio para que el controlador no toque la base de
        // datos real en esta prueba
        @MockBean
        private PacienteServicio pacienteServicio;

        @Test
        @WithMockUser // Le susurra a Spring: "Déjalo pasar, este usuario ya está autenticado"
        void debeCrearPacienteYRetornarEstadoCorrecto() throws Exception {
                String pacienteJson = """
                                {
                                    "nombre": "Firulais",
                                    "especieId": 1,
                                    "raza": "Mestizo",
                                    "clienteId": 1
                                }
                                """;

                mockMvc.perform(post("/api/pacientes")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(pacienteJson))
                                .andExpect(status().isCreated());
        }

        @Test
        @WithMockUser
        void debeRetornarBadRequestCuandoElNombreEstaVacio() throws Exception {
                String pacienteInvalidoJson = """
                                        {
                                "nombre":"",
                                "especieId": 1,
                                "raza":"Mestizo"}
                                        """;
                mockMvc.perform(post("/api/pacientes")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(pacienteInvalidoJson))
                                .andExpect(status().isBadRequest());
        }

        // --- ¡NUESTRO FLAMANTE TEST DE PAGINACIÓN! ---
        @Test
        @WithMockUser
        void debeObtenerPaginaDePacientesYRetornarEstadoOk() throws Exception {
                PacienteResponseDTO pacienteMock = new PacienteResponseDTO(1L, "Firulais", "Perro", "Mestizo", 1L);
                Page<PacienteResponseDTO> paginaMock = new PageImpl<>(List.of(pacienteMock));

                when(pacienteServicio.listarTodos(any())).thenReturn(paginaMock);

                mockMvc.perform(get("/api/pacientes?page=0&size=10")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content").isArray())
                                .andExpect(jsonPath("$.totalElements").value(1));
        }

        @Test
        @WithMockUser
        void debeObtenerPacientePorIdYRetornarEstadoOk() throws Exception {
                mockMvc.perform(get("/api/pacientes/1")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk());
        }

        @Test
        @WithMockUser
        void debeActualizarPacienteYRetornarEstadoOk() throws Exception {
                String pacienteActualizadoJson = """
                                {
                                    "nombre": "Firulais Corregido",
                                    "especieId": 1,
                                    "raza": "Mestizo",
                                    "clienteId": 1
                                }
                                """;

                mockMvc.perform(put("/api/pacientes/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(pacienteActualizadoJson))
                                .andExpect(status().isOk());
        }

        @Test
        @WithMockUser
        void debeEliminarPacienteYRetornarEstadoNoContent() throws Exception {
                mockMvc.perform(delete("/api/pacientes/1"))
                                .andExpect(status().isNoContent());
        }
}