package com.veterinaria.respositorios;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.veterinaria.modelos.Venta;

public interface VentaRepositorio extends JpaRepository<Venta, Long> {

    @Query("SELECT SUM(v.total) FROM Venta v WHERE v.caja.id = :cajaId AND v.estado = com.veterinaria.modelos.Enums.EstadoVenta.ACTIVA")
    BigDecimal sumarVentasPorCaja(@Param("cajaId") Long cajaId);

    @EntityGraph(attributePaths = { "cliente", "detalles", "detalles.producto", "detalles.servicio" })
    Page<Venta> findAll(Pageable pageable);

    @EntityGraph(attributePaths = { "cliente", "detalles", "detalles.producto", "detalles.servicio" })
    Optional<Venta> findById(Long id);

    // Ingresos del mes: suma de ventas activas filtrando por año y mes
    @Query("SELECT SUM(v.total) FROM Venta v " +
            "WHERE v.estado = com.veterinaria.modelos.Enums.EstadoVenta.ACTIVA " +
            "AND YEAR(v.fechaHora) = :anio " +
            "AND MONTH(v.fechaHora) = :mes")
    BigDecimal sumarVentasPorMesYAnio(@Param("anio") int anio, @Param("mes") int mes);
}