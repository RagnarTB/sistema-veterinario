package com.veterinaria.servicios;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.veterinaria.dtos.InventarioRequestDTO;
import com.veterinaria.dtos.IngresoStockDTO;
import com.veterinaria.dtos.SalidaStockDTO;
import com.veterinaria.dtos.LoteInventarioResponseDTO;
import com.veterinaria.dtos.MovimientoInventarioResponseDTO;
import com.veterinaria.modelos.InventarioSede;
import com.veterinaria.modelos.LoteInventario;
import com.veterinaria.modelos.MovimientoInventario;
import com.veterinaria.modelos.Producto;
import com.veterinaria.modelos.Proveedor;
import com.veterinaria.modelos.Sede;
import com.veterinaria.modelos.TipoMovimiento;
import com.veterinaria.modelos.Empleado;
import com.veterinaria.respositorios.InventarioSedeRepositorio;
import com.veterinaria.respositorios.LoteInventarioRepositorio;
import com.veterinaria.respositorios.MovimientoInventarioRepositorio;
import com.veterinaria.respositorios.ProductoRepositorio;
import com.veterinaria.respositorios.ProveedorRepositorio;
import com.veterinaria.respositorios.SedeRepositorio;
import com.veterinaria.respositorios.EmpleadoRepositorio;

@Service
public class InventarioServicio {

    private final InventarioSedeRepositorio inventarioSedeRepositorio;
    private final ProductoRepositorio productoRepositorio;
    private final SedeRepositorio sedeRepositorio;
    private final LoteInventarioRepositorio loteRepositorio;
    private final MovimientoInventarioRepositorio movimientoRepositorio;
    private final ProveedorRepositorio proveedorRepositorio;
    private final EmpleadoRepositorio empleadoRepositorio;

    public InventarioServicio(
            InventarioSedeRepositorio inventarioSedeRepositorio, 
            ProductoRepositorio productoRepositorio, 
            SedeRepositorio sedeRepositorio,
            LoteInventarioRepositorio loteRepositorio,
            MovimientoInventarioRepositorio movimientoRepositorio,
            ProveedorRepositorio proveedorRepositorio,
            EmpleadoRepositorio empleadoRepositorio) {
        this.inventarioSedeRepositorio = inventarioSedeRepositorio;
        this.productoRepositorio = productoRepositorio;
        this.sedeRepositorio = sedeRepositorio;
        this.loteRepositorio = loteRepositorio;
        this.movimientoRepositorio = movimientoRepositorio;
        this.proveedorRepositorio = proveedorRepositorio;
        this.empleadoRepositorio = empleadoRepositorio;
    }

    @Transactional
    public InventarioSede actualizarInventario(InventarioRequestDTO dto) {
        Producto producto = productoRepositorio.findById(dto.getProductoId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));
        Sede sede = sedeRepositorio.findById(dto.getSedeId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sede no encontrada"));

        InventarioSede inventario = inventarioSedeRepositorio.findByProductoIdAndSedeId(dto.getProductoId(), dto.getSedeId())
                .orElse(new InventarioSede());

        inventario.setProducto(producto);
        inventario.setSede(sede);
        inventario.setStockActual(dto.getStockActual());
        inventario.setStockMinimo(dto.getStockMinimo());

        return inventarioSedeRepositorio.save(inventario);
    }

    @Transactional
    public void registrarIngreso(IngresoStockDTO dto, String emailResponsable) {
        Producto producto = productoRepositorio.findById(dto.getProductoId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));
        Sede sede = sedeRepositorio.findById(dto.getSedeId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sede no encontrada"));
        
        Proveedor proveedor = null;
        if (dto.getProveedorId() != null) {
            proveedor = proveedorRepositorio.findById(dto.getProveedorId()).orElse(null);
        }

        Empleado responsable = empleadoRepositorio.findByUsuarioEmail(emailResponsable).orElse(null);

        // 1. Calcular la cantidad de ingreso en Unidad de Venta
        BigDecimal factor = producto.getFactorConversion() != null ? producto.getFactorConversion() : BigDecimal.ONE;
        BigDecimal cantidadIngresoVenta = dto.getCantidadComprada().multiply(factor);

        // 2. Crear el Lote
        LoteInventario lote = new LoteInventario();
        lote.setNumeroLote(dto.getNumeroLote());
        lote.setFechaVencimiento(dto.getFechaVencimiento());
        lote.setStockRestante(cantidadIngresoVenta);
        lote.setProducto(producto);
        lote.setSede(sede);
        lote.setProveedor(proveedor);
        lote.setActivo(true);
        loteRepositorio.save(lote);

        // 3. Crear el Movimiento (Kardex)
        MovimientoInventario movimiento = new MovimientoInventario();
        movimiento.setProducto(producto);
        movimiento.setSede(sede);
        movimiento.setTipoMovimiento(TipoMovimiento.ENTRADA_COMPRA);
        movimiento.setCantidad(cantidadIngresoVenta);
        movimiento.setMotivo(dto.getMotivo());
        movimiento.setFecha(LocalDateTime.now());
        movimiento.setResponsable(responsable);
        movimientoRepositorio.save(movimiento);

        // 4. Actualizar el Inventario General (Stock global de la sede)
        InventarioSede inventario = inventarioSedeRepositorio.findByProductoIdAndSedeId(producto.getId(), sede.getId())
                .orElseGet(() -> {
                    InventarioSede nuevo = new InventarioSede();
                    nuevo.setProducto(producto);
                    nuevo.setSede(sede);
                    nuevo.setStockActual(BigDecimal.ZERO);
                    nuevo.setStockMinimo(BigDecimal.ZERO);
                    return nuevo;
                });
        
        inventario.setStockActual(inventario.getStockActual().add(cantidadIngresoVenta));
        inventarioSedeRepositorio.save(inventario);
    }

