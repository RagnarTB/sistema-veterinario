package com.veterinaria.respositorios;

import java.time.DayOfWeek;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.veterinaria.modelos.HorarioVeterinario;

public interface HorarioVeterinarioRepositorio extends JpaRepository<HorarioVeterinario, Long> {
    Optional<HorarioVeterinario> findByVeterinarioIdAndDiaSemana(Long veterinarioId, DayOfWeek diaSemana);
}
