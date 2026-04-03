package com.veterinaria.respositorios;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.veterinaria.modelos.Especie;

@Repository
public interface EspecieRepositorio extends JpaRepository<Especie, Long> {
    // ¡JpaRepository nos regala el .save(), .findAll(), .findById(), etc!
}