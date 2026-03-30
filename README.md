# 1. Arquitectura Base y Base de Datos

Definimos el Diagrama Entidad-Relación (E-R) enfocado en 4 pilares, usando bases de datos relacionales:

- **Pruebas:** H2 en memoria  
- **Producción:** PostgreSQL  

## Pilares

- **Seguridad:** Usuario, Rol, Usuario_Rol  
- **Pacientes:** Cliente, Paciente  
- **Clínica:** Cita, Atencion_Medica (con `resumen_ia_cliente`), Imagen_Atencion  
- **Ventas (POS):** Producto, Venta, Detalle_Venta  

---

# 2. Metodología Aplicada: TDD (Test-Driven Development)

Hemos aplicado el ciclo estricto de desarrollo guiado por pruebas:

- **Rojo:**  
  Escribimos las pruebas (`PacienteControllerTest`) simulando peticiones HTTP antes de que existiera el código funcional.

- **Verde:**  
  Creamos el código mínimo en las capas para que la prueba pasara.  
  Aislamos la seguridad (`excludeAutoConfiguration`) e inyectamos un simulacro del servicio (`@MockBean`) para aislar la capa web.

- **Refactorización:**  
  Ajustamos nombres, paquetes, implementamos DTOs y validaciones para cumplir con los estándares de la industria.

---

# 3. Vertical Slice (Arquitectura por Capas)

Hemos construido el flujo completo de la API REST para el módulo de Pacientes.

## Capa de Entidades (`Paciente.java`)

Refleja exactamente una tabla en la base de datos.

- Uso de `@Entity` y `@Table`
- Uso de Lombok para reducir código repetitivo

## Capa de Repositorios (`PacienteRepositorio.java`)

Interfaz que se comunica con la base de datos.

- Extiende de `JpaRepository`
- Provee métodos automáticos como:
  - `save`
  - `findById`
  - `findAll`

## Capa de Servicios (`PacienteServicio.java`)

Contiene la lógica de negocio, validaciones y transformación de datos.

- Uso de inyección por constructor
- Encapsula reglas de negocio

## Capa de Controladores (`PacienteController.java`)

Puerta de entrada a la API.

- Sigue estándar REST:
  - Rutas en plural
  - Uso correcto de códigos HTTP
- No interactúa directamente con repositorios
- Solo se comunica con la capa de servicios

---

# 4. Patrón CRUD (Receta estándar para toda la aplicación)

Para mantener la seguridad y limpieza, la API no expone entidades directamente. Se usa el patrón DTO (Data Transfer Object).

## Tipos de DTO

- **RequestDTO:**
  - Representa los datos que envía el cliente
  - Incluye validaciones (`@Valid`, `@NotBlank`)
  - No contiene ID

- **ResponseDTO:**
  - Representa la respuesta al cliente
  - Incluye ID

---

## POST (Crear Recurso)

### Test
Verifica que:
- JSON válido → `201 Created`
- JSON inválido → `400 Bad Request`

### Controlador
- Usa `@PostMapping`
- Recibe `@Valid @RequestBody RequestDTO`
- Retorna `201 Created` con el `ResponseDTO`

### Servicio
1. Recibe el RequestDTO  
2. Crea una nueva entidad  
3. Copia los atributos del DTO a la entidad  
4. Guarda con `repositorio.save()`  
5. Convierte a ResponseDTO y retorna  

---

## GET (Listar Todos)

### Test
Verifica que la petición retorne `200 OK`

### Controlador
- Usa `@GetMapping`
- Retorna `ResponseEntity.ok()`

### Servicio
1. Obtiene datos con `repositorio.findAll()`  
2. Convierte entidades a DTOs usando `stream().map(...)`  
3. Retorna la lista  

---

## GET por ID (Buscar Uno)

### Test
Verifica que la búsqueda por ID retorne `200 OK`

### Controlador
- Usa `@GetMapping("/{id}")`
- Captura el ID con `@PathVariable`

### Servicio
1. Busca con `repositorio.findById(id)`  
2. Si existe, convierte a DTO  
3. Si no existe, lanza excepción `404 Not Found`  

---

## PUT (Actualizar Completo)

### Test
Verifica que la actualización retorne `200 OK`

### Controlador
- Usa `@PutMapping("/{id}")`
- Recibe `@PathVariable` y `@Valid @RequestBody`

### Servicio
1. Busca la entidad o lanza `404 Not Found`  
2. Actualiza los datos  
3. Guarda con `repositorio.save()`  
4. Convierte a ResponseDTO y retorna  

---

## DELETE (Eliminar)

### Test
Verifica que la eliminación retorne `204 No Content`

### Controlador
- Usa `@DeleteMapping("/{id}")`
- Retorna `ResponseEntity.noContent().build()`

### Servicio
1. Verifica existencia o lanza `404 Not Found`  
2. Elimina con `repositorio.delete(entidad)`  
3. No retorna contenido  


# 5. Relaciones entre Entidades (JPA / Hibernate)

