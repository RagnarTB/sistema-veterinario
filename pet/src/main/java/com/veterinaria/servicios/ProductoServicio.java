package com.veterinaria.servicios;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.veterinaria.dtos.ProductoRequestDTO;
import com.veterinaria.dtos.ProductoResponseDTO;
import com.veterinaria.modelos.InventarioSede;
import com.veterinaria.modelos.Producto;
import com.veterinaria.respositorios.InventarioSedeRepositorio;
import com.veterinaria.respositorios.ProductoRepositorio;
import com.veterinaria.respositorios.CategoriaProductoRepositorio;
import com.veterinaria.respositorios.UnidadMedidaRepositorio;

@Service
public class ProductoServicio {

    private final ProductoRepositorio productoRepositorio;
    private final InventarioSedeRepositorio inventarioSedeRepositorio;
    private final CategoriaProductoRepositorio categoriaRepositorio;
    private final UnidadMedidaRepositorio unidadRepositorio;

    public ProductoServicio(
            ProductoRepositorio productoRepositorio, 
            InventarioSedeRepositorio inventarioSedeRepositorio,
            CategoriaProductoRepositorio categoriaRepositorio,
            UnidadMedidaRepositorio unidadRepositorio) {
        this.productoRepositorio = productoRepositorio;
        this.inventarioSedeRepositorio = inventarioSedeRepositorio;
        this.categoriaRepositorio = categoriaRepositorio;
        this.unidadRepositorio = unidadRepositorio;
    }

    // =========================
    // POST /api/productos
    // =========================
    public ProductoResponseDTO guardar(ProductoRequestDTO dto) {
        Producto producto = new Producto();
        producto.setNombre(normalizarTexto(dto.getNombre()));
        producto.setDescripcion(dto.getDescripcion());
        producto.setPrecio(dto.getPrecio());
        producto.setMarca(normalizarTexto(dto.getMarca()));
        if (dto.getFactorConversion() != null) {
            producto.setFactorConversion(dto.getFactorConversion());
        }

        if (dto.getCategoriaId() != null) {
            producto.setCategoria(categoriaRepositorio.findById(dto.getCategoriaId()).orElse(null));
        }
        if (dto.getUnidadCompraId() != null) {
            producto.setUnidadCompra(unidadRepositorio.findById(dto.getUnidadCompraId()).orElse(null));
        }
        if (dto.getUnidadVentaId() != null) {
            producto.setUnidadVenta(unidadRepositorio.findById(dto.getUnidadVentaId()).orElse(null));
        }

        Producto productoGuardado = productoRepositorio.save(producto);
        return mapearAResponseDTO(productoGuardado);
    }

    // =========================
    // GET /api/productos
    // =========================
    public Page<ProductoResponseDTO> listarTodos(String buscar, Pageable pageable) {
        Page<Producto> pagina;
        if (buscar != null && !buscar.trim().isEmpty()) {
            pagina = productoRepositorio.findByNombreContainingIgnoreCase(buscar, pageable);
        } else {
            pagina = productoRepositorio.findAll(pageable);
        }
        return pagina.map(this::mapearAResponseDTO);
    }

    // =========================
    // GET /api/productos/{id}
    // =========================
    public ProductoResponseDTO buscarPorId(Long id) {
        return productoRepositorio.findById(id)
                .map(this::mapearAResponseDTO)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Producto no encontrado con ID: " + id));
    }

    // =========================
    // PUT /api/productos/{id}
    // =========================
    public ProductoResponseDTO actualizar(Long id, ProductoRequestDTO dto) {
        Producto productodb = productoRepositorio.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Producto no encontrado con ID: " + id));

        productodb.setNombre(normalizarTexto(dto.getNombre()));
        productodb.setDescripcion(dto.getDescripcion());
        productodb.setPrecio(dto.getPrecio());
        productodb.setMarca(normalizarTexto(dto.getMarca()));
        if (dto.getFactorConversion() != null) {
            productodb.setFactorConversion(dto.getFactorConversion());
        }

        if (dto.getCategoriaId() != null) {
            productodb.setCategoria(categoriaRepositorio.findById(dto.getCategoriaId()).orElse(null));
        } else {
            productodb.setCategoria(null);
        }

        if (dto.getUnidadCompraId() != null) {
            productodb.setUnidadCompra(unidadRepositorio.findById(dto.getUnidadCompraId()).orElse(null));
        } else {
            productodb.setUnidadCompra(null);
        }

        if (dto.getUnidadVentaId() != null) {
            productodb.setUnidadVenta(unidadRepositorio.findById(dto.getUnidadVentaId()).orElse(null));
        } else {
            productodb.setUnidadVenta(null);
        }

        Producto productoGuardado = productoRepositorio.save(productodb);
        return mapearAResponseDTO(productoGuardado);
    }

    // =========================
    // PATCH /api/productos/{id}/estado
    // =========================
    public void cambiarEstado(Long id, Boolean estado) {
        Producto productodb = productoRepositorio.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Producto no encontrado con id: " + id));
        productodb.setActivo(estado);
        productoRepositorio.save(productodb);
    }

    // =========================
    // GET /api/productos/alertas-stock
    // =========================
    public List<ProductoResponseDTO> obtenerAlertasStock() {
        List<InventarioSede> alertas = inventarioSedeRepositorio.findAlertasStock();

        // Para demo: devolvemos productos únicos (sin duplicar por sede).
        return alertas.stream()
                .map(InventarioSede::getProducto)
                .distinct()
                .map(this::mapearAResponseDTO)
                .collect(Collectors.toList());
    }

    private String normalizarTexto(String texto) {
        if (texto == null) return null;
        // 1. Convertir a minúsculas
        String resultado = texto.toLowerCase();
        // 2. Quitar caracteres extraños (quedarse solo con letras, números y espacios)
        resultado = resultado.replaceAll("[^a-z0-9áéíóúñ\\s]", "");
        // 3. Eliminar espacios repetidos y trim
        resultado = resultado.replaceAll("\\s+", " ").trim();
        return resultado;
    }

    // =========================
    // MAPPER PRIVADO (evita duplicar lógica de mapeo)
    // =========================
    private ProductoResponseDTO mapearAResponseDTO(Producto producto) {
        return new ProductoResponseDTO(
                producto.getId(),
                producto.getNombre(),
                producto.getDescripcion(),
                producto.getPrecio(),
                producto.getActivo(),
                producto.getMarca(),
                producto.getCategoria() != null ? producto.getCategoria().getId() : null,
                producto.getCategoria() != null ? producto.getCategoria().getNombre() : null,
                producto.getUnidadCompra() != null ? producto.getUnidadCompra().getId() : null,
                producto.getUnidadCompra() != null ? producto.getUnidadCompra().getNombre() : null,
                producto.getUnidadVenta() != null ? producto.getUnidadVenta().getId() : null,
                producto.getUnidadVenta() != null ? producto.getUnidadVenta().getNombre() : null,
                producto.getFactorConversion()); 
    }
}
