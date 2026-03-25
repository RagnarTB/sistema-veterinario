package com.veterinaria.modelos;

import java.time.LocalDate;
import java.time.LocalTime;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "citas")
public class Cita {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;
    private LocalDate fecha;
    private LocalTime hora;
    private String motivo;
    private EstadoCita estado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id") // La columna en BD se llamará paciente_id
    private Paciente paciente;// En Java, manejamos el objeto completo, no solo el ID

}
