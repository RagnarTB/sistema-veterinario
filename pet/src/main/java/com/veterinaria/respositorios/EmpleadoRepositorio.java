package com.veterinaria.respositorios;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.veterinaria.modelos.Empleado;

public interface EmpleadoRepositorio extends JpaRepository<Empleado, Long> {

    boolean existsByDni(String dni);

    // Método solicitado para encontrar empleado por el email del usuario
    Optional<Empleado> findByUsuarioEmail(String email);
    
    // Método para evitar que el mismo usuario se asigne a más de un empleado
    boolean existsByUsuarioId(Long usuarioId);

    // Verificar si hay empleados asignados a una sede
    boolean existsBySedes_Id(Long sedeId);

    // Búsqueda unificada para la barra de búsqueda de empleados
    Page<Empleado> findByNombreContainingIgnoreCaseOrApellidoContainingIgnoreCaseOrDniContaining(
            String nombre, String apellido, String dni, Pageable pageable);
}
