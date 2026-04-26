package com.veterinaria.respositorios;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.veterinaria.modelos.Paciente;

public interface PacienteRepositorio extends JpaRepository<Paciente, Long> {

    Page<Paciente> findByNombreContainingIgnoreCase(String nombre, Pageable pageable);

    boolean existsByEspecie_Id(Long especieId);
}
