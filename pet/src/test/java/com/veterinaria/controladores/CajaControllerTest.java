package com.veterinaria.controladores;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

// IMPORTANTE: Este servicio no existe todavía
import com.veterinaria.servicios.CajaServicio;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class CajaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CajaServicio cajaServicio;

    @Test
    @WithMockUser(roles = "ADMIN") // Solo el Admin o Recepcionista principal abre la caja
    void debeAbrirCajaConSaldoInicial() throws Exception {

        // Enviamos con cuánto dinero en efectivo iniciamos el día (para dar vueltos)
        String jsonRequest = """
                {
                    "saldoInicial": 100.50
                }
                """;

        // Simulamos la petición POST a nuestro futuro endpoint
        mockMvc.perform(post("/api/caja/abrir")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "ADMIN") // Solo un administrador cierra la caja
    void debeCerrarCajaYRetornarEstadoOk() throws Exception {

        // Simulamos la petición PUT para cerrar la caja del día
        mockMvc.perform(put("/api/caja/cerrar"))
                .andExpect(status().isOk());
    }
}