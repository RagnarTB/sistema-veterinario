package com.veterinaria.controladores;

import com.veterinaria.dtos.HospitalizacionRequestDTO;
import com.veterinaria.dtos.HospitalizacionResponseDTO;
import com.veterinaria.servicios.HospitalizacionServicio;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/hospitalizaciones")
@RequiredArgsConstructor
public class HospitalizacionController {

    private final HospitalizacionServicio hospitalizacionServicio;

    @PostMapping("/ingreso")
    @PreAuthorize("hasAnyRole('ADMIN', 'VETERINARIO')")
    public ResponseEntity<HospitalizacionResponseDTO> ingresarPaciente(@Valid @RequestBody HospitalizacionRequestDTO requestDTO) {
        HospitalizacionResponseDTO response = hospitalizacionServicio.ingresarPaciente(requestDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}/alta")
    @PreAuthorize("hasAnyRole('ADMIN', 'VETERINARIO')")
    public ResponseEntity<HospitalizacionResponseDTO> darDeAlta(@PathVariable Long id) {
        HospitalizacionResponseDTO response = hospitalizacionServicio.darDeAlta(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/traslado")
    @PreAuthorize("hasAnyRole('ADMIN', 'VETERINARIO')")
    public ResponseEntity<HospitalizacionResponseDTO> trasladarPaciente(
            @PathVariable Long id, 
            @Valid @RequestBody com.veterinaria.dtos.TrasladoHospitalizacionDTO requestDTO) {
        HospitalizacionResponseDTO response = hospitalizacionServicio.trasladarPaciente(id, requestDTO.getNuevaJaulaId());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/fallecimiento")
    @PreAuthorize("hasAnyRole('ADMIN', 'VETERINARIO')")
    public ResponseEntity<HospitalizacionResponseDTO> registrarFallecimiento(@PathVariable Long id) {
        HospitalizacionResponseDTO response = hospitalizacionServicio.registrarFallecimiento(id);
        return ResponseEntity.ok(response);
    }
}
