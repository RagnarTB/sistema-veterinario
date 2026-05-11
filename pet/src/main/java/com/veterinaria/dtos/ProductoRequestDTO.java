package com.veterinaria.dtos;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProductoRequestDTO {

    @NotBlank(message = "El nombre del producto es obligatorio")
    private String nombre;

    private String descripcion;

    @NotNull(message = "El precio del producto es obligatorio")
    @DecimalMin(value = "0.0", inclusive = true, message = "El precio no puede ser negativo")
    private BigDecimal precio;

    private String marca;
    
    private Long categoriaId;
    private Long unidadCompraId;
    private Long unidadVentaId;
    
    @DecimalMin(value = "0.01", message = "El factor de conversión debe ser mayor a 0")
    private BigDecimal factorConversion;
}
