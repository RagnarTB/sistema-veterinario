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
import com.veterinaria.modelos.Producto;
import com.veterinaria.respositorios.ProductoRepositorio;

@Service
public class ProductoServicio {

    private final ProductoRepositorio productoRepositorio;

    public ProductoServicio(ProductoRepositorio productoRepositorio) {
        this.productoRepositorio = productoRepositorio;
    }

    // =========================
    // POST /api/productos
    // =========================
    public ProductoResponseDTO guardar(ProductoRequestDTO dto) {
        Producto producto = new Producto();
        producto.setNombre(dto.getNombre());
        producto.setDescripcion(dto.getDescripcion());
        producto.setPrecio(dto.getPrecio());
        producto.setStockActual(dto.getStockActual());
        producto.setStockMinimo(dto.getStockMinimo()); // Nuevo campo

        Producto productoGuardado = productoRepositorio.save(producto);
        return mapearAResponseDTO(productoGuardado);
    }

    // =========================
    // GET /api/productos
    // =========================
    public Page<ProductoResponseDTO> listarTodos(Pageable pageable) {
        return productoRepositorio.findAll(pageable)
                .map(this::mapearAResponseDTO);
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

        productodb.setNombre(dto.getNombre());
        productodb.setDescripcion(dto.getDescripcion());
        productodb.setPrecio(dto.getPrecio());
        productodb.setStockActual(dto.getStockActual());
        productodb.setStockMinimo(dto.getStockMinimo()); // Nuevo campo

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
        return productoRepositorio.obtenerAlertasDeStock()
                .stream()
                .map(this::mapearAResponseDTO)
                .collect(Collectors.toList());
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
                producto.getStockActual(),
                producto.getStockMinimo()); // 6 argumentos — alineado con @AllArgsConstructor
    }
}
