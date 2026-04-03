package com.veterinaria.controladores;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.veterinaria.dtos.CajaRequestDTO;
import com.veterinaria.dtos.CierreCajaResponseDTO;
import com.veterinaria.servicios.CajaServicio;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/caja")
public class CajaController {

    @Autowired
    private CajaServicio cajaServicio;

    @PostMapping("/abrir")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<Void> abrirCaja(@Valid @RequestBody CajaRequestDTO dto) {
        cajaServicio.abrirCaja(dto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // ¡NUEVO ENDPOINT!
    @PutMapping("/cerrar")
    @PreAuthorize("hasRole('ADMIN')") // Por seguridad, solo el dueño/admin cierra la caja
    public ResponseEntity<CierreCajaResponseDTO> cerrarCaja() {
        CierreCajaResponseDTO resumen = cajaServicio.cerrarCaja();
        return ResponseEntity.ok(resumen);
    }
}