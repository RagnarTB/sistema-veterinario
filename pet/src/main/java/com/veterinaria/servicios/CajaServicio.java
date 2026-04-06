package com.veterinaria.servicios;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.veterinaria.dtos.CajaRequestDTO;
import com.veterinaria.dtos.CierreCajaResponseDTO;
import com.veterinaria.modelos.CajaDiaria;
import com.veterinaria.modelos.Empleado;
import com.veterinaria.modelos.Enums.TipoMovimiento;
import com.veterinaria.modelos.Sede;
import com.veterinaria.respositorios.CajaRepositorio;
import com.veterinaria.respositorios.VentaRepositorio;
import com.veterinaria.respositorios.SedeRepositorio;

@Service
public class CajaServicio {

        private final CajaRepositorio cajaRepositorio;
        private final VentaRepositorio ventaRepositorio;
        private final SedeRepositorio sedeRepositorio;

        public CajaServicio(CajaRepositorio cajaRepositorio, VentaRepositorio ventaRepositorio,
                        SedeRepositorio sedeRepositorio) {
                this.cajaRepositorio = cajaRepositorio;
                this.ventaRepositorio = ventaRepositorio;
                this.sedeRepositorio = sedeRepositorio;
        }

        public void abrirCaja(CajaRequestDTO dto, Empleado empleadoActual) {

                Sede sede = sedeRepositorio.findById(dto.getSedeId())
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                "Sede no encontrada"));

                // --- VALIDACIÓN CLAVE: el empleado debe pertenecer a esa sede ---
                if (!empleadoActual.getSedes().contains(sede)) {
                        throw new ResponseStatusException(
                                        HttpStatus.FORBIDDEN,
                                        "El empleado no pertenece a la sede solicitada. No puede abrir la caja de otra sede.");
                }

                // VALIDACIÓN: un empleado solo puede tener UNA caja abierta en todo el sistema
                if (cajaRepositorio.findByEmpleadoIdAndEstado(empleadoActual.getId(), "ABIERTA").isPresent()) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                        "Ya tienes una caja abierta en el sistema. Debes cerrarla antes de abrir una nueva.");
                }

                CajaDiaria nuevaCaja = new CajaDiaria();
                nuevaCaja.setSaldoInicial(dto.getSaldoInicial());
                nuevaCaja.setEstado("ABIERTA");
                nuevaCaja.setFechaApertura(LocalDateTime.now());
                nuevaCaja.setSede(sede);
                nuevaCaja.setEmpleado(empleadoActual); // Asignación personal

                cajaRepositorio.save(nuevaCaja);
        }

        public CierreCajaResponseDTO cerrarCaja(Long sedeId, Empleado empleadoActual) {
                Sede sede = sedeRepositorio.findById(sedeId)
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                "Sede no encontrada"));

                // El empleado debe pertenecer a la sede
                if (!empleadoActual.getSedes().contains(sede)) {
                        throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                                        "El empleado no pertenece a la sede solicitada. No puede cerrar la caja de otra sede.");
                }

                // Buscamos la caja abierta ESPECÍFICA de este empleado en esta sede
                CajaDiaria cajaAbierta = cajaRepositorio.findByEmpleadoIdAndSedeIdAndEstado(empleadoActual.getId(), sedeId, "ABIERTA")
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                "No tienes ninguna caja abierta en esta sede para cerrar"));

                LocalDateTime ahora = LocalDateTime.now();
                cajaAbierta.setEstado("CERRADA");
                cajaAbierta.setFechaCierre(ahora);

                BigDecimal totalVentas = ventaRepositorio.sumarVentasPorCaja(cajaAbierta.getId());
                totalVentas = (totalVentas == null) ? BigDecimal.ZERO : totalVentas;

                BigDecimal ingresosExtras = cajaAbierta.getMovimientos().stream()
                                .filter(m -> m.getTipoMovimiento() == TipoMovimiento.INGRESO)
                                .map(m -> m.getMonto())
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal egresosExtras = cajaAbierta.getMovimientos().stream()
                                .filter(m -> m.getTipoMovimiento() == TipoMovimiento.EGRESO)
                                .map(m -> m.getMonto())
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal saldoCalculado = cajaAbierta.getSaldoInicial()
                                .add(totalVentas)
                                .add(ingresosExtras)
                                .subtract(egresosExtras);

                cajaAbierta.setSaldoFinal(saldoCalculado);

                cajaRepositorio.save(cajaAbierta);

                return new CierreCajaResponseDTO(
                                cajaAbierta.getId(),
                                cajaAbierta.getFechaCierre(),
                                cajaAbierta.getSaldoInicial(),
                                totalVentas,
                                ingresosExtras,
                                egresosExtras,
                                saldoCalculado);
        }
}