    public List<LoteInventarioResponseDTO> obtenerLotesActivos(Long productoId, Long sedeId) {
        return loteRepositorio.findLotesParaFIFO(productoId, sedeId)
                .stream()
                .map(lote -> new LoteInventarioResponseDTO(
                        lote.getId(),
                        lote.getNumeroLote(),
                        lote.getFechaVencimiento(),
                        lote.getStockRestante(),
                        lote.getProveedor() != null ? lote.getProveedor().getRazonSocial() : null
                ))
                .toList();
    }

    public List<MovimientoInventarioResponseDTO> obtenerMovimientos(Long productoId, Long sedeId) {
        return movimientoRepositorio.findByProductoIdAndSedeIdOrderByFechaDesc(productoId, sedeId)
                .stream()
                .map(mov -> new MovimientoInventarioResponseDTO(
                        mov.getId(),
                        mov.getTipoMovimiento().name(),
                        mov.getCantidad(),
                        mov.getMotivo(),
                        mov.getFecha(),
                        mov.getResponsable() != null
                                ? mov.getResponsable().getUsuario().getNombre() + " " + mov.getResponsable().getUsuario().getApellido()
                                : "Sistema"
                ))
                .toList();
    }

    @Transactional
    public void registrarSalidaAjuste(SalidaStockDTO dto, String emailResponsable) {
        Producto producto = productoRepositorio.findById(dto.getProductoId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));
        Sede sede = sedeRepositorio.findById(dto.getSedeId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sede no encontrada"));

        Empleado responsable = empleadoRepositorio.findByUsuarioEmail(emailResponsable).orElse(null);

        TipoMovimiento tipo = TipoMovimiento.valueOf(dto.getTipoMovimiento());

        boolean esSalida = tipo == TipoMovimiento.SALIDA_CONSUMO_INTERNO
                || tipo == TipoMovimiento.AJUSTE_NEGATIVO
                || tipo == TipoMovimiento.MERMA_VENCIMIENTO;

        // Actualizar inventario global
        InventarioSede inventario = inventarioSedeRepositorio.findByProductoIdAndSedeId(producto.getId(), sede.getId())
                .orElseGet(() -> {
                    InventarioSede nuevo = new InventarioSede();
                    nuevo.setProducto(producto);
                    nuevo.setSede(sede);
                    nuevo.setStockActual(BigDecimal.ZERO);
                    nuevo.setStockMinimo(BigDecimal.ZERO);
                    return nuevo;
                });

        if (esSalida) {
            if (inventario.getStockActual().compareTo(dto.getCantidad()) < 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Stock insuficiente para esta salida.");
            }
            inventario.setStockActual(inventario.getStockActual().subtract(dto.getCantidad()));

            // Descontar FIFO de lotes
            List<LoteInventario> lotes = loteRepositorio.findLotesParaFIFO(producto.getId(), sede.getId());
            BigDecimal restante = dto.getCantidad();
            for (LoteInventario lote : lotes) {
                if (restante.compareTo(BigDecimal.ZERO) <= 0) break;
                BigDecimal disponible = lote.getStockRestante();
                if (disponible.compareTo(restante) >= 0) {
                    lote.setStockRestante(disponible.subtract(restante));
                    restante = BigDecimal.ZERO;
                } else {
                    restante = restante.subtract(disponible);
                    lote.setStockRestante(BigDecimal.ZERO);
                    lote.setActivo(false);
                }
                loteRepositorio.save(lote);
            }
        } else {
            // Ajuste positivo
            inventario.setStockActual(inventario.getStockActual().add(dto.getCantidad()));
        }
        inventarioSedeRepositorio.save(inventario);

        // Registrar movimiento de Kardex
        MovimientoInventario movimiento = new MovimientoInventario();
        movimiento.setProducto(producto);
        movimiento.setSede(sede);
        movimiento.setTipoMovimiento(tipo);
        movimiento.setCantidad(dto.getCantidad());
        movimiento.setMotivo(dto.getMotivo());
        movimiento.setFecha(LocalDateTime.now());
        movimiento.setResponsable(responsable);
        movimientoRepositorio.save(movimiento);
    }
}
