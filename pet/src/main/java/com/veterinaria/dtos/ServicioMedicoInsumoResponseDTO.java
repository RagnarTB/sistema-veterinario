package com.veterinaria.dtos;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServicioMedicoInsumoResponseDTO {
    private Long id;
    private Long servicioId;
    private Long productoId;
    private String productoNombre;
    private String unidadVentaNombre;
    private Boolean permiteDecimales;
    private BigDecimal cantidadEstimada;
    private String notas;
    private Boolean activo;
}
