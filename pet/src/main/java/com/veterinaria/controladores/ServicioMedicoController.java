package com.veterinaria.controladores;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.veterinaria.dtos.ServicioMedicoRequestDTO;
import com.veterinaria.dtos.ServicioMedicoResponseDTO;
import com.veterinaria.servicios.ServicioMedicoServicio;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/servicios-medicos")
public class ServicioMedicoController {

    @Autowired
    private ServicioMedicoServicio servicioMedicoServicio;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')") // Solo el administrador puede crear servicios
    public ResponseEntity<ServicioMedicoResponseDTO> crearServicio(@Valid @RequestBody ServicioMedicoRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(servicioMedicoServicio.guardar(dto));
    }

    @GetMapping
    // Abierto a cualquier usuario autenticado (Recepcionista, Veterinario, etc.)
    // para que puedan llenar selects en Angular
    public ResponseEntity<List<ServicioMedicoResponseDTO>> listarServicios() {
        return ResponseEntity.ok(servicioMedicoServicio.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServicioMedicoResponseDTO> obtenerServicio(@PathVariable Long id) {
        return ResponseEntity.ok(servicioMedicoServicio.buscarPorId(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ServicioMedicoResponseDTO> actualizarServicio(@PathVariable Long id,
            @Valid @RequestBody ServicioMedicoRequestDTO dto) {
        return ResponseEntity.ok(servicioMedicoServicio.actualizar(id, dto));
    }

    // Usamos PATCH para cambiar solo un atributo específico (el estado)
    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> cambiarEstadoServicio(@PathVariable Long id, @RequestParam Boolean activo) {
        servicioMedicoServicio.cambiarEstado(id, activo);
        return ResponseEntity.noContent().build();
    }
}