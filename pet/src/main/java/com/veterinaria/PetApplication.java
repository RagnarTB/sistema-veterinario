package com.veterinaria;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import com.veterinaria.modelos.Rol;
import com.veterinaria.respositorios.RolRespositorio;

@EnableJpaAuditing // al arrancar la app se creara una tabla espejo llamada atenciones_medicas_AUD
					// y registrara los cambios automaticamente
@SpringBootApplication
public class PetApplication {

	public static void main(String[] args) {
		SpringApplication.run(PetApplication.class, args);
	}

	// Este código se ejecuta automáticamente una sola vez cuando arranca el
	// servidor
	@Bean
	CommandLineRunner inicializarRoles(RolRespositorio rolRepositorio) {
		return args -> {
			// Si la tabla de roles está vacía, creamos los 4 roles fundamentales
			if (rolRepositorio.count() == 0) {
				rolRepositorio.save(new Rol(null, "ROLE_ADMIN"));
				rolRepositorio.save(new Rol(null, "ROLE_CLIENTE"));
				rolRepositorio.save(new Rol(null, "ROLE_RECEPCIONISTA"));
				rolRepositorio.save(new Rol(null, "ROLE_VETERINARIO"));
				System.out.println(" Roles inicializados en la base de datos");
			}
		};
	}
}