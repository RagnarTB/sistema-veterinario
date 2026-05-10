package com.veterinaria.controladores;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.veterinaria.modelos.CategoriaProducto;
import com.veterinaria.modelos.Proveedor;
import com.veterinaria.modelos.UnidadMedida;
import com.veterinaria.respositorios.CategoriaProductoRepositorio;
import com.veterinaria.respositorios.ProveedorRepositorio;
import com.veterinaria.respositorios.UnidadMedidaRepositorio;

@RestController
@RequestMapping("/api/catalogos")
public class CatalogoController {

    private final CategoriaProductoRepositorio categoriaRepositorio;
    private final UnidadMedidaRepositorio unidadRepositorio;
    private final ProveedorRepositorio proveedorRepositorio;

    public CatalogoController(
            CategoriaProductoRepositorio categoriaRepositorio,
            UnidadMedidaRepositorio unidadRepositorio,
            ProveedorRepositorio proveedorRepositorio) {
        this.categoriaRepositorio = categoriaRepositorio;
        this.unidadRepositorio = unidadRepositorio;
        this.proveedorRepositorio = proveedorRepositorio;
    }

    // ========== CATEGORÍAS ==========
    @GetMapping("/categorias")
    public ResponseEntity<List<CategoriaProducto>> listarCategorias() {
        return ResponseEntity.ok(categoriaRepositorio.findByActivoTrue());
    }

    @PostMapping("/categorias")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<CategoriaProducto> crearCategoria(@RequestBody CategoriaProducto categoria) {
        categoria.setActivo(true);
        return ResponseEntity.status(HttpStatus.CREATED).body(categoriaRepositorio.save(categoria));
    }

    @PutMapping("/categorias/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<CategoriaProducto> actualizarCategoria(@PathVariable Long id, @RequestBody CategoriaProducto dto) {
        CategoriaProducto cat = categoriaRepositorio.findById(id).orElseThrow();
        cat.setNombre(dto.getNombre());
        cat.setDescripcion(dto.getDescripcion());
        return ResponseEntity.ok(categoriaRepositorio.save(cat));
    }

    @DeleteMapping("/categorias/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminarCategoria(@PathVariable Long id) {
        CategoriaProducto cat = categoriaRepositorio.findById(id).orElseThrow();
        cat.setActivo(false);
        categoriaRepositorio.save(cat);
        return ResponseEntity.noContent().build();
    }

    // ========== UNIDADES ==========
    @GetMapping("/unidades")
    public ResponseEntity<List<UnidadMedida>> listarUnidades() {
        return ResponseEntity.ok(unidadRepositorio.findByActivoTrue());
    }

    @PostMapping("/unidades")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<UnidadMedida> crearUnidad(@RequestBody UnidadMedida unidad) {
        unidad.setActivo(true);
        return ResponseEntity.status(HttpStatus.CREATED).body(unidadRepositorio.save(unidad));
    }

    @PutMapping("/unidades/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<UnidadMedida> actualizarUnidad(@PathVariable Long id, @RequestBody UnidadMedida dto) {
        UnidadMedida uni = unidadRepositorio.findById(id).orElseThrow();
        uni.setNombre(dto.getNombre());
        uni.setAbreviatura(dto.getAbreviatura());
        return ResponseEntity.ok(unidadRepositorio.save(uni));
    }

    // ========== PROVEEDORES ==========
    @GetMapping("/proveedores")
    public ResponseEntity<List<Proveedor>> listarProveedores() {
        return ResponseEntity.ok(proveedorRepositorio.findByActivoTrue());
    }

    @PostMapping("/proveedores")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<Proveedor> crearProveedor(@RequestBody Proveedor proveedor) {
        proveedor.setActivo(true);
        return ResponseEntity.status(HttpStatus.CREATED).body(proveedorRepositorio.save(proveedor));
    }

    @PutMapping("/proveedores/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<Proveedor> actualizarProveedor(@PathVariable Long id, @RequestBody Proveedor dto) {
        Proveedor prov = proveedorRepositorio.findById(id).orElseThrow();
        prov.setRazonSocial(dto.getRazonSocial());
        prov.setRuc(dto.getRuc());
        prov.setContacto(dto.getContacto());
        prov.setTelefono(dto.getTelefono());
        prov.setEmail(dto.getEmail());
        prov.setDireccion(dto.getDireccion());
        return ResponseEntity.ok(proveedorRepositorio.save(prov));
    }
}
