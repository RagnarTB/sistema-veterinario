package com.veterinaria.dtos;

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
    private Double total;
    private List<DetalleVentaResponseDTO> detalles;
}