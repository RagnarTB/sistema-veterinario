package com.veterinaria.controladores;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.veterinaria.dtos.CitaRequestDTO;
import com.veterinaria.dtos.CitaResponseDTO;
import com.veterinaria.servicios.CitaServicio;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/citas")
public class CitaController {
    @Autowired
    private CitaServicio citaServicio;

    @PostMapping
    public ResponseEntity<CitaResponseDTO> crearCita(@Valid @RequestBody CitaRequestDTO dto) {
        CitaResponseDTO respuesta = citaServicio.guardar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

}
