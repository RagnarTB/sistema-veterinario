package com.veterinaria.respositorios;

import org.springframework.data.jpa.repository.JpaRepository;

import com.veterinaria.modelos.Cliente;

public interface ClienteRepositorio extends JpaRepository<Cliente, Long> {
    boolean existsByDni(String dni);
}
