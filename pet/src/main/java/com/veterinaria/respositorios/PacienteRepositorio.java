package com.veterinaria.respositorios;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.veterinaria.modelos.Paciente;

public interface PacienteRepositorio extends JpaRepository<Paciente, Long> {

    @org.springframework.data.jpa.repository.Query("SELECT p FROM Paciente p WHERE (:estado IS NULL OR p.activo = :estado) AND (LOWER(p.nombre) LIKE LOWER(CONCAT('%', :buscar, '%')) OR LOWER(p.cliente.usuario.nombre) LIKE LOWER(CONCAT('%', :buscar, '%')) OR LOWER(p.cliente.usuario.apellido) LIKE LOWER(CONCAT('%', :buscar, '%')))")
    Page<Paciente> buscarPacientes(@org.springframework.data.repository.query.Param("buscar") String buscar, @org.springframework.data.repository.query.Param("estado") Boolean estado, Pageable pageable);

    @org.springframework.data.jpa.repository.Query("SELECT p FROM Paciente p WHERE (:estado IS NULL OR p.activo = :estado)")
    Page<Paciente> findAllConFiltro(@org.springframework.data.repository.query.Param("estado") Boolean estado, Pageable pageable);

    boolean existsByEspecie_Id(Long especieId);
}
