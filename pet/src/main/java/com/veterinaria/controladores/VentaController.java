package com.veterinaria.controladores;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.veterinaria.dtos.VentaRequestDTO;
import com.veterinaria.dtos.VentaResponseDTO;
import com.veterinaria.servicios.VentaServicio;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/ventas")
public class VentaController {

    @Autowired
    private VentaServicio ventaServicio;

    @Autowired
    private com.veterinaria.servicios.EmpleadoAutenticadoService empleadoAutenticadoService;

    @PostMapping
    public ResponseEntity<VentaResponseDTO> crearVenta(
            @Valid @RequestBody VentaRequestDTO dto) {
        com.veterinaria.modelos.Empleado empleadoActual = empleadoAutenticadoService.obtenerEmpleadoActual();
        VentaResponseDTO respuesta = ventaServicio.guardar(dto, empleadoActual);
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    // GET /api/ventas
    @GetMapping
    public ResponseEntity<Page<VentaResponseDTO>> listarVentas(Pageable pageable) {
        Page<VentaResponseDTO> ventas = ventaServicio.listarTodas(pageable);
        return ResponseEntity.ok(ventas);
    }

    // GET /api/ventas/{id}
    @GetMapping("/{id}")
    public ResponseEntity<VentaResponseDTO> buscarVentaPorId(@PathVariable Long id) {
        return ResponseEntity.ok(ventaServicio.buscarPorId(id));
    }

    @PatchMapping("/{id}/anular")
    @PreAuthorize("hasRole('ADMIN')") // Un cajero normal no debería poder anular sin permiso
    public ResponseEntity<com.veterinaria.dtos.MensajeResponseDTO> anularVenta(@PathVariable Long id) {
        com.veterinaria.modelos.Empleado empleadoActual = empleadoAutenticadoService.obtenerEmpleadoActual();
        return ResponseEntity.ok(ventaServicio.anularVenta(id, empleadoActual));
    }
}