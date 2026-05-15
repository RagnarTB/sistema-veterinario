package com.veterinaria.respositorios;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.veterinaria.modelos.Empleado;

public interface EmpleadoRepositorio extends JpaRepository<Empleado, Long> {



    // Método solicitado para encontrar empleado por el email del usuario
    Optional<Empleado> findByUsuarioEmail(String email);
    
    // Método para evitar que el mismo usuario se asigne a más de un empleado
    boolean existsByUsuarioId(Long usuarioId);

    // Verificar si hay empleados asignados a una sede
    boolean existsBySedes_Id(Long sedeId);

    // Búsqueda unificada para la barra de búsqueda de empleados
    @Query("SELECT DISTINCT e FROM Empleado e JOIN e.usuario u JOIN u.roles r LEFT JOIN e.sedes s WHERE r.nombre IN ('ROLE_ADMIN', 'ROLE_RECEPCIONISTA', 'ROLE_VETERINARIO') AND (:estado IS NULL OR e.activo = :estado) AND (:sedeId IS NULL OR s.id = :sedeId) AND (LOWER(u.nombre) LIKE LOWER(CONCAT('%', :buscar, '%')) OR LOWER(u.apellido) LIKE LOWER(CONCAT('%', :buscar, '%')) OR u.dni LIKE CONCAT('%', :buscar, '%'))")
    Page<Empleado> buscarEmpleadosConRoles(@Param("buscar") String buscar, @Param("estado") Boolean estado, @Param("sedeId") Long sedeId, Pageable pageable);

    @Query("SELECT DISTINCT e FROM Empleado e JOIN e.usuario u JOIN u.roles r LEFT JOIN e.sedes s WHERE r.nombre IN ('ROLE_ADMIN', 'ROLE_RECEPCIONISTA', 'ROLE_VETERINARIO') AND (:estado IS NULL OR e.activo = :estado) AND (:sedeId IS NULL OR s.id = :sedeId)")
    Page<Empleado> findAllConRoles(@Param("estado") Boolean estado, @Param("sedeId") Long sedeId, Pageable pageable);
}