Hemos implementado bases de datos relacionales, conectando las tablas mediante el uso de claves foráneas (Foreign Keys) generadas automáticamente por Hibernate.

## Relación Uno a Muchos (1:N) - Cliente y Paciente

En la lógica de negocio:

- Un Cliente tiene muchos Pacientes  
- Un Paciente pertenece a un solo Cliente  

### Lado de los "Muchos" (Paciente.java)

Es la tabla que guarda la llave foránea (ID del cliente).

- Uso de `@ManyToOne(fetch = FetchType.LAZY)`  
  - Define la relación  
  - `FetchType.LAZY` mejora el rendimiento evitando cargar datos innecesarios  

- Uso de `@JoinColumn(name = "cliente_id")`  
  - Define el nombre físico de la columna en la base de datos  

### Lado del "Uno" (Cliente.java)

Uso de la anotación:

    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL, orphanRemoval = true)

- **mappedBy**  
  Indica que la relación es gestionada por la propiedad `cliente` en la entidad Paciente  

- **cascade = CascadeType.ALL**  
  Propaga operaciones (persist, delete, etc.) a los pacientes relacionados  

- **orphanRemoval = true**  
  Elimina automáticamente registros huérfanos  

---

## Relación Uno a Uno (1:1) - Cita y Atención Médica

- Una Cita genera una única Atención Médica.
- Lado fuerte (`AtencionMedica`): Usa `@OneToOne` y `@JoinColumn(name = "cita_id")`.
- Lado inverso (`Cita`): Usa `@OneToOne(mappedBy = "cita", cascade = CascadeType.ALL)`.

---

## Regla de Arquitectura para Relaciones en DTOs

Por seguridad, nunca se envían objetos completos relacionados desde el frontend.

### Enfoque aplicado

- PacienteRequestDTO  
  - Solo recibe `clienteId` (`@NotNull`)  

- PacienteServicio  
  - Inyecta ClienteRepositorio  
  - Busca el cliente por ID  
  - Si existe, realiza la asociación:

        paciente.setCliente(cliente)

---

## Relación Compleja (POS) - Venta, DetalleVenta y Producto

Para el Punto de Venta y el control de inventario, modelamos una estructura de ticket real:
- **Cabecera (Venta):** Contiene la fecha, el total y el cliente (`@ManyToOne`). Una `Venta` tiene muchos detalles (`@OneToMany` con `cascade = CascadeType.ALL`).
- **Filas (DetalleVenta):** Representa cada línea del ticket. Pertenece a una `Venta` (`@ManyToOne`) y está asociado a un `Producto` (`@ManyToOne`). Guarda una "foto" del precio y cantidad exactos en el momento de la transacción.

# 6. Lógica Transaccional y Reglas de Negocio (Módulo POS)

Para el Módulo 4 (Ventas e Inventario), implementamos reglas de negocio estrictas para evitar fugas de inventario y vulnerabilidades de seguridad:

- **Confianza Cero en el Frontend (Zero Trust):** Por seguridad, el `VentaRequestDTO` *solo* recibe el `clienteId`, `productoId` y `cantidad`. El Backend es el único responsable de ir a la base de datos, consultar el precio real, calcular los subtotales, generar el total y estampar la fecha del servidor. Así evitamos que usuarios maliciosos manipulen precios desde el navegador.
- **Integridad con `@Transactional`:** El método de cobro en `VentaServicio` está blindado con la anotación `@Transactional`. Esto asegura que si un cliente pide 3 productos y el último no tiene stock, se lance un `400 Bad Request` y la base de datos revierta toda la operación (Rollback), evitando boletas a medias y stocks descuadrados.

# 7. Entorno Local y Seguridad (Fase de Desarrollo)

Para facilitar el desarrollo y pruebas de la API con herramientas como Postman o Thunder Client, se ha adaptado la configuración de seguridad.

## Configuración actual

- Desactivación temporal de Spring Security:

        @SpringBootApplication(exclude = { SecurityAutoConfiguration.class })

- Se aplica en la clase principal (PetApplication)  
- Evita errores `401 Unauthorized` durante el desarrollo inicial  

## Próximos pasos

- Se eliminará esta exclusión  
- Se implementará:  
  - Autenticación basada en JWT (JSON Web Tokens)  
  - Control de acceso basado en roles (RBAC)  

---

# 8. Mapa de Ruta del Proyecto

Estado actual del desarrollo:

- [x] Módulo 1: Setup y Arquitectura Base  
- [x] Módulo 2: Gestión de Pacientes y Clientes (CRUD y relaciones)  
- [x] Módulo 3: Citas y Atención Médica   
- [x] Módulo 4: Punto de Venta (POS) e Inventario (pendiente)  
- [ ] Módulo 5: Seguridad y Autenticación JWT (pendiente)  
- [ ] Módulo 6: Frontend (Angular) (pendiente)  
- [ ] Módulo 7: Producción (pendiente)  