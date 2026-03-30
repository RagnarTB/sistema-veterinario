package com.veterinaria.dtos;

import java.util.List;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class VentaRequestDTO {
    @NotNull(message = "El ID del cliente es obligatorio")
    private Long clienteId;

    // No pedimos pacienteId porque el cliente podría venir solo a comprar comida
    // No pedimos total, fecha ni hora (eso lo calcula el Backend por seguridad)

    @NotEmpty(message = "La venta debe tener al menos un detalle")
    private List<DetalleVentaRequestDTO> detalles;
}