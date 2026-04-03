package com.veterinaria.respositorios;

import org.springframework.data.jpa.repository.JpaRepository;

import com.veterinaria.modelos.HorarioVeterinario;  

public interface HorarioVeterinarioRepositorio extends JpaRepository<HorarioVeterinario,Long>{
    
}
