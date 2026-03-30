package com.veterinaria.respositorios;

import org.springframework.data.jpa.repository.JpaRepository;

import com.veterinaria.modelos.Producto;

public interface ProductoRepositorio extends JpaRepository<Producto, Long> {

}
