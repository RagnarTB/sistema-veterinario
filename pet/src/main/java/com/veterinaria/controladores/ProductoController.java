package com.veterinaria.controladores;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.veterinaria.dtos.ProductoRequestDTO;
import com.veterinaria.dtos.ProductoResponseDTO;
import com.veterinaria.servicios.ProductoServicio;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    @Autowired
    private ProductoServicio productoServicio;

    @PostMapping
    public ResponseEntity<ProductoResponseDTO> crearProducto(@Valid @RequestBody ProductoRequestDTO dto) {
        ProductoResponseDTO respuesta = productoServicio.guardar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    @GetMapping
    public ResponseEntity<Page<ProductoResponseDTO>> listarProductos(Pageable pageable) {
        Page<ProductoResponseDTO> productos = productoServicio.listarTodos(pageable);
        return ResponseEntity.ok(productos);
    }

    // IMPORTANTE: Este endpoint debe ir ANTES de /{id}.
    // Si fuera después, Spring interpretaría "alertas-stock" como un Long {id}
    // y lanzaría un TypeMismatchException.
    @GetMapping("/alertas-stock")
    public ResponseEntity<List<ProductoResponseDTO>> obtenerAlertasStock() {
        List<ProductoResponseDTO> alertas = productoServicio.obtenerAlertasStock();
        return ResponseEntity.ok(alertas);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductoResponseDTO> obtenerProductoPorId(@PathVariable Long id) {
        ProductoResponseDTO producto = productoServicio.buscarPorId(id);
        return ResponseEntity.ok(producto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductoResponseDTO> actualizarProducto(@PathVariable Long id,
            @Valid @RequestBody ProductoRequestDTO dto) {
        ProductoResponseDTO productoActualizado = productoServicio.actualizar(id, dto);
        return ResponseEntity.ok(productoActualizado);
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> cambiarEstadoProducto(@PathVariable Long id, @RequestParam Boolean activo) {
        productoServicio.cambiarEstado(id, activo);
        return ResponseEntity.noContent().build();
    }
}
