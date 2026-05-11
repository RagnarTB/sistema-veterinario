package com.veterinaria.respositorios;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.veterinaria.modelos.UnidadMedida;

public interface UnidadMedidaRepositorio extends JpaRepository<UnidadMedida, Long> {
    List<UnidadMedida> findByActivoTrue();
}
