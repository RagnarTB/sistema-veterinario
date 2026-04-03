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
        // 1. Buscamos nuestro usuario en la base de datos por email y validamos si esta activo
        Usuario usuario = usuarioRepositorio.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con email: " + email));

        if (!usuario.getActivo()) {
            throw new UsernameNotFoundException("Usuario inactivo");
        }

        // 2. Traducimos nuestros Roles a GrantedAuthorities de Spring Security
        var authorities = usuario.getRoles().stream()
                .map(rol -> new SimpleGrantedAuthority(rol.getNombre()))
                .collect(Collectors.toList());

        // 3. Retornamos el objeto User que Spring Security sí entiende
        return new User(usuario.getEmail(), usuario.getPassword(), authorities);
    }
}