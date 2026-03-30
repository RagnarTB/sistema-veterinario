package com.veterinaria.respositorios;

import org.springframework.data.jpa.repository.JpaRepository;

import com.veterinaria.modelos.AtencionMedica;

public interface AtencionMedicaRepositorio extends JpaRepository<AtencionMedica, Long> {

}
