package com.veterinaria.respositorios;

import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.veterinaria.modelos.Venta;

public interface VentaRepositorio extends JpaRepository<Venta, Long> {

    // ¡LA MAGIA DE JPQL!
    // Sumamos la columna "total" de las ventas cuya fechaHora esté entre Inicio y
    // Fin
    @Query("SELECT SUM(v.total) FROM Venta v WHERE v.caja.id = :cajaId AND v.estado = 'ACTIVA'")
    Double sumarVentasPorCaja(@Param("cajaId") Long cajaId);
}
