package com.veterinaria;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.veterinaria.modelos.*;
import com.veterinaria.respositorios.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@EnableJpaAuditing // al arrancar la app se creara una tabla espejo llamada atenciones_medicas_AUD
					// y registrara los cambios automaticamente
@EnableScheduling
@SpringBootApplication
public class PetApplication {

	public static void main(String[] args) {
		SpringApplication.run(PetApplication.class, args);
	}

	// Este código se ejecuta automáticamente una sola vez cuando arranca el
	// servidor
	@Bean
	CommandLineRunner inicializarDatos(RolRespositorio rolRepositorio,
			UsuarioRepositorio usuarioRepositorio,
			EmpleadoRepositorio empleadoRepositorio,
			SedeRepositorio sedeRepositorio,
			PasswordEncoder passwordEncoder) {
		return args -> {
			// 1. Inicializar Roles
			if (rolRepositorio.count() == 0) {
				rolRepositorio.save(new Rol(null, "ROLE_ADMIN"));
				rolRepositorio.save(new Rol(null, "ROLE_CLIENTE"));
				rolRepositorio.save(new Rol(null, "ROLE_RECEPCIONISTA"));
				rolRepositorio.save(new Rol(null, "ROLE_VETERINARIO"));
				System.out.println("✅ Roles inicializados");
			}

			// 2. Inicializar Sede por defecto
			Sede sedePrincipal;
			if (sedeRepositorio.count() == 0) {
				sedePrincipal = new Sede();
				sedePrincipal.setNombre("Sede Central");
				sedePrincipal.setDireccion("Av. Principal 123");
				sedePrincipal.setTelefono("999999999");
				sedePrincipal.setActivo(true);
				sedePrincipal = sedeRepositorio.save(sedePrincipal);
				System.out.println("✅ Sede inicializada");
			} else {
				sedePrincipal = sedeRepositorio.findAll().get(0);
			}

			// 3. Inicializar Usuario Admin y Empleado
			if (usuarioRepositorio.findByEmail("admin@veterinaria.com").isEmpty()) {
				// Buscar el rol ADMIN
				Rol adminRol = rolRepositorio.findByNombre("ROLE_ADMIN")
						.orElseThrow(() -> new RuntimeException("Error: Rol ADMIN no encontrado"));

				// Crear Usuario
				Usuario adminUsuario = new Usuario();
				adminUsuario.setEmail("admin@veterinaria.com");
				adminUsuario.setPassword(passwordEncoder.encode("admin123"));
				adminUsuario.setActivo(true);
				Set<Rol> roles = new HashSet<>();
				roles.add(adminRol);
				adminUsuario.setRoles(roles);
				adminUsuario = usuarioRepositorio.save(adminUsuario);

				// Crear Empleado asociado
				Empleado adminEmpleado = new Empleado();
				adminEmpleado.setNombre("Administrador");
				adminEmpleado.setApellido("Sistema");
				adminEmpleado.setDni("00000000"); // DNI ficticio
				adminEmpleado.setTelefono("999999999");
				adminEmpleado.setSueldoBase(new BigDecimal("3000.00"));
				adminEmpleado.setActivo(true);
				adminEmpleado.setUsuario(adminUsuario);

				// Agregar sede
				Set<Sede> sedes = new HashSet<>();
				sedes.add(sedePrincipal);
				adminEmpleado.setSedes(sedes);

				empleadoRepositorio.save(adminEmpleado);

				System.out.println("✅ Usuario administrador y empleado creados (admin@veterinaria.com / admin123)");
			}

		};
	}
}