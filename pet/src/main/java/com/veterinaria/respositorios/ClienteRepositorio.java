package com.veterinaria.respositorios;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.veterinaria.modelos.Cliente;

public interface ClienteRepositorio extends JpaRepository<Cliente, Long> {
    boolean existsByDni(String dni);
    java.util.Optional<Cliente> findByDni(String dni);

    // Conteo de clientes activos para el dashboard
    long countByActivoTrue();

    Page<Cliente> findByNombreContainingIgnoreCaseOrApellidoContainingIgnoreCaseOrDniContaining(
            String nombre, String apellido, String dni, Pageable pageable);
}
