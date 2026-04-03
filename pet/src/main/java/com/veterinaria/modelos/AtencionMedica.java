package com.veterinaria.modelos;

import org.hibernate.envers.Audited;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.envers.RelationTargetAuditMode;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "atenciones_medicas")
@Audited // guarda versiones de cambios mejor dicho historial
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
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @OneToOne
    @JoinColumn(name = "cita_id")
    private Cita cita;

    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @ManyToOne
    @JoinColumn(name = "veterinario_id")
    private Usuario veterinario;
}
