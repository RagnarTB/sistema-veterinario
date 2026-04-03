package com.veterinaria.respositorios;

import java.util.Optional; // ¡Importante!
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.veterinaria.modelos.CajaDiaria;

@Repository
public interface CajaRepositorio extends JpaRepository<CajaDiaria, Long> {

    // ¡NUEVO! Con solo escribir este nombre, Spring Boot genera la consulta SQL
    // automáticamente
    Optional<CajaDiaria> findByEstado(String estado);
}