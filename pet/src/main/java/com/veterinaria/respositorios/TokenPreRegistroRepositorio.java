package com.veterinaria.respositorios;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.veterinaria.modelos.TokenPreRegistro;

public interface TokenPreRegistroRepositorio extends JpaRepository<TokenPreRegistro, Long> {
    Optional<TokenPreRegistro> findByToken(String token);
    void deleteByEmail(String email);
}
