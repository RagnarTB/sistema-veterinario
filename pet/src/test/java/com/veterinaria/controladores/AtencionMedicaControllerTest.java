package com.veterinaria.controladores;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.veterinaria.modelos.AtencionMedica;
import com.veterinaria.servicios.AtencionMedicaServicio;

@WebMvcTest(controllers = AtencionMedicaController.class, excludeAutoConfiguration = {
        SecurityAutoConfiguration.class })
public class AtencionMedicaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AtencionMedicaServicio atencionMedicaServicio;

    @Test
    void debeCrearAtencionMedicaYRetornarEstadoCorrecto() throws Exception {
        String atencionMedicaJson = """
                {
                    "citaId" : 1,
                    "sintomas" : "esta decaido",
                    "diagnostico" :  "tiene fiebre",
                    "tratamiento" : "tomar pastillas",
                    "peso" : 20.50,
                    "temperatura" : 27.2,
                    "frecuenciaCardiaca" : 80


                }
                """;

        mockMvc.perform(post("/api/atenciones")
                .contentType(MediaType.APPLICATION_JSON)
                .content(atencionMedicaJson)).andExpect(status().isCreated());
    }

}
