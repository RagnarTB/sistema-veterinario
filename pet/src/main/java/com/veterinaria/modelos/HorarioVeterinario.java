package com.veterinaria.modelos;

import java.time.DayOfWeek;
import java.time.LocalTime;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "horarios_veterinarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HorarioVeterinario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "veterinario_id", nullable = false)
    private Usuario veterinario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DayOfWeek diaSemana; // MONDAY, TUESDAY, etc.

    @Column(nullable = false)
    private LocalTime horaEntrada;

    @Column(nullable = false)
    private LocalTime horaSalida;

    // Opcionales por si el doctor tiene hora de almuerzo
    private LocalTime inicioRefrigerio;
    private LocalTime finRefrigerio;
}