package com.veterinaria.controladores;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.veterinaria.dtos.EspecieRequestDTO;
import com.veterinaria.dtos.EspecieResponseDTO;
import com.veterinaria.servicios.EspecieServicio;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/especies")
public class EspecieController {

    @Autowired
    private EspecieServicio especieServicio;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EspecieResponseDTO> crearEspecie(@Valid @RequestBody EspecieRequestDTO dto) {

        // 1. Delegamos el trabajo al servicio
        EspecieResponseDTO respuesta = especieServicio.guardar(dto);

        // 2. Metemos la respuesta en la caja HTTP 201 y la enviamos
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }
}