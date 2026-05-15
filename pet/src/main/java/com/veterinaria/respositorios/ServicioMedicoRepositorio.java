package com.veterinaria.respositorios;

import org.springframework.data.jpa.repository.JpaRepository;

import com.veterinaria.modelos.ServicioMedico;

public interface ServicioMedicoRepositorio extends JpaRepository<ServicioMedico, Long> {
    boolean existsByNombreIgnoreCase(String nombre);
    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long id);
    java.util.List<ServicioMedico> findByActivo(Boolean activo);
}
