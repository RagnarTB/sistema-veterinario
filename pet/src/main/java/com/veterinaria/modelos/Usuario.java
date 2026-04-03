package com.veterinaria.modelos;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "usuarios")
@Data
@NoArgsConstructor
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // El email será nuestro "username" para el login
    private String email;

    // Aquí guardaremos la contraseña CIFRADA (nunca en texto plano)
    private String password;

    // Relación Muchos a Muchos: Un usuario puede tener varios roles
    // FetchType.EAGER es vital aquí: cuando busquemos al usuario, necesitamos traer
    // sus roles de inmediato para ver si tiene permiso de entrar.
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "usuario_roles", joinColumns = @JoinColumn(name = "usuario_id"), inverseJoinColumns = @JoinColumn(name = "rol_id"))
    private Set<Rol> roles = new HashSet<>();

    private Boolean activo = true;

    
}