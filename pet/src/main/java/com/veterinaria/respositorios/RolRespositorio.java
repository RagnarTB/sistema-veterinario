package com.veterinaria.respositorios;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.veterinaria.modelos.Rol;

public interface RolRespositorio extends JpaRepository<Rol, Long> {

    Optional<Rol> findByNombre(String nombre);
    boolean existsByNombreIgnoreCase(String nombre);

}
