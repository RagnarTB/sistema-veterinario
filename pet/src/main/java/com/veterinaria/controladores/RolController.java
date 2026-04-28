package com.veterinaria.controladores;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.veterinaria.dtos.RolRequestDTO;
import com.veterinaria.dtos.RolResponseDTO;
import com.veterinaria.servicios.RolServicio;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/roles")
@PreAuthorize("hasRole('ADMIN')")
public class RolController {

    private final RolServicio rolServicio;

    public RolController(RolServicio rolServicio) {
        this.rolServicio = rolServicio;
    }

    @GetMapping
    public List<RolResponseDTO> listarTodos() {
        return rolServicio.listarTodos();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RolResponseDTO guardar(@Valid @RequestBody RolRequestDTO dto) {
        return rolServicio.guardar(dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        rolServicio.eliminar(id);
    }

    @PatchMapping("/{id}/estado")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cambiarEstado(@PathVariable Long id, @RequestParam Boolean estado) {
        rolServicio.cambiarEstado(id, estado);
    }
}
