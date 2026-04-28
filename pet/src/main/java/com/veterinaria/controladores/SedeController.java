package com.veterinaria.controladores;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.veterinaria.dtos.SedeRequestDTO;
import com.veterinaria.dtos.SedeResponseDTO;
import com.veterinaria.servicios.SedeServicio;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/sedes")
@PreAuthorize("hasRole('ADMIN')")
public class SedeController {

    private final SedeServicio sedeServicio;

    public SedeController(SedeServicio sedeServicio) {
        this.sedeServicio = sedeServicio;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SedeResponseDTO guardar(@Valid @RequestBody SedeRequestDTO dto) {
        return sedeServicio.guardar(dto);
    }

    @GetMapping
    public Page<SedeResponseDTO> listarTodas(Pageable pageable) {
        return sedeServicio.listarTodas(pageable);
    }

    @GetMapping("/{id}")
    public SedeResponseDTO buscarPorId(@PathVariable Long id) {
        return sedeServicio.buscarPorId(id);
    }

    @PutMapping("/{id}")
    public SedeResponseDTO actualizar(@PathVariable Long id, @Valid @RequestBody SedeRequestDTO dto) {
        return sedeServicio.actualizar(id, dto);
    }

    @PatchMapping("/{id}/estado")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cambiarEstado(@PathVariable Long id, @RequestParam Boolean estado) {
        sedeServicio.cambiarEstado(id, estado);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        sedeServicio.eliminar(id);
    }
}
