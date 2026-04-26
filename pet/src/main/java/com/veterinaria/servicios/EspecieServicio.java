package com.veterinaria.servicios;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.veterinaria.dtos.EspecieRequestDTO;
import com.veterinaria.dtos.EspecieResponseDTO;
import com.veterinaria.modelos.Especie;
import com.veterinaria.respositorios.EspecieRepositorio;
import com.veterinaria.respositorios.PacienteRepositorio;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EspecieServicio {

    private final EspecieRepositorio especieRepositorio;
    private final PacienteRepositorio pacienteRepositorio;

    public EspecieServicio(EspecieRepositorio especieRepositorio, PacienteRepositorio pacienteRepositorio) {
        this.especieRepositorio = especieRepositorio;
        this.pacienteRepositorio = pacienteRepositorio;
    }

    // --- C: CREATE ---
    @Transactional
    public EspecieResponseDTO guardar(EspecieRequestDTO dto) {
        Especie especie = new Especie();
        especie.setNombre(dto.getNombre().trim().toUpperCase());
        // El atributo "activo" ya es true por defecto gracias a tu modelo

        Especie especieGuardada = especieRepositorio.save(especie);
        return mapearADTO(especieGuardada);
    }

    // --- R: READ ---
    @Transactional(readOnly = true)
    public List<EspecieResponseDTO> obtenerTodas() {
        return especieRepositorio.findAll().stream()
                .map(this::mapearADTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public EspecieResponseDTO obtenerPorId(Long id) {
        Especie especie = buscarEspeciePorId(id);
        return mapearADTO(especie);
    }

    // --- U: UPDATE ---
    @Transactional
    public EspecieResponseDTO actualizar(Long id, EspecieRequestDTO dto) {
        Especie especie = buscarEspeciePorId(id);
        especie.setNombre(dto.getNombre().trim().toUpperCase());

        Especie especieActualizada = especieRepositorio.save(especie);
        return mapearADTO(especieActualizada);
    }

    // --- D: DELETE (Cambiar Estado o Físico) ---
    @Transactional
    public void cambiarEstado(Long id) {
        Especie especie = buscarEspeciePorId(id);
        
        // Si se intenta desactivar y está vinculado a un paciente, bloquear.
        if (especie.getActivo() && pacienteRepositorio.existsByEspecie_Id(id)) {
            throw new RuntimeException("No se puede desactivar la especie porque está asignada a uno o más pacientes.");
        }
        
        // Si está en true, pasa a false. Si está en false, pasa a true.
        especie.setActivo(!especie.getActivo());
        especieRepositorio.save(especie);
    }

    @Transactional
    public void eliminarFisicamente(Long id) {
        Especie especie = buscarEspeciePorId(id);
        
        if (pacienteRepositorio.existsByEspecie_Id(id)) {
            throw new RuntimeException("No se puede eliminar físicamente la especie porque está asignada a uno o más pacientes.");
        }
        
        especieRepositorio.delete(especie);
    }

    // --- Métodos Auxiliares ---
    private Especie buscarEspeciePorId(Long id) {
        return especieRepositorio.findById(id)
                .orElseThrow(() -> new RuntimeException("No se encontró la especie con el ID: " + id));
        // Nota: Idealmente, cambia RuntimeException por una excepción personalizada
        // (ej. ResourceNotFoundException)
    }

    private EspecieResponseDTO mapearADTO(Especie especie) {
        return new EspecieResponseDTO(
                especie.getId(),
                especie.getNombre(),
                especie.getActivo());
    }
}