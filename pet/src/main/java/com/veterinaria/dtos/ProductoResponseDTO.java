package com.veterinaria.dtos;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductoResponseDTO {
    private Long id;
    private String nombre;
    private String descripcion;
    private BigDecimal precio;
    private Boolean activo;
    private String marca;
    private Long categoriaId;
    private String categoriaNombre;
    private Long unidadCompraId;
    private String unidadCompraNombre;
    private Long unidadVentaId;
    private String unidadVentaNombre;
    private BigDecimal factorConversion;
}
