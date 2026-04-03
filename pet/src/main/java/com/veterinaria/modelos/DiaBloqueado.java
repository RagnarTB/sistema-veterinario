package com.veterinaria.modelos;

import java.time.LocalDate;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "dias_bloqueados")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiaBloqueado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(nullable = false)
    private String motivo; // Ej: "Día del Trabajador", "Fumigación"

    // Opcional: Si es nulo, TODA la clínica cierra. Si tiene un ID, solo ese doctor
    // no atiende.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "veterinario_id")
    private Usuario veterinario;
}