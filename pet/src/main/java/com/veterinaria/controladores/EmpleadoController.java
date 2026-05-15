package com.veterinaria.controladores;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.veterinaria.dtos.EmpleadoRequestDTO;
import com.veterinaria.dtos.EmpleadoResponseDTO;
import com.veterinaria.servicios.EmpleadoServicio;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/empleados")
@PreAuthorize("hasRole('ADMIN')")
public class EmpleadoController {

    private final EmpleadoServicio empleadoServicio;

    public EmpleadoController(EmpleadoServicio empleadoServicio) {
        this.empleadoServicio = empleadoServicio;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EmpleadoResponseDTO guardar(@Valid @RequestBody EmpleadoRequestDTO dto) {
        return empleadoServicio.guardar(dto);
    }

    @GetMapping
    public Page<EmpleadoResponseDTO> listarTodos(
            Pageable pageable,
            @RequestParam(required = false) String buscar,
            @RequestParam(required = false) Boolean estado,
            @RequestParam(required = false) Long sedeId) {
        return empleadoServicio.listarTodos(buscar, estado, sedeId, pageable);
    }

    @GetMapping("/{id}")
    public EmpleadoResponseDTO buscarPorId(@PathVariable Long id) {
        return empleadoServicio.buscarPorId(id);
    }

    @PutMapping("/{id}")
    public EmpleadoResponseDTO actualizar(@PathVariable Long id, @Valid @RequestBody EmpleadoRequestDTO dto) {
        return empleadoServicio.actualizar(id, dto);
    }

    @PatchMapping("/{id}/estado")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cambiarEstado(@PathVariable Long id, @RequestParam Boolean estado) {
        empleadoServicio.cambiarEstado(id, estado);
    }
}
