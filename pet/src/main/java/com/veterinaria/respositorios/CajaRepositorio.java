package com.veterinaria.respositorios;

import java.util.Optional; // ¡Importante!
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.veterinaria.modelos.CajaDiaria;

@Repository
public interface CajaRepositorio extends JpaRepository<CajaDiaria, Long> {

    // Búsqueda de la caja abierta de un empleado en una sede específica
    Optional<CajaDiaria> findByEmpleadoIdAndSedeIdAndEstado(Long empleadoId, Long sedeId, String estado);

    // Búsqueda de cualquier caja abierta del empleado en cualquier sede
    Optional<CajaDiaria> findByEmpleadoIdAndEstado(Long empleadoId, String estado);
}