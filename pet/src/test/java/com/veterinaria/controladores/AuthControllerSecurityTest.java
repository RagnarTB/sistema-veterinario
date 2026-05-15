package com.veterinaria.controladores;

import com.veterinaria.dtos.MensajeResponseDTO;
import com.veterinaria.servicios.auth.AuthLoginServicio;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthLoginServicio authLoginServicio;

    @Test
    void cambiarPasswordSinAutenticacionDebeRetornar401() throws Exception {
        String json = """
                {
                  "passwordActual": "Actual123",
                  "passwordNueva": "Nueva123"
                }
                """;

        mockMvc.perform(post("/api/auth/cambiar-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "user@test.com", roles = "CLIENTE")
    void cambiarPasswordConAutenticacionDebeRetornar200() throws Exception {
        when(authLoginServicio.cambiarPassword(anyString(), any()))
                .thenReturn(new MensajeResponseDTO("ok"));

        String json = """
                {
                  "passwordActual": "Actual123",
                  "passwordNueva": "Nueva123"
                }
                """;

        mockMvc.perform(post("/api/auth/cambiar-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());
    }
}

