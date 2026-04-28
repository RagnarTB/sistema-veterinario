package com.veterinaria.modelos;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "verification_tokens")
@Data
@NoArgsConstructor
public class VerificationToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String token;

    @OneToOne(targetEntity = Cliente.class, fetch = FetchType.EAGER)
    @JoinColumn(nullable = true, name = "cliente_id")
    private Cliente cliente;

    @OneToOne(targetEntity = com.veterinaria.modelos.Empleado.class, fetch = FetchType.EAGER)
    @JoinColumn(nullable = true, name = "empleado_id")
    private com.veterinaria.modelos.Empleado empleado;

    private LocalDateTime fechaExpiracion;

    public VerificationToken(String token, Cliente cliente, LocalDateTime fechaExpiracion) {
        this.token = token;
        this.cliente = cliente;
        this.fechaExpiracion = fechaExpiracion;
    }

    public VerificationToken(String token, com.veterinaria.modelos.Empleado empleado, LocalDateTime fechaExpiracion) {
        this.token = token;
        this.empleado = empleado;
        this.fechaExpiracion = fechaExpiracion;
    }
}
