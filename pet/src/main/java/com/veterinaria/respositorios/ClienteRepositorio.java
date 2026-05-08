package com.veterinaria.respositorios;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.veterinaria.modelos.Cliente;

public interface ClienteRepositorio extends JpaRepository<Cliente, Long> {


    // Conteo de clientes activos para el dashboard
    long countByActivoTrue();

    @org.springframework.data.jpa.repository.Query("SELECT DISTINCT c FROM Cliente c JOIN c.usuario u JOIN u.roles r WHERE r.nombre = 'ROLE_CLIENTE' AND (LOWER(u.nombre) LIKE LOWER(CONCAT('%', :buscar, '%')) OR LOWER(u.apellido) LIKE LOWER(CONCAT('%', :buscar, '%')) OR u.dni LIKE CONCAT('%', :buscar, '%'))")
    Page<Cliente> buscarClientesConRol(@org.springframework.data.repository.query.Param("buscar") String buscar, Pageable pageable);

    @org.springframework.data.jpa.repository.Query("SELECT DISTINCT c FROM Cliente c JOIN c.usuario u JOIN u.roles r WHERE r.nombre = 'ROLE_CLIENTE'")
    Page<Cliente> findAllConRol(Pageable pageable);
}
