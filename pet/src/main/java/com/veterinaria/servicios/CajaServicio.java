package com.veterinaria.servicios;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.veterinaria.dtos.CajaRequestDTO;
import com.veterinaria.dtos.CierreCajaResponseDTO;
import com.veterinaria.modelos.CajaDiaria;
import com.veterinaria.modelos.MovimientoCaja;
import com.veterinaria.modelos.Enums.TipoMovimiento;
import com.veterinaria.respositorios.CajaRepositorio;
import com.veterinaria.respositorios.VentaRepositorio; // NUEVO IMPORT

@Service
public class CajaServicio {

    private final CajaRepositorio cajaRepositorio;
    private final VentaRepositorio ventaRepositorio; // NUEVO

    // NUEVO Actualizamos el constructor para inyectar ambos repositorios
    public CajaServicio(CajaRepositorio cajaRepositorio, VentaRepositorio ventaRepositorio) {
        this.cajaRepositorio = cajaRepositorio;
        this.ventaRepositorio = ventaRepositorio;
    }

    public void abrirCaja(CajaRequestDTO dto) {

        if (cajaRepositorio.findByEstado("ABIERTA").isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ya existe una caja abierta en este momento");
        }

        CajaDiaria nuevaCaja = new CajaDiaria();
        nuevaCaja.setSaldoInicial(dto.getSaldoInicial());
        nuevaCaja.setEstado("ABIERTA");
        nuevaCaja.setFechaApertura(LocalDateTime.now());

        cajaRepositorio.save(nuevaCaja);
    }

    // Cambiamos 'void' por 'CierreCajaResponseDTO'
    public CierreCajaResponseDTO cerrarCaja() {
        // 1. Buscar la caja abierta usando el Enum
        CajaDiaria cajaAbierta = cajaRepositorio.findByEstado("ABIERTA")
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No hay ninguna caja abierta para cerrar"));

        LocalDateTime ahora = LocalDateTime.now();
        cajaAbierta.setEstado("CERRADA");
        cajaAbierta.setFechaCierre(ahora);

        // 2. Sumar Ventas (Ingresos automáticos por módulo ventas)
        Double totalVentas = ventaRepositorio.sumarVentasPorCaja(cajaAbierta.getId());
        totalVentas = (totalVentas == null) ? 0.0 : totalVentas;

        // 3. Calcular Ingresos y Egresos extras (Movimientos manuales y devoluciones)
        Double ingresosExtras = cajaAbierta.getMovimientos().stream()
                .filter(m -> m.getTipoMovimiento() == TipoMovimiento.INGRESO)
                .mapToDouble(MovimientoCaja::getMonto)
                .sum();

        Double egresosExtras = cajaAbierta.getMovimientos().stream()
                .filter(m -> m.getTipoMovimiento() == TipoMovimiento.EGRESO)
                .mapToDouble(MovimientoCaja::getMonto)
                .sum();

        // 4. ¡NUEVA FÓRMULA DEL ARQUEO!
        double saldoCalculado = cajaAbierta.getSaldoInicial() + totalVentas + ingresosExtras - egresosExtras;
        cajaAbierta.setSaldoFinal(saldoCalculado);

        cajaRepositorio.save(cajaAbierta);

        // 5. Retornamos el "Recibo" detallado para el Frontend
        return new com.veterinaria.dtos.CierreCajaResponseDTO(
                cajaAbierta.getId(),
                cajaAbierta.getFechaCierre(),
                cajaAbierta.getSaldoInicial(),
                totalVentas,
                ingresosExtras,
                egresosExtras,
                saldoCalculado);
    }
}