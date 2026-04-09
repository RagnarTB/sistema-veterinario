package com.veterinaria.respositorios;

import com.veterinaria.modelos.Hospitalizacion;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HospitalizacionRepositorio extends JpaRepository<Hospitalizacion, Long> {
    Optional<Hospitalizacion> findByPacienteIdAndEstado(Long pacienteId, String estado);
    
    boolean existsByJaulaIdAndEstado(Long jaulaId, String estado);

    @EntityGraph(attributePaths = { "paciente", "medicoAsignado", "jaula" })
    List<Hospitalizacion> findAll();
}
