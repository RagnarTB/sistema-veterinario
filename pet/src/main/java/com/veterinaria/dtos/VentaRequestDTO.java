package com.veterinaria.dtos;

import java.util.List;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class VentaRequestDTO {
    @NotNull(message = "El ID del cliente es obligatorio")
    @Positive(message = "El ID del cliente debe ser positivo")
    private Long clienteId;

    // No pedimos pacienteId porque el cliente podría venir solo a comprar comida
    // No pedimos total, fecha ni hora (eso lo calcula el Backend por seguridad)

    @NotNull(message = "La sede es obligatoria")
    @Positive(message = "El ID de sede debe ser positivo")
    private Long sedeId;

    @NotNull(message = "El método de pago es obligatorio")
    private com.veterinaria.modelos.Enums.MetodoPago metodoPago;

    @NotEmpty(message = "La venta debe tener al menos un detalle")
    @Valid
    private List<DetalleVentaRequestDTO> detalles;
}