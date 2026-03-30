package com.veterinaria.servicios;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.veterinaria.dtos.DetalleVentaRequestDTO;
import com.veterinaria.dtos.DetalleVentaResponseDTO;
import com.veterinaria.dtos.VentaRequestDTO;
import com.veterinaria.dtos.VentaResponseDTO;
import com.veterinaria.modelos.Cliente;
import com.veterinaria.modelos.DetalleVenta;
import com.veterinaria.modelos.Producto;
import com.veterinaria.modelos.Venta;
import com.veterinaria.respositorios.ClienteRepositorio;
import com.veterinaria.respositorios.ProductoRepositorio;
import com.veterinaria.respositorios.VentaRepositorio;

import jakarta.transaction.Transactional;

@Service
public class VentaServicio {

    private VentaRepositorio ventaRepositorio;
    private ClienteRepositorio clienteRepositorio;
    private ProductoRepositorio productoRepositorio;

    public VentaServicio(
            VentaRepositorio ventaRepositorio,
            ClienteRepositorio clienteRepositorio,
            ProductoRepositorio productoRepositorio) {
        this.ventaRepositorio = ventaRepositorio;
        this.clienteRepositorio = clienteRepositorio;
        this.productoRepositorio = productoRepositorio;
    }

    @Transactional
    public VentaResponseDTO guardar(VentaRequestDTO dto) {
        Cliente cliente = clienteRepositorio.findById(dto.getClienteId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Cliente no encontrado con id: " + dto.getClienteId()));

        Venta venta = new Venta();
        venta.setCliente(cliente);
        venta.setFechaHora(LocalDateTime.now());

        double totalVenta = 0.0;

        for (DetalleVentaRequestDTO detalleDto : dto.getDetalles()) {

            Producto producto = productoRepositorio.findById(detalleDto.getProductoId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Producto no encontrado con id: " + detalleDto.getProductoId()));

            if (producto.getStockActual() < detalleDto.getCantidad()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "No hay stock suficiente para: " + producto.getNombre());
            }

            producto.setStockActual(
                    producto.getStockActual() - detalleDto.getCantidad());

            double subtotal = detalleDto.getCantidad() * producto.getPrecio();
            totalVenta += subtotal;

            DetalleVenta nuevoDetalle = new DetalleVenta();
            nuevoDetalle.setProducto(producto);
            nuevoDetalle.setCantidad(detalleDto.getCantidad());
            nuevoDetalle.setPrecioUnitario(producto.getPrecio());
            nuevoDetalle.setSubtotal(subtotal);

            venta.agregarDetalle(nuevoDetalle);
        }

        venta.setTotal(totalVenta);
        Venta ventaGuardada = ventaRepositorio.save(venta);

        return mapearAVentaResponseDTO(ventaGuardada);
    }

    // =========================
    // GET /api/ventas
    // =========================
    public List<VentaResponseDTO> listarTodas() {
        return ventaRepositorio.findAll()
                .stream()
                .map(this::mapearAVentaResponseDTO)
                .collect(Collectors.toList());
    }

    // =========================
    // GET /api/ventas/{id}
    // =========================
    public VentaResponseDTO buscarPorId(Long id) {
        Venta venta = ventaRepositorio.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Venta no encontrada con id: " + id));

        return mapearAVentaResponseDTO(venta);
    }

    // =========================
    // MAPPER PRIVADO
    // =========================
    private VentaResponseDTO mapearAVentaResponseDTO(Venta venta) {
        VentaResponseDTO respuesta = new VentaResponseDTO();
        respuesta.setId(venta.getId());
        respuesta.setClienteId(venta.getCliente().getId());
        respuesta.setFechaHora(venta.getFechaHora());
        respuesta.setTotal(venta.getTotal());

        List<DetalleVentaResponseDTO> detallesDTO = venta.getDetalles()
                .stream()
                .map(detalle -> new DetalleVentaResponseDTO(
                        detalle.getProducto().getId(),
                        detalle.getProducto().getNombre(),
                        detalle.getCantidad(),
                        detalle.getPrecioUnitario(),
                        detalle.getSubtotal()))
                .collect(Collectors.toList());

        respuesta.setDetalles(detallesDTO);

        return respuesta;
    }
}