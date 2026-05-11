package com.veterinaria.servicios;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.veterinaria.dtos.DetalleVentaRequestDTO;
import com.veterinaria.dtos.DetalleVentaResponseDTO;
import com.veterinaria.dtos.MensajeResponseDTO;
import com.veterinaria.dtos.VentaRequestDTO;
import com.veterinaria.dtos.VentaResponseDTO;
import com.veterinaria.modelos.CajaDiaria;
import com.veterinaria.modelos.Cliente;
import com.veterinaria.modelos.DetalleVenta;
import com.veterinaria.modelos.MovimientoCaja;
import com.veterinaria.modelos.Producto;
import com.veterinaria.modelos.ServicioMedico;
import com.veterinaria.modelos.Venta;
import com.veterinaria.modelos.Enums.EstadoVenta;
import com.veterinaria.modelos.Enums.TipoMovimiento;
import com.veterinaria.respositorios.CajaRepositorio;
import com.veterinaria.respositorios.ClienteRepositorio;
import com.veterinaria.respositorios.MovimientoCajaRespositorio;
import com.veterinaria.respositorios.ProductoRepositorio;
import com.veterinaria.respositorios.ServicioMedicoRepositorio;
import com.veterinaria.respositorios.VentaRepositorio;
import com.veterinaria.respositorios.InventarioSedeRepositorio;
import com.veterinaria.respositorios.LoteInventarioRepositorio;
import com.veterinaria.respositorios.MovimientoInventarioRepositorio;
import com.veterinaria.modelos.InventarioSede;
import com.veterinaria.modelos.LoteInventario;
import com.veterinaria.modelos.MovimientoInventario;

import jakarta.transaction.Transactional;

@Service
public class VentaServicio {

    private final VentaRepositorio ventaRepositorio;
    private final ClienteRepositorio clienteRepositorio;
    private final ProductoRepositorio productoRepositorio;
    private final ServicioMedicoRepositorio servicioMedicoRepositorio;
    private final CajaRepositorio cajaRepositorio;
    private final MovimientoCajaRespositorio movimientoCajaRespositorio;
    private final InventarioSedeRepositorio inventarioSedeRepositorio;
    private final LoteInventarioRepositorio loteInventarioRepositorio;
    private final MovimientoInventarioRepositorio movimientoInventarioRepositorio;

    public VentaServicio(VentaRepositorio ventaRepositorio,
            ClienteRepositorio clienteRepositorio,
            ProductoRepositorio productoRepositorio,
            ServicioMedicoRepositorio servicioMedicoRepositorio,
            CajaRepositorio cajaRepositorio,
            MovimientoCajaRespositorio movimientoCajaRespositorio,
            InventarioSedeRepositorio inventarioSedeRepositorio,
            LoteInventarioRepositorio loteInventarioRepositorio,
            MovimientoInventarioRepositorio movimientoInventarioRepositorio) {
        this.ventaRepositorio = ventaRepositorio;
        this.clienteRepositorio = clienteRepositorio;
        this.productoRepositorio = productoRepositorio;
        this.servicioMedicoRepositorio = servicioMedicoRepositorio;
        this.cajaRepositorio = cajaRepositorio;
        this.movimientoCajaRespositorio = movimientoCajaRespositorio;
        this.inventarioSedeRepositorio = inventarioSedeRepositorio;
        this.loteInventarioRepositorio = loteInventarioRepositorio;
        this.movimientoInventarioRepositorio = movimientoInventarioRepositorio;
    }

