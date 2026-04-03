package com.veterinaria.controladores;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.veterinaria.dtos.HorarioVeterinarioRequestDTO;
import com.veterinaria.dtos.HorarioVeterinarioResponseDTO;
import com.veterinaria.servicios.HorarioVeterinarioServicio;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/horarios")
public class HorarioVeterinarioController {

    @Autowired
    private HorarioVeterinarioServicio horarioServicio;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')") // Solo el Admin arma los horarios de los doctores
    public ResponseEntity<HorarioVeterinarioResponseDTO> crearHorario(
            @Valid @RequestBody HorarioVeterinarioRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(horarioServicio.guardar(dto));
    }

    @GetMapping
    public ResponseEntity<List<HorarioVeterinarioResponseDTO>> listarHorarios() {
        return ResponseEntity.ok(horarioServicio.listarTodos());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminarHorario(@PathVariable Long id) {
        horarioServicio.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}