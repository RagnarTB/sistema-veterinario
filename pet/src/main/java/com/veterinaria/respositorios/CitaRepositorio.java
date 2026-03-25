package com.veterinaria.respositorios;

import org.springframework.data.jpa.repository.JpaRepository;

import com.veterinaria.modelos.Cita;

public interface CitaRepositorio extends JpaRepository<Cita, Long> {

}
