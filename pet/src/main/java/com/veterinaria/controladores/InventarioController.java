package com.veterinaria.controladores;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import jakarta.validation.Valid;
import com.veterinaria.dtos.InventarioRequestDTO;
import com.veterinaria.dtos.IngresoStockDTO;
import com.veterinaria.servicios.InventarioServicio;
import com.veterinaria.modelos.InventarioSede;
import com.veterinaria.dtos.LoteInventarioResponseDTO;
import com.veterinaria.dtos.MovimientoInventarioResponseDTO;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/inventario")
public class InventarioController {
    
    private final InventarioServicio inventarioServicio;

    public InventarioController(InventarioServicio inventarioServicio) {
        this.inventarioServicio = inventarioServicio;
    }

    @PostMapping("/ajustar")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    public InventarioSede ajustar(@Valid @RequestBody InventarioRequestDTO dto) {
        return inventarioServicio.actualizarInventario(dto);
    }

    @PostMapping("/ingreso")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public void registrarIngreso(@Valid @RequestBody IngresoStockDTO dto, Principal principal) {
        inventarioServicio.registrarIngreso(dto, principal.getName());
    }

    @GetMapping("/producto/{productoId}/sede/{sedeId}/lotes")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public List<LoteInventarioResponseDTO> obtenerLotes(@PathVariable Long productoId, @PathVariable Long sedeId) {
        return inventarioServicio.obtenerLotesActivos(productoId, sedeId);
    }

    @GetMapping("/producto/{productoId}/sede/{sedeId}/movimientos")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public List<MovimientoInventarioResponseDTO> obtenerMovimientos(@PathVariable Long productoId, @PathVariable Long sedeId) {
        return inventarioServicio.obtenerMovimientos(productoId, sedeId);
    }
}
