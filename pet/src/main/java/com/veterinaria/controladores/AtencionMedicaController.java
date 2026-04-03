package com.veterinaria.controladores;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.veterinaria.dtos.AtencionMedicaRequestDTO;
import com.veterinaria.dtos.AtencionMedicaResponseDTO;
import com.veterinaria.servicios.AtencionMedicaServicio;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/atenciones") // Misma ruta que tu test
public class AtencionMedicaController {

    @Autowired
    private AtencionMedicaServicio atencionMedicaServicio;

    @PostMapping
    @PreAuthorize("hasRole('VETERINARIO')") // candado para abrir el metodo segun el rol
    public ResponseEntity<AtencionMedicaResponseDTO> crearAtencionMedica(
            @Valid @RequestBody AtencionMedicaRequestDTO dto) { // CORRECCIÓN: Usamos el RequestDTO correcto
        AtencionMedicaResponseDTO respuesta = atencionMedicaServicio.guardar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    @GetMapping
    public ResponseEntity<Page<AtencionMedicaResponseDTO>> listarAtenciones(Pageable pageable) {
        Page<AtencionMedicaResponseDTO> atenciones = atencionMedicaServicio.listarTodos(pageable);
        return ResponseEntity.ok(atenciones);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AtencionMedicaResponseDTO> obtenerAtencionPorId(@PathVariable Long id) {
        AtencionMedicaResponseDTO respuesta = atencionMedicaServicio.buscarPorId(id);
        return ResponseEntity.ok(respuesta);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AtencionMedicaResponseDTO> actualizarAtencion(@PathVariable Long id,
            @Valid @RequestBody AtencionMedicaRequestDTO dto) {
        AtencionMedicaResponseDTO atencionActualizada = atencionMedicaServicio.actualizar(id, dto);
        return ResponseEntity.ok(atencionActualizada);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarAtencion(@PathVariable Long id) {
        atencionMedicaServicio.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}