package com.veterinaria.servicios;

import org.springframework.stereotype.Service;
import com.veterinaria.dtos.EspecieRequestDTO;
import com.veterinaria.dtos.EspecieResponseDTO;
import com.veterinaria.modelos.Especie;
import com.veterinaria.respositorios.EspecieRepositorio;

@Service
public class EspecieServicio {

    // Inyectamos nuestro nuevo repositorio
    private final EspecieRepositorio especieRepositorio;

    public EspecieServicio(EspecieRepositorio especieRepositorio) {
        this.especieRepositorio = especieRepositorio;
    }

    public EspecieResponseDTO guardar(EspecieRequestDTO dto) {
        // 1. Mapeo de entrada: RequestDTO -> Entidad
        Especie especie = new Especie();
        especie.setNombre(dto.getNombre());

        // 2. Guardar en la base de datos real
        Especie especieGuardada = especieRepositorio.save(especie);

        // 3. Mapeo de salida: Entidad -> ResponseDTO
        return new EspecieResponseDTO(
                especieGuardada.getId(),
                especieGuardada.getNombre());
    }
}