package com.veterinaria.modelos;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "especies")
public class Especie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Hacemos que el nombre sea único para no tener "Canino" y "canino" repetidos
    @Column(nullable = false, unique = true)
    private String nombre;
}