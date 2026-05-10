package com.veterinaria.controladores;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.veterinaria.dtos.PacienteRequestDTO;
import com.veterinaria.dtos.PacienteResponseDTO;
import com.veterinaria.servicios.PacienteServicio;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/pacientes")
public class PacienteController {

    @Autowired
    private PacienteServicio pacienteServicio;

    @PostMapping
    public ResponseEntity<PacienteResponseDTO> crearPaciente(@Valid @RequestBody PacienteRequestDTO dto) {
        // 1. Mandamos a guardar y recibimos el objeto con su nuevo ID
        PacienteResponseDTO respuesta = pacienteServicio.guardar(dto);
        // 2. .body(respuesta) significa "Mete este objeto dentro del paquete y
        // envíaselo a Angular"
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    @GetMapping
    public ResponseEntity<Page<PacienteResponseDTO>> listarPacientes(
            Pageable pageable,
            @RequestParam(required = false) String buscar,
            @RequestParam(required = false) Boolean estado) {
        Page<PacienteResponseDTO> pacientes = pacienteServicio.listarTodos(buscar, estado, pageable);
        return ResponseEntity.ok(pacientes);
    }

    @GetMapping("/{id}") // Le decimos que espere un ID en la URL
    public ResponseEntity<PacienteResponseDTO> obtenerPacientePorId(@PathVariable Long id) {

        // Llamamos al servicio pasándole el ID que atrapamos de la URL
        PacienteResponseDTO paciente = pacienteServicio.buscarPorId(id);

        return ResponseEntity.ok(paciente);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PacienteResponseDTO> actualizarPaciente(
            @PathVariable Long id,
            @Valid @RequestBody PacienteRequestDTO dto) {

        // Le pasamos al servicio TANTO el ID que queremos buscar, COMO los datos nuevos
        PacienteResponseDTO pacienteActualizado = pacienteServicio.actualizar(id, dto);

        return ResponseEntity.ok(pacienteActualizado);
    }

    @DeleteMapping("/{id}/estado")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> cambiarEstadoPaciente(@PathVariable Long id, @RequestParam Boolean activo) {
        pacienteServicio.cambiarEstado(id, activo);
        // Si todo sale bien, devuelve 204 No Content
        return ResponseEntity.noContent().build();
    }

}