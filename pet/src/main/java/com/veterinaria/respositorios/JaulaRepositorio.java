package com.veterinaria.respositorios;

import com.veterinaria.modelos.Jaula;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JaulaRepositorio extends JpaRepository<Jaula, Long> {
    List<Jaula> findBySedeIdAndEstadoAndActivoTrue(Long sedeId, String estado);
    List<Jaula> findByActivoTrue();
}
