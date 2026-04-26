package com.veterinaria.respositorios;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.veterinaria.modelos.VerificationToken;

@Repository
public interface VerificationTokenRepositorio extends JpaRepository<VerificationToken, Long> {
    Optional<VerificationToken> findByToken(String token);
}
