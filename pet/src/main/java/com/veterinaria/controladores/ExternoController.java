package com.veterinaria.controladores;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;

import com.veterinaria.dtos.ReniecResponseDTO;
import com.veterinaria.servicios.ReniecServicio;

@RestController
@RequestMapping("/api/externo/reniec")
@CrossOrigin(origins = "http://localhost:4200")
public class ExternoController {

    @Autowired
    private ReniecServicio reniecServicio;

    @GetMapping("/dni/{numero}")
    public ResponseEntity<ReniecResponseDTO> consultarDni(@PathVariable String numero) {
        ReniecResponseDTO data = reniecServicio.consultarDni(numero);
        return ResponseEntity.ok(data);
    }

}
