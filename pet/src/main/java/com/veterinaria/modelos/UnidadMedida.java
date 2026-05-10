package com.veterinaria.modelos;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "unidades_medida")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UnidadMedida {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre; // Ej. Caja, Tableta, Mililitro
    private String abreviatura; // Ej. Cj, Tab, ml
    
    private Boolean permiteDecimales = false;
    private Boolean activo = true;
}
