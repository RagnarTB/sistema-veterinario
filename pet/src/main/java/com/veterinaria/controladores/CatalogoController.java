package com.veterinaria.controladores;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.veterinaria.modelos.CategoriaProducto;
import com.veterinaria.modelos.Proveedor;
import com.veterinaria.modelos.UnidadMedida;
import com.veterinaria.respositorios.CategoriaProductoRepositorio;
import com.veterinaria.respositorios.LoteInventarioRepositorio;
import com.veterinaria.respositorios.ProductoRepositorio;
import com.veterinaria.respositorios.ProveedorRepositorio;
import com.veterinaria.respositorios.UnidadMedidaRepositorio;

@RestController
@RequestMapping("/api/catalogos")
public class CatalogoController {

    private final CategoriaProductoRepositorio categoriaRepositorio;
    private final UnidadMedidaRepositorio unidadRepositorio;
    private final ProveedorRepositorio proveedorRepositorio;
    private final ProductoRepositorio productoRepositorio;
    private final LoteInventarioRepositorio loteRepositorio;

    public CatalogoController(
            CategoriaProductoRepositorio categoriaRepositorio,
            UnidadMedidaRepositorio unidadRepositorio,
            ProveedorRepositorio proveedorRepositorio,
            ProductoRepositorio productoRepositorio,
            LoteInventarioRepositorio loteRepositorio) {
        this.categoriaRepositorio = categoriaRepositorio;
        this.unidadRepositorio = unidadRepositorio;
        this.proveedorRepositorio = proveedorRepositorio;
        this.productoRepositorio = productoRepositorio;
        this.loteRepositorio = loteRepositorio;
    }

    // ========== CATEGORÍAS ==========
    @GetMapping("/categorias")
    public ResponseEntity<List<CategoriaProducto>> listarCategoriasActivas() {
        return ResponseEntity.ok(categoriaRepositorio.findByActivoTrue());
    }

    @GetMapping("/categorias/todas")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<List<CategoriaProducto>> listarCategoriasTodas() {
        return ResponseEntity.ok(categoriaRepositorio.findAll());
    }

    @PostMapping("/categorias")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<CategoriaProducto> crearCategoria(@RequestBody CategoriaProducto categoria) {
        if (categoria.getNombre() == null || categoria.getNombre().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre de la categoría es obligatorio.");
        }
        if (categoriaRepositorio.existsByNombreIgnoreCase(categoria.getNombre().trim())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ya existe una categoría con ese nombre.");
        }
        categoria.setNombre(categoria.getNombre().trim());
        categoria.setActivo(true);
        return ResponseEntity.status(HttpStatus.CREATED).body(categoriaRepositorio.save(categoria));
    }

    @PutMapping("/categorias/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<CategoriaProducto> actualizarCategoria(@PathVariable Long id, @RequestBody CategoriaProducto dto) {
        CategoriaProducto cat = categoriaRepositorio.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoría no encontrada"));
        
        if (!cat.getNombre().equalsIgnoreCase(dto.getNombre().trim()) && categoriaRepositorio.existsByNombreIgnoreCase(dto.getNombre().trim())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ya existe una categoría con ese nombre.");
        }
        
        cat.setNombre(dto.getNombre().trim());
        cat.setDescripcion(dto.getDescripcion());
        return ResponseEntity.ok(categoriaRepositorio.save(cat));
    }

    @PutMapping("/categorias/{id}/activar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoriaProducto> activarCategoria(@PathVariable Long id) {
        CategoriaProducto cat = categoriaRepositorio.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoría no encontrada"));
        cat.setActivo(true);
        return ResponseEntity.ok(categoriaRepositorio.save(cat));
    }

