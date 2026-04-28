package com.veterinaria.servicios;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.veterinaria.dtos.RolRequestDTO;
import com.veterinaria.dtos.RolResponseDTO;
import com.veterinaria.modelos.Rol;
import com.veterinaria.respositorios.RolRespositorio;
import com.veterinaria.respositorios.UsuarioRepositorio;

@Service
public class RolServicio {

    private final RolRespositorio rolRepositorio;
    private final UsuarioRepositorio usuarioRepositorio;

    public RolServicio(RolRespositorio rolRepositorio, UsuarioRepositorio usuarioRepositorio) {
        this.rolRepositorio = rolRepositorio;
        this.usuarioRepositorio = usuarioRepositorio;
    }

    public List<RolResponseDTO> listarTodos() {
        return rolRepositorio.findAll().stream()
                .map(rol -> new RolResponseDTO(rol.getId(), rol.getNombre(), rol.getActivo()))
                .collect(Collectors.toList());
    }

    public RolResponseDTO guardar(RolRequestDTO dto) {
        String nombre = dto.getNombre().toUpperCase().trim();
        if (!nombre.startsWith("ROLE_")) {
            nombre = "ROLE_" + nombre;
        }

        if (rolRepositorio.findByNombre(nombre).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El rol ya existe");
        }

        Rol rol = new Rol();
        rol.setNombre(nombre);
        rol.setActivo(true);
        Rol guardado = rolRepositorio.save(rol);

        return new RolResponseDTO(guardado.getId(), guardado.getNombre(), guardado.getActivo());
    }

    public void cambiarEstado(Long id, Boolean estado) {
        Rol rol = rolRepositorio.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Rol no encontrado"));
                
        List<String> rolesProtegidos = List.of("ROLE_ADMIN", "ROLE_CLIENTE", "ROLE_VETERINARIO", "ROLE_RECEPCIONISTA");
        if (rolesProtegidos.contains(rol.getNombre())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se puede cambiar el estado de un rol protegido del sistema");
        }

        rol.setActivo(estado);
        rolRepositorio.save(rol);
    }

    public void eliminar(Long id) {
        Rol rol = rolRepositorio.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Rol no encontrado"));

        // Roles de sistema protegidos
        List<String> rolesProtegidos = List.of("ROLE_ADMIN", "ROLE_CLIENTE", "ROLE_VETERINARIO", "ROLE_RECEPCIONISTA");
        if (rolesProtegidos.contains(rol.getNombre())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se puede eliminar un rol protegido del sistema");
        }

        if (usuarioRepositorio.existsByRoles_Id(id)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se puede eliminar el rol porque tiene usuarios asignados");
        }

        rolRepositorio.delete(rol);
    }
}
