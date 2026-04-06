package com.veterinaria.respositorios;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.veterinaria.modelos.Cita;
import com.veterinaria.modelos.Enums.EstadoCita; // Importamos el Enum

public interface CitaRepositorio extends JpaRepository<Cita, Long> {

        @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Cita c " +
                        "WHERE c.veterinario.id = :veterinarioId " +
                        "AND c.fecha = :fecha " +
                        // SOLUCIÓN: Usamos NOT IN con el parámetro dinámico
                        "AND c.estado NOT IN :estadosIgnorados " +
                        "AND c.horaInicio < :nuevaHoraFin " +
                        "AND c.horaFin > :nuevaHoraInicio " +
                        "AND c.id != :citaIgnoradaId")
        boolean existeCruceDeHorario(
                        @Param("veterinarioId") Long veterinarioId,
                        @Param("fecha") LocalDate fecha,
                        @Param("nuevaHoraInicio") LocalTime nuevaHoraInicio,
                        @Param("nuevaHoraFin") LocalTime nuevaHoraFin,
                        @Param("citaIgnoradaId") Long citaIgnoradaId,
                        @Param("estadosIgnorados") List<EstadoCita> estadosIgnorados);

        // Buscamos todas las citas ACTIVAS de un doctor en una fecha específica para
        // esquivarlas
        @Query("SELECT c FROM Cita c WHERE c.veterinario.id = :veterinarioId AND c.fecha = :fecha " +
                        "AND c.estado NOT IN :estadosIgnorados ORDER BY c.horaInicio ASC")
        List<Cita> buscarCitasAgendadasDelDia(
                        @Param("veterinarioId") Long veterinarioId,
                        @Param("fecha") LocalDate fecha,
                        @Param("estadosIgnorados") List<EstadoCita> estadosIgnorados);

        // Solucionando el N+1 para las Citas
        @EntityGraph(attributePaths = { "pacientes", "servicio", "veterinario" })
        Page<Cita> findAll(Pageable pageable);

        @EntityGraph(attributePaths = { "pacientes", "servicio", "veterinario" })
        Optional<Cita> findById(Long id);
}
