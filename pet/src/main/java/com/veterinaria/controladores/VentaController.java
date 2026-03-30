package com.veterinaria.controladores;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @PostMapping
    public ResponseEntity<VentaResponseDTO> crearVenta(
            @Valid @RequestBody VentaRequestDTO dto) {
        VentaResponseDTO respuesta = ventaServicio.guardar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    // GET /api/ventas
    @GetMapping
    public ResponseEntity<List<VentaResponseDTO>> listarVentas() {
        return ResponseEntity.ok(ventaServicio.listarTodas());
    }

    // GET /api/ventas/{id}
    @GetMapping("/{id}")
    public ResponseEntity<VentaResponseDTO> buscarVentaPorId(@PathVariable Long id) {
        return ResponseEntity.ok(ventaServicio.buscarPorId(id));
    }
}