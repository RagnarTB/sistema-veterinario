package com.veterinaria.dtos;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VentaResponseDTO {
    private Long id;
    private Long clienteId;
    private LocalDateTime fechaHora;
    private BigDecimal total;
    private com.veterinaria.modelos.Enums.MetodoPago metodoPago;
    private List<DetalleVentaResponseDTO> detalles;
}