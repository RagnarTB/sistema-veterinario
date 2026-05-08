package com.veterinaria.respositorios;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.veterinaria.modelos.Cliente;

public interface ClienteRepositorio extends JpaRepository<Cliente, Long> {


    // Conteo de clientes activos para el dashboard
    long countByActivoTrue();

    Page<Cliente> findByUsuarioNombreContainingIgnoreCaseOrUsuarioApellidoContainingIgnoreCaseOrUsuarioDniContaining(
            String nombre, String apellido, String dni, Pageable pageable);
}
