package com.veterinaria.controladores;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.veterinaria.dtos.AuthResponseDTO;
import com.veterinaria.dtos.CambiarPasswordRequestDTO;
import com.veterinaria.dtos.LoginRequestDTO;
import com.veterinaria.dtos.MensajeResponseDTO;
import com.veterinaria.dtos.RefreshTokenRequestDTO;
import com.veterinaria.dtos.RegistroClienteDTO;
import com.veterinaria.dtos.GoogleLoginRequestDTO;
import com.veterinaria.dtos.SolicitarRegistroCorreoDTO;
import com.veterinaria.dtos.CompletarRegistroDTO;
import com.veterinaria.dtos.SeleccionarRolRequestDTO;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth") // AQUI: Ruta base corregida
@CrossOrigin(origins = "http://localhost:4200")
public class AuthController {

    @Autowired
    private com.veterinaria.servicios.auth.AuthLoginServicio authLoginServicio;

    @Autowired
    private com.veterinaria.servicios.auth.AuthRegistroServicio authRegistroServicio;

    @Autowired
    private com.veterinaria.servicios.auth.AuthGoogleServicio authGoogleServicio;

    @PostMapping("/registro")
    public ResponseEntity<MensajeResponseDTO> registrarCliente(@Valid @RequestBody RegistroClienteDTO dto) {
        MensajeResponseDTO respuesta = authRegistroServicio.registrarCliente(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO dto) {
        AuthResponseDTO respuesta = authLoginServicio.login(dto);
        return ResponseEntity.ok(respuesta);
    }

    @PostMapping("/google")
    public ResponseEntity<?> loginConGoogle(@Valid @RequestBody GoogleLoginRequestDTO dto) {
        Object respuesta = authGoogleServicio.loginConGoogle(dto);
        if (respuesta instanceof AuthResponseDTO) {
            return ResponseEntity.ok(respuesta);
        } else {
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(respuesta); 
        }
    }

    @PostMapping("/solicitar-registro-correo")
    public ResponseEntity<MensajeResponseDTO> solicitarRegistroCorreo(@Valid @RequestBody SolicitarRegistroCorreoDTO dto) {
        MensajeResponseDTO respuesta = authRegistroServicio.solicitarRegistroCorreo(dto);
        return ResponseEntity.ok(respuesta);
    }

    @PostMapping("/completar-registro")
    public ResponseEntity<AuthResponseDTO> completarRegistro(@Valid @RequestBody CompletarRegistroDTO dto) {
        AuthResponseDTO respuesta = authRegistroServicio.completarRegistro(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    @PostMapping("/cambiar-password")
    public ResponseEntity<MensajeResponseDTO> cambiarPassword(@Valid @RequestBody CambiarPasswordRequestDTO dto) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        MensajeResponseDTO respuesta = authLoginServicio.cambiarPassword(email, dto);
        return ResponseEntity.ok(respuesta);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponseDTO> refresh(@Valid @RequestBody RefreshTokenRequestDTO dto) {
        AuthResponseDTO respuesta = authLoginServicio.refreshToken(dto.getRefreshToken());
        return ResponseEntity.ok(respuesta);
    }

    @PostMapping("/logout")
    public ResponseEntity<MensajeResponseDTO> logout(@Valid @RequestBody RefreshTokenRequestDTO dto) {
        MensajeResponseDTO respuesta = authLoginServicio.logout(dto.getRefreshToken());
        return ResponseEntity.ok(respuesta);
    }

    @PostMapping("/confirmar-token")
    public ResponseEntity<MensajeResponseDTO> confirmarToken(@Valid @RequestBody com.veterinaria.dtos.ConfirmarTokenRequestDTO dto) {
        MensajeResponseDTO respuesta = authRegistroServicio.confirmarToken(dto.getToken(), dto.getPassword());
        return ResponseEntity.ok(respuesta);
    }

    @PostMapping("/seleccionar-rol")
    public ResponseEntity<AuthResponseDTO> seleccionarRol(
            @Valid @RequestBody SeleccionarRolRequestDTO dto) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        AuthResponseDTO respuesta = authLoginServicio.seleccionarRol(email, dto.getRol());
        return ResponseEntity.ok(respuesta);
    }
}