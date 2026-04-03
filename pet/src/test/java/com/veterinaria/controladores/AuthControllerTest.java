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

import com.veterinaria.servicios.AuthServicio;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthServicio authServicio;

    @Test
    void debeRegistrarClienteYRetornarCreated() throws Exception {
        String registroJson = """
                {
                    "email": "juan@gmail.com",
                    "password": "MiPasswordSecreto123",
                    "nombre": "Juan",
                    "apellido": "Perez",
                    "telefono": "999888777",
                    "dni": "77778888"
                }
                """;

        mockMvc.perform(post("/api/auth/registro")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registroJson))
                .andExpect(status().isCreated());
    }

    @Test
    void debeAutenticarUsuarioYRetornarToken() throws Exception {
        String loginJson = """
                {
                    "email": "juan@gmail.com",
                    "password": "MiPasswordSecreto123"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson))
                .andExpect(status().isOk()); // El login exitoso devuelve 200 OK
    }
}