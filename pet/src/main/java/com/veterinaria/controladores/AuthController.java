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
import com.veterinaria.servicios.AuthServicio;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth") // AQUI: Ruta base corregida
@CrossOrigin(origins = "http://localhost:4200")
public class AuthController {

    @Autowired
    private AuthServicio authServicio;

    @PostMapping("/registro") // AQUI: Endpoint específico
    public ResponseEntity<MensajeResponseDTO> registrarCliente(@Valid @RequestBody RegistroClienteDTO dto) {
        MensajeResponseDTO respuesta = authServicio.registrarCliente(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO dto) {
        AuthResponseDTO respuesta = authServicio.login(dto);
        return ResponseEntity.ok(respuesta);

    }

    @PostMapping("/cambiar-password")
    public ResponseEntity<MensajeResponseDTO> cambiarPassword(@Valid @RequestBody CambiarPasswordRequestDTO dto) {
        // Extraemos el email del token JWT actual
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        MensajeResponseDTO respuesta = authServicio.cambiarPassword(email, dto);
        return ResponseEntity.ok(respuesta);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponseDTO> refresh(@Valid @RequestBody RefreshTokenRequestDTO dto) {
        AuthResponseDTO respuesta = authServicio.refreshToken(dto.getRefreshToken());
        return ResponseEntity.ok(respuesta);
    }

    @PostMapping("/logout")
    public ResponseEntity<MensajeResponseDTO> logout(@Valid @RequestBody RefreshTokenRequestDTO dto) {
        MensajeResponseDTO respuesta = authServicio.logout(dto.getRefreshToken());
        return ResponseEntity.ok(respuesta);
    }

    @PostMapping("/confirmar-token")
    public ResponseEntity<MensajeResponseDTO> confirmarToken(@Valid @RequestBody com.veterinaria.dtos.ConfirmarTokenRequestDTO dto) {
        MensajeResponseDTO respuesta = authServicio.confirmarToken(dto.getToken(), dto.getPassword());
        return ResponseEntity.ok(respuesta);
    }
}