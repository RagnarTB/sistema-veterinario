package com.veterinaria.modelos;

import java.time.LocalDateTime;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
public class TokenPreRegistro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;

    private String token;

    private LocalDateTime fechaExpiracion;

    public TokenPreRegistro(String email, String token, LocalDateTime fechaExpiracion) {
        this.email = email;
        this.token = token;
        this.fechaExpiracion = fechaExpiracion;
    }
}
