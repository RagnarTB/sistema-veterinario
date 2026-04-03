package com.veterinaria.respositorios;

import org.springframework.data.jpa.repository.JpaRepository;

import com.veterinaria.modelos.MovimientoCaja;

public interface MovimientoCajaRespositorio extends JpaRepository<MovimientoCaja, Long> {

}
