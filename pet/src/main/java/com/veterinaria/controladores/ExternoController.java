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

    @Autowired
    private com.veterinaria.respositorios.UsuarioRepositorio usuarioRepositorio;

    @GetMapping("/dni/{numero}")
    public ResponseEntity<ReniecResponseDTO> consultarDni(@PathVariable String numero) {
        ReniecResponseDTO data = null;
        java.util.Optional<com.veterinaria.modelos.Usuario> userOpt = usuarioRepositorio.findByDni(numero);

        try {
            data = reniecServicio.consultarDni(numero);
        } catch (Exception e) {
            if (userOpt.isPresent()) {
                data = new ReniecResponseDTO();
                data.setDocumentNumber(numero);
            } else {
                throw e;
            }
        }

        if (userOpt.isPresent()) {
            com.veterinaria.modelos.Usuario u = userOpt.get();
            data.setExisteEnBd(true);
            data.setEmail(u.getEmail());
            data.setTelefono(u.getTelefono());
            // Sobrescribimos o asignamos el nombre del sistema
            data.setFirstName(u.getNombre());
            data.setFirstLastName(u.getApellido());
            data.setSecondLastName("");
            data.setFullName(u.getNombre() + " " + u.getApellido());
        }

        return ResponseEntity.ok(data);
    }

}
