package com.veterinaria.modelos;

import java.util.List;

import jakarta.annotation.Generated;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "pacientes")
public class Paciente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;

    private String nombre;
    // Actualizacion de la especie con tabla
    @ManyToOne
    @JoinColumn(name = "especie_id", nullable = false)
    private Especie especie;
    private String raza;
    @ManyToOne(fetch = FetchType.LAZY) // "Muchos pacientes pertenecen a Un cliente"
    @JoinColumn(name = "cliente_id") // se llamará la columna en la base de datos
    private Cliente cliente;

    // EL CAMBIO Actualizamos el lado pasivo de la relación
    @ManyToMany(mappedBy = "pacientes")
    private List<Cita> citas;

}
