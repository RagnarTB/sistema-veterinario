package com.veterinaria.respositorios;

import java.time.LocalDate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.veterinaria.modelos.DiaBloqueado;

public interface DiaBloqueadoRepositorio extends JpaRepository<DiaBloqueado, Long> {

    // Verificamos si la clínica entera cierra (veterinario IS NULL) o si ese doctor
    // en específico no atiende hoy
    @Query("SELECT CASE WHEN COUNT(d) > 0 THEN true ELSE false END FROM DiaBloqueado d " +
            "WHERE d.fecha = :fecha AND (d.veterinario IS NULL OR d.veterinario.id = :veterinarioId)")
    boolean estaBloqueadoElDia(@Param("fecha") LocalDate fecha, @Param("veterinarioId") Long veterinarioId);
}