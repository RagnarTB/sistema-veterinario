package com.veterinaria.controladores;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import com.veterinaria.servicios.PacienteServicio;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PacienteController.class, excludeAutoConfiguration = { SecurityAutoConfiguration.class }) // Le
                                                                                                                    // //
                                                                                                                    // controlador
class PacienteControllerTest {

    @Autowired
    private MockMvc mockMvc; // Herramienta para simular peticiones HTTP

    @MockBean // <- ¡Esta es la magia! Crea un servicio simulado para el test
    private PacienteServicio pacienteServicio;

    @Test
    void debeCrearPacienteYRetornarEstadoCorrecto() throws Exception {
        // 1. Preparar los datos (Simulamos el JSON que enviaría Angular)
        String pacienteJson = """
                {
                    "nombre": "Firulais",
                    "especie": "Perro",
                    "raza": "Mestizo",
                    "clienteId": 1
                }
                """;

        // 2. Ejecutar la acción y 3. Verificar el resultado
        mockMvc.perform(post("/api/pacientes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(pacienteJson))
                .andExpect(status().isCreated());
    }

    @Test
    void debeRetornarBadRequestCuandoElNombreEstaVacio() throws Exception {
        String pacienteInvalidoJson = """
                        {
                "nombre":"",
                "especie": "Perro",
                "raza":"Mestizo"}
                        """;
        mockMvc.perform(post("/api/pacientes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(pacienteInvalidoJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void debeObtenerListaDePacientesYRetornarEstadoCorrecto() throws Exception {
        // Ejecutar la petición GET a la ruta /api/pacientes y verificar
        mockMvc.perform(get("/api/pacientes")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()); // ¿Qué código esperamos aquí?
    }

    @Test
    void debeObtenerPacientePorIdYRetornarEstadoOk() throws Exception {
        // Simulamos que buscamos al paciente con ID 1
        mockMvc.perform(get("/api/pacientes/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void debeActualizarPacienteYRetornarEstadoOk() throws Exception {
        // 1. Preparamos los nuevos datos corregidos
        String pacienteActualizadoJson = """
                {
                    "nombre": "Firulais Corregido",
                    "especie": "Perro",
                    "raza": "Mestizo",
                    "clienteId": 1
                }
                """;

        // 2. Ejecutamos un PUT a la URL del paciente 1
        mockMvc.perform(put("/api/pacientes/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(pacienteActualizadoJson))
                .andExpect(status().isOk()); // Esperamos un 200 OK
    }

    @Test
    void debeEliminarPacienteYRetornarEstadoNoContent() throws Exception {
        // Ejecutamos la petición DELETE a la URL del paciente 1
        mockMvc.perform(delete("/api/pacientes/1"))
                .andExpect(status().isNoContent()); // Esperamos un 204 No Content
    }
}