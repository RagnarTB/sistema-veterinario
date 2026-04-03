package com.veterinaria.respositorios;

import org.springframework.data.jpa.repository.JpaRepository;

import com.veterinaria.modelos.ServicioMedico;

public interface ServicioMedicoRepositorio extends JpaRepository<ServicioMedico, Long> {

}
