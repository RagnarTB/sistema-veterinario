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
@Table(name = "roles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Rol {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Guardará valores como: "ROLE_ADMIN", "ROLE_CLIENTE", "ROLE_VETERINARIO"
    // Spring Security EXIGE que los roles empiecen con "ROLE_"
    private String nombre;

    private Boolean activo = true;

}