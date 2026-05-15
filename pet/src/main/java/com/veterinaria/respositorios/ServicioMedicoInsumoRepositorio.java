package com.veterinaria.respositorios;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.veterinaria.modelos.ServicioMedicoInsumo;

public interface ServicioMedicoInsumoRepositorio extends JpaRepository<ServicioMedicoInsumo, Long> {
    List<ServicioMedicoInsumo> findByServicioMedicoId(Long servicioId);
    List<ServicioMedicoInsumo> findByServicioMedicoIdAndActivoTrue(Long servicioId);
    boolean existsByServicioMedicoIdAndProductoIdAndActivoTrue(Long servicioId, Long productoId);
    boolean existsByServicioMedicoIdAndProductoIdAndActivoTrueAndIdNot(Long servicioId, Long productoId, Long id);
}
