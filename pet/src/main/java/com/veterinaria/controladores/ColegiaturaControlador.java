package com.veterinaria.controladores;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.veterinaria.servicios.ColegiaturaServicio;

import java.util.Map;

@RestController
@RequestMapping("/api/colegiaturas")
public class ColegiaturaControlador {

    private final ColegiaturaServicio colegiaturaServicio;

    public ColegiaturaControlador(ColegiaturaServicio colegiaturaServicio) {
        this.colegiaturaServicio = colegiaturaServicio;
    }

    @GetMapping("/validar/{numero}")
    public ResponseEntity<Map<String, Object>> validarColegiatura(@PathVariable String numero) {
        Map<String, Object> respuesta = colegiaturaServicio.validarVeterinario(numero);
        if (respuesta.containsKey("error")) {
            return ResponseEntity.badRequest().body(respuesta);
        }
        return ResponseEntity.ok(respuesta);
    }
}
