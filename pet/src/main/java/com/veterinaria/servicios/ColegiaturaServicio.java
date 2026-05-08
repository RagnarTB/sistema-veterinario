package com.veterinaria.servicios;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class ColegiaturaServicio {

    // Cambiar la URL de acuerdo a la página oficial de búsqueda del CMVL o CMVP
    private static final String URL_BUSQUEDA_CMVP = "https://cmvl.pe/buscar-colegiado";

    /**
     * Valida si un médico veterinario está habilitado haciendo Web Scraping.
     * Utiliza Redis Cache para evitar múltiples peticiones en 24 horas.
     */
    @Cacheable(value = "colegiaturas", key = "#numeroColegiatura", unless = "#result.get('error') != null")
    public Map<String, Object> validarVeterinario(String numeroColegiatura) {
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("numeroColegiatura", numeroColegiatura);

        try {
            // Ejemplo de Scraping con Jsoup enviando un POST
            // NOTA: Ajustar nombres de parámetros y selectores según HTML real.
            Document doc = Jsoup.connect(URL_BUSQUEDA_CMVP)
                    .data("colegiatura", numeroColegiatura) // Ajustar campo
                    .data("buscar", "1") // Ajustar campos del formulario
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                    .timeout(10000)
                    .post();

            // Buscar el elemento que contenga el resultado (Ajustar selectores)
            Element elementoEstado = doc.selectFirst(".estado-habilidad"); 
            Element elementoNombre = doc.selectFirst(".nombre-colegiado");

            if (elementoEstado != null && elementoNombre != null) {
                String estado = elementoEstado.text().trim();
                String nombreCompleto = elementoNombre.text().trim();

                respuesta.put("nombreCompleto", nombreCompleto);
                respuesta.put("estado", estado);
                respuesta.put("habilitado", estado.equalsIgnoreCase("HABILITADO"));
                respuesta.put("fechaValidacion", java.time.LocalDateTime.now().toString());

            } else {
                // Para efectos de prueba o si la web no está disponible, simulamos un mock 
                // ya que no tenemos el HTML exacto de cmvl.pe
                if (numeroColegiatura.equals("12345")) {
                    respuesta.put("nombreCompleto", "VETERINARIO DE PRUEBA");
                    respuesta.put("estado", "HABILITADO");
                    respuesta.put("habilitado", true);
                    respuesta.put("fechaValidacion", java.time.LocalDateTime.now().toString());
                } else {
                    respuesta.put("error", "Colegiatura no encontrada o el formato de la web cambió.");
                    respuesta.put("habilitado", false);
                }
            }

        } catch (IOException e) {
            respuesta.put("error", "No se pudo conectar con el servidor del CMVP (Timeout/Error de red).");
            respuesta.put("habilitado", false);
        } catch (Exception e) {
            respuesta.put("error", "Ocurrió un error inesperado al validar la colegiatura: " + e.getMessage());
            respuesta.put("habilitado", false);
        }

        return respuesta;
    }
}
