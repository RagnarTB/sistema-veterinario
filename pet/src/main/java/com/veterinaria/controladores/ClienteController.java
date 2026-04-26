package com.veterinaria.controladores;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.veterinaria.dtos.ClienteRequestDTO;
import com.veterinaria.dtos.ClienteResponseDTO;
import com.veterinaria.servicios.ClienteServicio;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.web.bind.annotation.CrossOrigin;

@RestController
@RequestMapping("/api/clientes")
@CrossOrigin(origins = "http://localhost:4200")
public class ClienteController {

    @Autowired
    private ClienteServicio clienteServicio;

    @PostMapping
    public ResponseEntity<ClienteResponseDTO> crearCliente(@Valid @RequestBody ClienteRequestDTO dto) {
        ClienteResponseDTO respuesta = clienteServicio.guardar(dto);

        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    @GetMapping
    public ResponseEntity<Page<ClienteResponseDTO>> listarClientes(
            Pageable pageable,
            @RequestParam(required = false) String buscar) {

        Page<ClienteResponseDTO> clientes = clienteServicio.listarTodos(buscar, pageable);

        return ResponseEntity.ok(clientes);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClienteResponseDTO> obtenerClientePorId(@PathVariable Long id) {
        ClienteResponseDTO cliente = clienteServicio.buscarPorId(id);
        return ResponseEntity.ok(cliente);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClienteResponseDTO> actualizarCliente(@PathVariable Long id,
            @Valid @RequestBody ClienteRequestDTO dto) {
        ClienteResponseDTO clienteActualizado = clienteServicio.actualizar(id, dto);
        return ResponseEntity.ok(clienteActualizado);

    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> cambiarEstadoCliente(@PathVariable Long id, @RequestParam Boolean activo) {
        clienteServicio.cambiarEstado(id, activo);
        return ResponseEntity.noContent().build();
    }

}
