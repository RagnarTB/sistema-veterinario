package com.veterinaria.respositorios;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.veterinaria.dtos.CitasVeterinarioDTO;
import com.veterinaria.modelos.Cita;
import com.veterinaria.modelos.Enums.EstadoCita;

import jakarta.persistence.LockModeType;

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

    // Versión con lock para mitigar carreras en agenda (no elimina todas sin constraint, pero reduce doble-reserva).
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Cita c WHERE c.veterinario.id = :veterinarioId AND c.fecha = :fecha " +
            "AND c.estado NOT IN :estadosIgnorados ORDER BY c.horaInicio ASC")
    List<Cita> buscarCitasAgendadasDelDiaConLock(
            @Param("veterinarioId") Long veterinarioId,
            @Param("fecha") LocalDate fecha,
            @Param("estadosIgnorados") List<EstadoCita> estadosIgnorados);

    @EntityGraph(attributePaths = { "pacientes", "servicio", "veterinario", "sede" })
    Page<Cita> findAll(Pageable pageable);

    @EntityGraph(attributePaths = { "pacientes", "servicio", "veterinario", "sede" })
    Page<Cita> findBySedeId(Long sedeId, Pageable pageable);

    @EntityGraph(attributePaths = { "pacientes", "servicio", "veterinario" })
    Optional<Cita> findById(Long id);

    @Query("SELECT new com.veterinaria.dtos.CitasVeterinarioDTO(usr.email, COUNT(c)) " +
            "FROM Cita c " +
            "JOIN c.veterinario u " +
            "JOIN u.usuario usr " +
            "WHERE c.estado = com.veterinaria.modelos.Enums.EstadoCita.COMPLETADA " +
            "GROUP BY usr.id, usr.email " +
            "ORDER BY COUNT(c) DESC")
    List<CitasVeterinarioDTO> contarCitasCompletadasPorVeterinario();

    @EntityGraph(attributePaths = { "pacientes", "servicio", "veterinario", "sede" })
    @Query("SELECT DISTINCT c FROM Cita c " +
            "LEFT JOIN c.pacientes p " +
            "LEFT JOIN p.cliente cl " +
            "LEFT JOIN cl.usuario ucl " +
            "JOIN c.veterinario v " +
            "JOIN v.usuario uv " +
            "WHERE c.sede.id = :sedeId AND (" +
            "UPPER(p.nombre) LIKE UPPER(CONCAT('%', :buscar, '%')) OR " +
            "UPPER(ucl.nombre) LIKE UPPER(CONCAT('%', :buscar, '%')) OR " +
            "UPPER(ucl.apellido) LIKE UPPER(CONCAT('%', :buscar, '%')) OR " +
            "ucl.dni LIKE CONCAT('%', :buscar, '%') OR " +
            "UPPER(uv.nombre) LIKE UPPER(CONCAT('%', :buscar, '%')) OR " +
            "UPPER(uv.apellido) LIKE UPPER(CONCAT('%', :buscar, '%'))" +
            ")")
    Page<Cita> buscarEnSede(@Param("sedeId") Long sedeId, @Param("buscar") String buscar, Pageable pageable);

    @Query("SELECT COUNT(c) > 0 FROM Cita c WHERE c.veterinario.id = :veterinarioId AND c.fecha BETWEEN :fechaInicio AND :fechaFin AND c.estado IN :estados")
    boolean existenCitasPendientesPorVeterinarioYRangoFechas(
            @Param("veterinarioId") Long veterinarioId,
            @Param("fechaInicio") LocalDate fechaInicio,
            @Param("fechaFin") LocalDate fechaFin,
            @Param("estados") List<EstadoCita> estados);

    @Query("SELECT COUNT(c) > 0 FROM Cita c WHERE c.fecha BETWEEN :fechaInicio AND :fechaFin AND c.estado IN :estados")
    boolean existenCitasPendientesPorRangoFechas(
            @Param("fechaInicio") LocalDate fechaInicio, 
            @Param("fechaFin") LocalDate fechaFin, 
            @Param("estados") List<EstadoCita> estados);
}
