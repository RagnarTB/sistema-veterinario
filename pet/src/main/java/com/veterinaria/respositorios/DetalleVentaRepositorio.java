package com.veterinaria.respositorios;

import org.springframework.data.jpa.repository.JpaRepository;

import com.veterinaria.modelos.DetalleVenta;

public interface DetalleVentaRepositorio extends JpaRepository<DetalleVenta, Long> {
}
