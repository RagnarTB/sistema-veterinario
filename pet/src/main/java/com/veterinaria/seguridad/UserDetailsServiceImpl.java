package com.veterinaria.seguridad;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.veterinaria.modelos.Usuario;
import com.veterinaria.respositorios.UsuarioRepositorio;

import java.util.stream.Collectors;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UsuarioRepositorio usuarioRepositorio;

    public UserDetailsServiceImpl(UsuarioRepositorio usuarioRepositorio) {
        this.usuarioRepositorio = usuarioRepositorio;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // 1. Buscamos nuestro usuario en la base de datos por email
        Usuario usuario = usuarioRepositorio.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con email: " + email));

        // El usuario debe estar activo a nivel de cuenta base
        if (!usuario.getActivo()) {
            throw new UsernameNotFoundException("Su cuenta ha sido desactivada por el administrador.");
        }

        // 2. Filtramos los roles basándonos en su estado y el estado de su vinculación (Cliente/Empleado)
        var authorities = usuario.getRoles().stream()
                .filter(rol -> {
                    // El rol en sí debe estar activo
                    if (rol.getActivo() != null && !rol.getActivo()) return false;

                    String nombreRol = rol.getNombre();
                    
                    // Si es Cliente, validamos el estado en la tabla clientes
                    if ("ROLE_CLIENTE".equals(nombreRol)) {
                        return usuario.getCliente() != null && Boolean.TRUE.equals(usuario.getCliente().getActivo());
                    }
                    
                    // Si es un rol de personal (Admin, Vet, Rec), validamos el estado en la tabla empleados
                    if (nombreRol.equals("ROLE_ADMIN") || nombreRol.equals("ROLE_VETERINARIO") || nombreRol.equals("ROLE_RECEPCIONISTA")) {
                        return usuario.getEmpleado() != null && Boolean.TRUE.equals(usuario.getEmpleado().getActivo());
                    }

                    return true; // Otros roles (si existieran) pasan si no están explícitamente bloqueados
                })
                .map(rol -> new SimpleGrantedAuthority(rol.getNombre()))
                .collect(Collectors.toList());

        // Si después de filtrar no queda ningún rol activo, el usuario no puede entrar
        if (authorities.isEmpty()) {
            throw new UsernameNotFoundException("No tiene roles activos para acceder al sistema.");
        }

        // 3. Retornamos el objeto User que Spring Security sí entiende
        return new User(usuario.getEmail(), usuario.getPassword(), authorities);
    }
}