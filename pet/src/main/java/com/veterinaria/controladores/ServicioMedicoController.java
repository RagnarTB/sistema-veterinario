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

    @Autowired
    private com.veterinaria.servicios.ServicioMedicoInsumoServicio insumoServicio;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')") // Solo el administrador puede crear servicios
    public ResponseEntity<ServicioMedicoResponseDTO> crearServicio(@Valid @RequestBody ServicioMedicoRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(servicioMedicoServicio.guardar(dto));
    }

    @GetMapping
    // Abierto a cualquier usuario autenticado (Recepcionista, Veterinario, etc.)
    // para que puedan llenar selects en Angular
    public ResponseEntity<List<ServicioMedicoResponseDTO>> listarServicios(@RequestParam(required = false) Boolean activo) {
        return ResponseEntity.ok(servicioMedicoServicio.listarTodos(activo));
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

    // ========== INSUMOS DEL SERVICIO (PLANTILLA) ==========

    @GetMapping("/{servicioId}/insumos")
    public ResponseEntity<List<com.veterinaria.dtos.ServicioMedicoInsumoResponseDTO>> listarInsumos(@PathVariable Long servicioId) {
        return ResponseEntity.ok(insumoServicio.listarPorServicio(servicioId));
    }

    @PostMapping("/{servicioId}/insumos")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<com.veterinaria.dtos.ServicioMedicoInsumoResponseDTO> agregarInsumo(
            @PathVariable Long servicioId, 
            @Valid @RequestBody com.veterinaria.dtos.ServicioMedicoInsumoRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(insumoServicio.agregar(servicioId, dto));
    }

    @PutMapping("/{servicioId}/insumos/{insumoId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<com.veterinaria.dtos.ServicioMedicoInsumoResponseDTO> actualizarInsumo(
            @PathVariable Long servicioId,
            @PathVariable Long insumoId,
            @Valid @RequestBody com.veterinaria.dtos.ServicioMedicoInsumoRequestDTO dto) {
        return ResponseEntity.ok(insumoServicio.actualizar(insumoId, dto));
    }

    @PatchMapping("/{servicioId}/insumos/{insumoId}/estado")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> cambiarEstadoInsumo(
            @PathVariable Long servicioId,
            @PathVariable Long insumoId,
            @RequestParam Boolean activo) {
        insumoServicio.cambiarEstado(insumoId, activo);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{servicioId}/insumos/{insumoId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminarInsumo(
            @PathVariable Long servicioId,
            @PathVariable Long insumoId) {
        insumoServicio.eliminarPermanente(insumoId);
        return ResponseEntity.noContent().build();
    }
}