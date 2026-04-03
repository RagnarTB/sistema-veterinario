package com.veterinaria.controladores;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.veterinaria.dtos.DiaBloqueadoRequestDTO;
import com.veterinaria.dtos.DiaBloqueadoResponseDTO;
import com.veterinaria.servicios.DiaBloqueadoServicio;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/dias-bloqueados")
public class DiaBloqueadoController {

    @Autowired
    private DiaBloqueadoServicio diaBloqueadoServicio;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')") // Solo el Admin puede cerrar la clínica o dar días libres
    public ResponseEntity<DiaBloqueadoResponseDTO> crearDiaBloqueado(@Valid @RequestBody DiaBloqueadoRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(diaBloqueadoServicio.guardar(dto));
    }

    @GetMapping
    public ResponseEntity<List<DiaBloqueadoResponseDTO>> listarDiasBloqueados() {
        return ResponseEntity.ok(diaBloqueadoServicio.listarTodos());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminarDiaBloqueado(@PathVariable Long id) {
        diaBloqueadoServicio.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}