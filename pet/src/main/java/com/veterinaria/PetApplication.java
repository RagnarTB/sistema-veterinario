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
			CategoriaProductoRepositorio categoriaProductoRepositorio,
			UnidadMedidaRepositorio unidadMedidaRepositorio,
			ProveedorRepositorio proveedorRepositorio,
			PasswordEncoder passwordEncoder) {
		return args -> {
			// 1. Inicializar Roles
			if (rolRepositorio.count() == 0) {
				rolRepositorio.save(new Rol(null, "ROLE_ADMIN", true));
				rolRepositorio.save(new Rol(null, "ROLE_CLIENTE", true));
				rolRepositorio.save(new Rol(null, "ROLE_RECEPCIONISTA", true));
				rolRepositorio.save(new Rol(null, "ROLE_VETERINARIO", true));
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
				adminUsuario.setNombre("Administrador");
				adminUsuario.setApellido("Sistema");
				adminUsuario.setDni("00000000"); // DNI ficticio
				adminUsuario.setTelefono("999999999");
				adminUsuario.setEmail("admin@veterinaria.com");
				adminUsuario.setPassword(passwordEncoder.encode("admin123"));
				adminUsuario.setActivo(true);
				Set<Rol> roles = new HashSet<>();
				roles.add(adminRol);
				adminUsuario.setRoles(roles);
				adminUsuario = usuarioRepositorio.save(adminUsuario);

				// Crear Empleado asociado
				Empleado adminEmpleado = new Empleado();
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

			// 4. Inicializar Categorías de Producto
			if (categoriaProductoRepositorio.count() == 0) {
				categoriaProductoRepositorio.save(new CategoriaProducto(null, "Retail y Pet Shop", "Alimentos, accesorios, premios y snacks", true));
				categoriaProductoRepositorio.save(new CategoriaProducto(null, "Farmacia Veterinaria", "Medicamentos, antiparasitarios y biológicos", true));
				categoriaProductoRepositorio.save(new CategoriaProducto(null, "Insumos Clínicos", "Material descartable, suministros quirúrgicos y reactivos", true));
				categoriaProductoRepositorio.save(new CategoriaProducto(null, "Higiene y Cuidado", "Champús, sprays y productos de limpieza", true));
				System.out.println("✅ Categorías de producto inicializadas");
			}

			// 5. Inicializar Unidades de Medida
			if (unidadMedidaRepositorio.count() == 0) {
				unidadMedidaRepositorio.save(new UnidadMedida(null, "Unidad", "Un", false, true));
				unidadMedidaRepositorio.save(new UnidadMedida(null, "Caja", "Cj", false, true));
				unidadMedidaRepositorio.save(new UnidadMedida(null, "Tableta", "Tab", false, true));
				unidadMedidaRepositorio.save(new UnidadMedida(null, "Blíster", "Blíst", false, true));
				unidadMedidaRepositorio.save(new UnidadMedida(null, "Mililitro", "ml", true, true));
				unidadMedidaRepositorio.save(new UnidadMedida(null, "Frasco", "Fr", false, true));
				unidadMedidaRepositorio.save(new UnidadMedida(null, "Saco", "Sc", false, true));
				unidadMedidaRepositorio.save(new UnidadMedida(null, "Kilogramo", "kg", true, true));
				unidadMedidaRepositorio.save(new UnidadMedida(null, "Metro", "m", true, true));
				unidadMedidaRepositorio.save(new UnidadMedida(null, "Kit / Prueba", "Kit", false, true));
				System.out.println("✅ Unidades de medida inicializadas");
			}

			// 6. Inicializar Proveedores de ejemplo
			if (proveedorRepositorio.count() == 0) {
				proveedorRepositorio.save(new Proveedor(null, "Distribuidora VetPharma S.A.C.", "20512345678", "Juan Pérez", "987654321", "ventas@vetpharma.com", "Av. Los Olivos 456", true));
				proveedorRepositorio.save(new Proveedor(null, "Pet Food Importaciones E.I.R.L.", "20587654321", "María López", "912345678", "contacto@petfood.com", "Jr. Comercio 789", true));
				System.out.println("✅ Proveedores de ejemplo inicializados");
			}

		};
	}
}