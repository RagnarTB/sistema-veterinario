package com.veterinaria.servicios;

import java.util.List;

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
    private ProductoRepositorio productoRepositorio;

    public ProductoServicio(ProductoRepositorio productoRepositorio) {
        this.productoRepositorio = productoRepositorio;
    }

    public ProductoResponseDTO guardar(ProductoRequestDTO dto) {
        Producto producto = new Producto();
        producto.setNombre(dto.getNombre());
        producto.setDescripcion(dto.getDescripcion());
        producto.setPrecio(dto.getPrecio());
        producto.setStockActual(dto.getStockActual());

        Producto productoGuardado = productoRepositorio.save(producto);

        ProductoResponseDTO respuesta = new ProductoResponseDTO();
        respuesta.setId(productoGuardado.getId());
        respuesta.setNombre(productoGuardado.getNombre());
        respuesta.setDescripcion(productoGuardado.getDescripcion());
        respuesta.setPrecio(productoGuardado.getPrecio());
        respuesta.setStockActual(productoGuardado.getStockActual());

        return respuesta;

    }

    public Page<ProductoResponseDTO> listarTodos(Pageable pageable) {
        return productoRepositorio.findAll(pageable)
                .map(producto -> new ProductoResponseDTO(
                        producto.getId(),
                        producto.getNombre(),
                        producto.getDescripcion(),
                        producto.getPrecio(),
                        producto.getStockActual()));
    }

    public ProductoResponseDTO buscarPorId(Long id) {
        return productoRepositorio.findById(id)
                .map(producto -> new ProductoResponseDTO(producto.getId(), producto.getNombre(),
                        producto.getDescripcion(), producto.getPrecio(), producto.getStockActual()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Producto no encontrado con ID: " + id));
    }

    public ProductoResponseDTO actualizar(Long id, ProductoRequestDTO dto) {
        Producto productodb = productoRepositorio.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado con ID : " + id));
        productodb.setNombre(dto.getNombre());
        productodb.setDescripcion(dto.getDescripcion());
        productodb.setPrecio(dto.getPrecio());
        productodb.setStockActual(dto.getStockActual());

        Producto productoGuardado = productoRepositorio.save(productodb);
        return new ProductoResponseDTO(
                productoGuardado.getId(),
                productoGuardado.getNombre(),
                productoGuardado.getDescripcion(),
                productoGuardado.getPrecio(),
                productoGuardado.getStockActual());
    }

    public void eliminar(Long id) {
        Producto productodb = productoRepositorio.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado con id: " + id));

        productoRepositorio.delete(productodb);
    }

}
