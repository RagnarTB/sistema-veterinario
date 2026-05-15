package com.veterinaria.servicios;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.veterinaria.dtos.ServicioMedicoInsumoRequestDTO;
import com.veterinaria.dtos.ServicioMedicoInsumoResponseDTO;
import com.veterinaria.modelos.Producto;
import com.veterinaria.modelos.ServicioMedico;
import com.veterinaria.modelos.ServicioMedicoInsumo;
import com.veterinaria.respositorios.ProductoRepositorio;
import com.veterinaria.respositorios.ServicioMedicoInsumoRepositorio;
import com.veterinaria.respositorios.ServicioMedicoRepositorio;

@Service
public class ServicioMedicoInsumoServicio {

    private final ServicioMedicoInsumoRepositorio insumoRepositorio;
    private final ServicioMedicoRepositorio servicioRepositorio;
    private final ProductoRepositorio productoRepositorio;

    public ServicioMedicoInsumoServicio(ServicioMedicoInsumoRepositorio insumoRepositorio,
                                        ServicioMedicoRepositorio servicioRepositorio,
                                        ProductoRepositorio productoRepositorio) {
        this.insumoRepositorio = insumoRepositorio;
        this.servicioRepositorio = servicioRepositorio;
        this.productoRepositorio = productoRepositorio;
    }

    public List<ServicioMedicoInsumoResponseDTO> listarPorServicio(Long servicioId) {
        return insumoRepositorio.findByServicioMedicoId(servicioId).stream()
                .map(this::mapearAResponse)
                .collect(Collectors.toList());
    }

    public ServicioMedicoInsumoResponseDTO agregar(Long servicioId, ServicioMedicoInsumoRequestDTO dto) {
        ServicioMedico servicio = servicioRepositorio.findById(servicioId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Servicio no encontrado"));

        if (!servicio.getActivo()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se pueden agregar insumos a un servicio inactivo");
        }

        Producto producto = productoRepositorio.findById(dto.getProductoId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));

        if (!producto.getActivo()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se puede usar un producto inactivo");
        }

        if (insumoRepositorio.existsByServicioMedicoIdAndProductoIdAndActivoTrue(servicioId, dto.getProductoId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El producto ya está en la plantilla de este servicio");
        }

        validarDecimales(producto, dto.getCantidadEstimada());

        ServicioMedicoInsumo insumo = new ServicioMedicoInsumo();
        insumo.setServicioMedico(servicio);
        insumo.setProducto(producto);
        insumo.setCantidadEstimada(dto.getCantidadEstimada());
        insumo.setNotas(dto.getNotas());

        return mapearAResponse(insumoRepositorio.save(insumo));
    }

    public ServicioMedicoInsumoResponseDTO actualizar(Long id, ServicioMedicoInsumoRequestDTO dto) {
        ServicioMedicoInsumo insumo = insumoRepositorio.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Insumo de servicio no encontrado"));

        validarDecimales(insumo.getProducto(), dto.getCantidadEstimada());

        insumo.setCantidadEstimada(dto.getCantidadEstimada());
        insumo.setNotas(dto.getNotas());

        return mapearAResponse(insumoRepositorio.save(insumo));
    }

    public void cambiarEstado(Long id, Boolean estado) {
        ServicioMedicoInsumo insumo = insumoRepositorio.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Insumo de servicio no encontrado"));
        insumo.setActivo(estado);
        insumoRepositorio.save(insumo);
    }

    public void eliminarPermanente(Long id) {
        if (!insumoRepositorio.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Insumo no encontrado");
        }
        insumoRepositorio.deleteById(id);
    }

    private void validarDecimales(Producto p, BigDecimal cantidad) {
        boolean permiteDecimales = p.getUnidadVenta() != null && p.getUnidadVenta().getPermiteDecimales();
        if (!permiteDecimales && cantidad.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) != 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                "El producto " + p.getNombre() + " no permite cantidades decimales (" + p.getUnidadVenta().getNombre() + ")");
        }
    }

    private ServicioMedicoInsumoResponseDTO mapearAResponse(ServicioMedicoInsumo i) {
        return new ServicioMedicoInsumoResponseDTO(
                i.getId(),
                i.getServicioMedico().getId(),
                i.getProducto().getId(),
                i.getProducto().getNombre(),
                i.getProducto().getUnidadVenta() != null ? i.getProducto().getUnidadVenta().getNombre() : "N/A",
                i.getProducto().getUnidadVenta() != null ? i.getProducto().getUnidadVenta().getPermiteDecimales() : false,
                i.getCantidadEstimada(),
                i.getNotas(),
                i.getActivo()
        );
    }
}