    @DeleteMapping("/categorias/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> eliminarCategoria(@PathVariable Long id) {
        CategoriaProducto cat = categoriaRepositorio.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoría no encontrada"));
        
        if (cat.getActivo()) {
            // Desactivar siempre es posible
            cat.setActivo(false);
            categoriaRepositorio.save(cat);
            return ResponseEntity.ok(Map.of("accion", "DESACTIVADA", "mensaje", "La categoría fue desactivada."));
        } else {
            // Borrado físico solo si no tiene productos
            boolean enUso = productoRepositorio.existsByCategoriaId(id);
            if (enUso) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se puede eliminar permanentemente la categoría porque tiene productos asociados. Manténgala desactivada.");
            }
            categoriaRepositorio.delete(cat);
            return ResponseEntity.ok(Map.of("accion", "ELIMINADA", "mensaje", "La categoría fue eliminada permanentemente."));
        }
    }

    // ========== UNIDADES ==========
    @GetMapping("/unidades")
    public ResponseEntity<List<UnidadMedida>> listarUnidadesActivas() {
        return ResponseEntity.ok(unidadRepositorio.findByActivoTrue());
    }

    @GetMapping("/unidades/todas")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<List<UnidadMedida>> listarUnidadesTodas() {
        return ResponseEntity.ok(unidadRepositorio.findAll());
    }

    @PostMapping("/unidades")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<UnidadMedida> crearUnidad(@RequestBody UnidadMedida unidad) {
        if (unidad.getNombre() == null || unidad.getNombre().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre de la unidad es obligatorio.");
        }
        if (unidadRepositorio.existsByNombreIgnoreCase(unidad.getNombre().trim())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ya existe una unidad de medida con ese nombre.");
        }
        unidad.setNombre(unidad.getNombre().trim());
        unidad.setActivo(true);
        return ResponseEntity.status(HttpStatus.CREATED).body(unidadRepositorio.save(unidad));
    }

    @PutMapping("/unidades/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<UnidadMedida> actualizarUnidad(@PathVariable Long id, @RequestBody UnidadMedida dto) {
        UnidadMedida uni = unidadRepositorio.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Unidad no encontrada"));
        
        if (!uni.getNombre().equalsIgnoreCase(dto.getNombre().trim()) && unidadRepositorio.existsByNombreIgnoreCase(dto.getNombre().trim())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ya existe una unidad de medida con ese nombre.");
        }
        
        uni.setNombre(dto.getNombre().trim());
        uni.setAbreviatura(dto.getAbreviatura());
        return ResponseEntity.ok(unidadRepositorio.save(uni));
    }

    @PutMapping("/unidades/{id}/activar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UnidadMedida> activarUnidad(@PathVariable Long id) {
        UnidadMedida uni = unidadRepositorio.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Unidad no encontrada"));
        uni.setActivo(true);
        return ResponseEntity.ok(unidadRepositorio.save(uni));
    }

    @DeleteMapping("/unidades/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> eliminarUnidad(@PathVariable Long id) {
        UnidadMedida uni = unidadRepositorio.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Unidad no encontrada"));
        
        if (uni.getActivo()) {
            // Desactivar siempre es posible (soft delete)
            uni.setActivo(false);
            unidadRepositorio.save(uni);
            return ResponseEntity.ok(Map.of("accion", "DESACTIVADA", "mensaje", "La unidad fue desactivada."));
        } else {
            // Para eliminar permanentemente, validamos si está en uso
            boolean enUso = productoRepositorio.existsByUnidadCompraId(id) || productoRepositorio.existsByUnidadVentaId(id);
            if (enUso) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se puede eliminar permanentemente porque tiene productos asociados. Manténgala desactivada.");
            }
            unidadRepositorio.delete(uni);
            return ResponseEntity.ok(Map.of("accion", "ELIMINADA", "mensaje", "La unidad fue eliminada permanentemente."));
        }
    }

    // ========== PROVEEDORES ==========
    @GetMapping("/proveedores")
    public ResponseEntity<List<Proveedor>> listarProveedoresActivos() {
        return ResponseEntity.ok(proveedorRepositorio.findByActivoTrue());
    }

    @GetMapping("/proveedores/todas")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<List<Proveedor>> listarProveedoresTodos() {
        return ResponseEntity.ok(proveedorRepositorio.findAll());
    }

    @PostMapping("/proveedores")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<Proveedor> crearProveedor(@RequestBody Proveedor proveedor) {
        if (proveedor.getRuc() != null && !proveedor.getRuc().trim().isEmpty() && proveedorRepositorio.existsByRuc(proveedor.getRuc().trim())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ya existe un proveedor con ese RUC.");
        }
        if (proveedor.getRuc() != null) proveedor.setRuc(proveedor.getRuc().trim());
        proveedor.setActivo(true);
        return ResponseEntity.status(HttpStatus.CREATED).body(proveedorRepositorio.save(proveedor));
    }

    @PutMapping("/proveedores/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<Proveedor> actualizarProveedor(@PathVariable Long id, @RequestBody Proveedor dto) {
        Proveedor prov = proveedorRepositorio.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Proveedor no encontrado"));
        
        if (dto.getRuc() != null && !dto.getRuc().trim().isEmpty() && !dto.getRuc().trim().equals(prov.getRuc()) && proveedorRepositorio.existsByRuc(dto.getRuc().trim())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ya existe un proveedor con ese RUC.");
        }
        
        if (dto.getRazonSocial() != null) prov.setRazonSocial(dto.getRazonSocial());
        if (dto.getRuc() != null) prov.setRuc(dto.getRuc().trim());
        if (dto.getContacto() != null) prov.setContacto(dto.getContacto());
        if (dto.getTelefono() != null) prov.setTelefono(dto.getTelefono());
        if (dto.getEmail() != null) prov.setEmail(dto.getEmail());
        if (dto.getDireccion() != null) prov.setDireccion(dto.getDireccion());
        return ResponseEntity.ok(proveedorRepositorio.save(prov));
    }

    @PutMapping("/proveedores/{id}/activar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Proveedor> activarProveedor(@PathVariable Long id) {
        Proveedor prov = proveedorRepositorio.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Proveedor no encontrado"));
        prov.setActivo(true);
        return ResponseEntity.ok(proveedorRepositorio.save(prov));
    }

    @DeleteMapping("/proveedores/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> eliminarProveedor(@PathVariable Long id) {
        Proveedor prov = proveedorRepositorio.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Proveedor no encontrado"));
        
        if (prov.getActivo()) {
            // Desactivar siempre es posible
            prov.setActivo(false);
            proveedorRepositorio.save(prov);
            return ResponseEntity.ok(Map.of("accion", "DESACTIVADA", "mensaje", "El proveedor fue desactivado."));
        } else {
            // Borrado físico solo si no tiene lotes
            boolean enUso = loteRepositorio.existsByProveedorId(id);
            if (enUso) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se puede eliminar permanentemente el proveedor porque tiene lotes registrados. Manténgalo desactivado.");
            }
            proveedorRepositorio.delete(prov);
            return ResponseEntity.ok(Map.of("accion", "ELIMINADA", "mensaje", "El proveedor fue eliminado permanentemente."));
        }
    }
}
