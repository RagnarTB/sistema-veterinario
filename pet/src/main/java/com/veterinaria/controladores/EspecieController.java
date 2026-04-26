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
import java.util.List;

@RestController
@RequestMapping("/api/especies")
public class EspecieController {

    @Autowired
    private EspecieServicio especieServicio;

    // Crear
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EspecieResponseDTO> crearEspecie(@Valid @RequestBody EspecieRequestDTO dto) {
        EspecieResponseDTO respuesta = especieServicio.guardar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    // Obtener Todas
    @GetMapping
    public ResponseEntity<List<EspecieResponseDTO>> obtenerTodas() {
        return ResponseEntity.ok(especieServicio.obtenerTodas());
    }

    // Obtener por ID
    @GetMapping("/{id}")
    public ResponseEntity<EspecieResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(especieServicio.obtenerPorId(id));
    }

    // Actualizar nombre
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EspecieResponseDTO> actualizarEspecie(
            @PathVariable Long id,
            @Valid @RequestBody EspecieRequestDTO dto) {

        EspecieResponseDTO respuesta = especieServicio.actualizar(id, dto);
        return ResponseEntity.ok(respuesta);
    }

    // Cambiar Estado (Borrado lógico / Restaurar)
    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> cambiarEstadoEspecie(@PathVariable Long id) {
        especieServicio.cambiarEstado(id);
        return ResponseEntity.noContent().build(); // HTTP 204: Petición procesada con éxito, sin contenido que devolver
    }

    // Borrado Físico Definitivo
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminarEspecie(@PathVariable Long id) {
        especieServicio.eliminarFisicamente(id);
        return ResponseEntity.noContent().build();
    }
}