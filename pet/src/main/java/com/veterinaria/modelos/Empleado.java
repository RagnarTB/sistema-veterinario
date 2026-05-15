package com.veterinaria.modelos;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.Set;
import java.util.HashSet;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.JoinTable;
import jakarta.persistence.FetchType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "empleados")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Empleado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 20)
    private String numeroColegiatura;

    private String especialidad;

    @Column(precision = 10, scale = 2)
    private BigDecimal sueldoBase;

    @Column(nullable = false)
    private Boolean activo = true;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "empleado_sedes",
        joinColumns = @JoinColumn(name = "empleado_id"),
        inverseJoinColumns = @JoinColumn(name = "sede_id")
    )
    private Set<Sede> sedes = new HashSet<>();

    // Relación OneToOne con la cuenta de autenticación
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", referencedColumnName = "id")
    private Usuario usuario;
}
