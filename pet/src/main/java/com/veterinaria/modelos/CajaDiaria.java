package com.veterinaria.modelos;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "caja_diaria")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CajaDiaria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime fechaApertura;
    private LocalDateTime fechaCierre;

    @Column(nullable = false)
    private Double saldoInicial;

    private Double saldoFinal;

    @Column(nullable = false)
    private String estado; // Guardaremos "ABIERTA" o "CERRADA"

    @OneToMany(mappedBy = "cajaDiaria", cascade = CascadeType.ALL)
    private List<MovimientoCaja> movimientos = new ArrayList<>();
}