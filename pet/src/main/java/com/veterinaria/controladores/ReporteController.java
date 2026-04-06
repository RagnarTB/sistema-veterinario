package com.veterinaria.controladores;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.veterinaria.dtos.CitasVeterinarioDTO;
import com.veterinaria.dtos.DashboardResumenDTO;
import com.veterinaria.dtos.TopProductoDTO;
import com.veterinaria.servicios.ReporteServicio;

@RestController
@RequestMapping("/api/reportes")
@PreAuthorize("hasRole('ADMIN')") // Solo administradores pueden ver los reportes
public class ReporteController {

    private final ReporteServicio reporteServicio;

    public ReporteController(ReporteServicio reporteServicio) {
        this.reporteServicio = reporteServicio;
    }

    // GET /api/reportes/dashboard
    // Resumen ejecutivo: ventas del mes y clientes activos
    @GetMapping("/dashboard")
    public ResponseEntity<DashboardResumenDTO> obtenerDashboard() {
        return ResponseEntity.ok(reporteServicio.obtenerResumenDashboard());
    }

    // GET /api/reportes/top-productos
    // Top 5 productos más vendidos del historial
    @GetMapping("/top-productos")
    public ResponseEntity<List<TopProductoDTO>> obtenerTopProductos() {
        return ResponseEntity.ok(reporteServicio.obtenerTopProductos());
    }

    // GET /api/reportes/rendimiento-veterinarios
    // Ranking de veterinarios por citas completadas
    @GetMapping("/rendimiento-veterinarios")
    public ResponseEntity<List<CitasVeterinarioDTO>> obtenerRendimientoVeterinarios() {
        return ResponseEntity.ok(reporteServicio.obtenerRendimientoVeterinarios());
    }
}
