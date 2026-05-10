package com.veterinaria.servicios;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import com.veterinaria.dtos.SunatRucResponseDTO;

@Service
public class SunatServicio {

    private final String API_URL = "https://api.decolecta.com/v1/sunat/ruc?numero=";
    private final String API_KEY = "sk_9739.xfKEozZkKVg69oje8RACY8preIWY6nwh";

    public SunatRucResponseDTO consultarRuc(String ruc) {
        try {
            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + API_KEY);
            headers.set("Content-Type", "application/json");

            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<SunatRucResponseDTO> response = restTemplate.exchange(
                    API_URL + ruc,
                    HttpMethod.GET,
                    entity,
                    SunatRucResponseDTO.class
            );

            return response.getBody();

        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error al consultar RUC o no encontrado");
        }
    }
}
