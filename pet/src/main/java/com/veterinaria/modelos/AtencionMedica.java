package com.veterinaria.modelos;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "atenciones_medicas")
public class AtencionMedica {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String sintomas;
    private String diagnostico;
    private String tratamiento;
    private Double peso;
    private Double temperatura;
    private Integer frecuenciaCardiaca;
    private String resumenIaCliente;
    @OneToOne
    @JoinColumn(name = "cita_id")
    private Cita cita;
}
