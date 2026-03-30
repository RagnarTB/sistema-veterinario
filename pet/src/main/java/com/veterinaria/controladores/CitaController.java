package com.veterinaria.controladores;

import java.util.List;

import org.apache.catalina.connector.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.veterinaria.dtos.CitaRequestDTO;
import com.veterinaria.dtos.CitaResponseDTO;
import com.veterinaria.dtos.PacienteResponseDTO;
import com.veterinaria.servicios.CitaServicio;

import jakarta.validation.Valid;
import jakarta.websocket.server.PathParam;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/api/citas")
public class CitaController {
    @Autowired
    private CitaServicio citaServicio;

    @PostMapping
    public ResponseEntity<CitaResponseDTO> crearCita(@Valid @RequestBody CitaRequestDTO dto) {
        CitaResponseDTO respuesta = citaServicio.guardar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    @GetMapping
    public ResponseEntity<List<CitaResponseDTO>> listarCitas() {
        List<CitaResponseDTO> citas = citaServicio.listar();

        return ResponseEntity.ok(citas);

    }

    @GetMapping("/{id}")
    public ResponseEntity<CitaResponseDTO> obtenerCitaPorId(@PathVariable Long id) {
        CitaResponseDTO respuesta = citaServicio.buscarPorId(id);
        return ResponseEntity.ok(respuesta);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CitaResponseDTO> actualizarCita(@PathVariable Long id,
            @Valid @RequestBody CitaRequestDTO dto) {
        CitaResponseDTO citaActualizada = citaServicio.actualizar(id, dto);

        return ResponseEntity.ok(citaActualizada);
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> eliminarCita(@PathVariable Long id) {
        citaServicio.eliminar(id);

        return ResponseEntity.noContent().build();
    }

}