    // =========================
    // POST /api/ventas
    // =========================
    @Transactional
    public VentaResponseDTO guardar(VentaRequestDTO dto, com.veterinaria.modelos.Empleado empleadoActual) {

        // REGLA DE NEGOCIO: Cada empleado debe tener su propia caja abierta en la sede
        CajaDiaria cajaAbierta = cajaRepositorio.findByEmpleadoIdAndSedeIdAndEstado(empleadoActual.getId(), dto.getSedeId(), "ABIERTA")
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "No se puede realizar la venta porque no has abierto tu caja personal en esta sede"));

        Cliente cliente = clienteRepositorio.findById(dto.getClienteId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Cliente no encontrado con id: " + dto.getClienteId()));

        Venta venta = new Venta();
        venta.setCliente(cliente);
        venta.setFechaHora(LocalDateTime.now());
        venta.setCaja(cajaAbierta);
        venta.setMetodoPago(dto.getMetodoPago());

        BigDecimal totalVenta = BigDecimal.ZERO;

        for (DetalleVentaRequestDTO detalleDto : dto.getDetalles()) {

            // ----------------------------------------------------------
            // VALIDACIÓN PREVIA: debe venir exactamente uno de los dos IDs
            // ----------------------------------------------------------
            boolean tieneProducto = detalleDto.getProductoId() != null;
            boolean tieneServicio = detalleDto.getServicioId() != null;

            if (!tieneProducto && !tieneServicio) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Cada línea del detalle debe tener un 'productoId' o un 'servicioId'.");
            }
            if (tieneProducto && tieneServicio) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Cada línea del detalle no puede tener 'productoId' y 'servicioId' al mismo tiempo.");
            }

            DetalleVenta nuevoDetalle = new DetalleVenta();
            nuevoDetalle.setCantidad(detalleDto.getCantidad());

            if (tieneProducto) {
                // ---- RAMA PRODUCTO: tiene stock, se valida y se descuenta ----
                Producto producto = productoRepositorio.findById(detalleDto.getProductoId())
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Producto no encontrado con id: " + detalleDto.getProductoId()));

                if (!producto.getActivo()) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "El producto '" + producto.getNombre()
                                    + "' está inactivo y no puede venderse.");
                }

                InventarioSede inventario = inventarioSedeRepositorio.findByProductoIdAndSedeId(detalleDto.getProductoId(), dto.getSedeId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "El producto no tiene stock registrado en esta sede."));

                if (inventario.getStockActual().compareTo(detalleDto.getCantidad()) < 0) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Stock insuficiente para el producto: " + producto.getNombre());
                }

                // Descontar stock global
                inventario.setStockActual(inventario.getStockActual().subtract(detalleDto.getCantidad()));
                inventarioSedeRepositorio.save(inventario);

                // ---- FIFO: descontar de lotes (los más próximos a vencer primero) ----
                descontarStockFIFO(detalleDto.getProductoId(), dto.getSedeId(), detalleDto.getCantidad());

                // ---- Registrar movimiento de Kardex ----
                MovimientoInventario movKardex = new MovimientoInventario();
                movKardex.setProducto(producto);
                movKardex.setSede(cajaAbierta.getSede());
                movKardex.setTipoMovimiento(com.veterinaria.modelos.TipoMovimiento.SALIDA_VENTA);
                movKardex.setCantidad(detalleDto.getCantidad());
                movKardex.setMotivo("Venta");
                movKardex.setFecha(LocalDateTime.now());
                movKardex.setResponsable(empleadoActual);
                movimientoInventarioRepositorio.save(movKardex);

                BigDecimal subtotal = producto.getPrecio().multiply(detalleDto.getCantidad());

                nuevoDetalle.setProducto(producto);
                nuevoDetalle.setPrecioUnitario(producto.getPrecio());
                nuevoDetalle.setSubtotal(subtotal);
                totalVenta = totalVenta.add(subtotal);

            } else {
                // ---- RAMA SERVICIO: NO tiene stock, solo se cobra ----
                ServicioMedico servicio = servicioMedicoRepositorio.findById(detalleDto.getServicioId())
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Servicio médico no encontrado con id: " + detalleDto.getServicioId()));

                if (!servicio.getActivo()) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "El servicio '" + servicio.getNombre()
                                    + "' está inactivo y no puede venderse.");
                }

                // Los servicios NO manejan stock → sin validación ni descuento
                BigDecimal subtotal = servicio.getPrecio().multiply(detalleDto.getCantidad());

                nuevoDetalle.setServicio(servicio);
                nuevoDetalle.setPrecioUnitario(servicio.getPrecio());
                nuevoDetalle.setSubtotal(subtotal);
                totalVenta = totalVenta.add(subtotal);
            }

            venta.agregarDetalle(nuevoDetalle);
        }

        venta.setTotal(totalVenta);
        venta.setEstado(EstadoVenta.ACTIVA);

        Venta ventaGuardada = ventaRepositorio.save(venta);
        return mapearAVentaResponseDTO(ventaGuardada);
    }

    // =========================
    // GET /api/ventas
    // =========================
    public Page<VentaResponseDTO> listarTodas(Pageable pageable) {
        return ventaRepositorio.findAll(pageable)
                .map(this::mapearAVentaResponseDTO);
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
    // DELETE /api/ventas/{id}/anular
    // =========================
    @Transactional
    public MensajeResponseDTO anularVenta(Long idVenta, com.veterinaria.modelos.Empleado empleadoActual) {

        Venta venta = ventaRepositorio.findById(idVenta)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Venta no encontrada"));

        if (venta.getEstado() == EstadoVenta.ANULADA) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La venta ya está anulada");
        }
        
        if (venta.getCaja() != null && "CERRADA".equals(venta.getCaja().getEstado())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se puede anular la venta porque la caja original en la que se operó ya está CERRADA.");
        }

        // Para anular, el empleado actual debe tener su caja abierta para registrar la devolución (egreso)
        CajaDiaria cajaAbierta = cajaRepositorio.findByEmpleadoIdAndSedeIdAndEstado(empleadoActual.getId(), venta.getCaja().getSede().getId(), "ABIERTA")
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "No se puede anular: no tienes una caja abierta en esta sede para registrar el egreso (devolución)"));

        if (venta.getMetodoPago() == com.veterinaria.modelos.Enums.MetodoPago.EFECTIVO) {
            BigDecimal ventasActuales = ventaRepositorio.sumarVentasPorCaja(cajaAbierta.getId());
            ventasActuales = (ventasActuales == null) ? BigDecimal.ZERO : ventasActuales;

            if (ventasActuales.compareTo(venta.getTotal()) < 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "No hay suficiente efectivo en caja para devolver el monto de esta venta.");
            }
        }

        // Devolver stock SOLO de los ítems que son productos físicos
        for (DetalleVenta detalle : venta.getDetalles()) {
            if (detalle.getProducto() != null) {
                // 1. Aumentar stock global
                InventarioSede inventario = inventarioSedeRepositorio.findByProductoIdAndSedeId(detalle.getProducto().getId(), venta.getCaja().getSede().getId())
                        .orElse(null);
                if (inventario != null) {
                    inventario.setStockActual(inventario.getStockActual().add(detalle.getCantidad()));
                    inventarioSedeRepositorio.save(inventario);
                }

                // 2. Devolver stock al lote más reciente (o al que no esté vencido)
                List<LoteInventario> lotes = loteInventarioRepositorio.findByProductoIdAndSedeIdOrderByFechaVencimientoDesc(
                    detalle.getProducto().getId(), venta.getCaja().getSede().getId());
                
                if (!lotes.isEmpty()) {
                    LoteInventario loteDestino = lotes.get(0); // El más nuevo o con vencimiento más lejano
                    loteDestino.setStockRestante(loteDestino.getStockRestante().add(detalle.getCantidad()));
                    loteDestino.setActivo(true);
                    loteInventarioRepositorio.save(loteDestino);
                }

                // 3. Registrar movimiento de Kardex por anulación
                MovimientoInventario movKardex = new MovimientoInventario();
                movKardex.setProducto(detalle.getProducto());
                movKardex.setSede(venta.getCaja().getSede());
                movKardex.setTipoMovimiento(com.veterinaria.modelos.TipoMovimiento.AJUSTE_POSITIVO);
                movKardex.setCantidad(detalle.getCantidad());
                movKardex.setMotivo("Devolución por anulación de Venta #" + venta.getId());
                movKardex.setFecha(LocalDateTime.now());
                movKardex.setResponsable(empleadoActual);
                movimientoInventarioRepositorio.save(movKardex);
            }
        }

        MovimientoCaja egreso = new MovimientoCaja();
        egreso.setConcepto("Anulación de Venta ID: " + venta.getId());
        egreso.setMonto(venta.getTotal());
        egreso.setTipoMovimiento(TipoMovimiento.EGRESO);
        egreso.setFechaHora(LocalDateTime.now());
        egreso.setCajaDiaria(cajaAbierta);
        egreso.setMetodoPago(venta.getMetodoPago());
        movimientoCajaRespositorio.save(egreso);

        venta.setEstado(EstadoVenta.ANULADA);
        ventaRepositorio.save(venta);

        return new MensajeResponseDTO("Venta anulada correctamente y stock devuelto");
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
        respuesta.setMetodoPago(venta.getMetodoPago());

        List<DetalleVentaResponseDTO> detallesDTO = venta.getDetalles()
                .stream()
                .map(detalle -> {
                    if (detalle.getProducto() != null) {
                        // Ítem de tipo Producto
                        return new DetalleVentaResponseDTO(
                                detalle.getProducto().getId(), // productoId
                                null,                          // servicioId
                                detalle.getProducto().getNombre(), // nombreItem
                                detalle.getCantidad(),
                                detalle.getPrecioUnitario(),
                                detalle.getSubtotal());
                    } else {
                        // Ítem de tipo Servicio
                        return new DetalleVentaResponseDTO(
                                null,                              // productoId
                                detalle.getServicio().getId(),     // servicioId
                                detalle.getServicio().getNombre(), // nombreItem
                                detalle.getCantidad(),
                                detalle.getPrecioUnitario(),
                                detalle.getSubtotal());
                    }
                })
                .collect(Collectors.toList());

        respuesta.setDetalles(detallesDTO);
        return respuesta;
    }

    // =========================
    // FIFO: Descontar stock de los lotes más antiguos (próximos a vencer)
    // =========================
    private void descontarStockFIFO(Long productoId, Long sedeId, BigDecimal cantidadRequerida) {
        List<LoteInventario> lotes = loteInventarioRepositorio.findLotesParaFIFO(productoId, sedeId);

        BigDecimal restante = cantidadRequerida;

        for (LoteInventario lote : lotes) {
            if (restante.compareTo(BigDecimal.ZERO) <= 0) break;

            BigDecimal disponible = lote.getStockRestante();
            if (disponible.compareTo(restante) >= 0) {
                // Este lote cubre toda la cantidad restante
                lote.setStockRestante(disponible.subtract(restante));
                restante = BigDecimal.ZERO;
            } else {
                // Este lote no alcanza, lo vaciamos y seguimos con el siguiente
                restante = restante.subtract(disponible);
                lote.setStockRestante(BigDecimal.ZERO);
                lote.setActivo(false); // lote agotado
            }
            loteInventarioRepositorio.save(lote);
        }
    }
}