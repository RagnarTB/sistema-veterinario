package com.veterinaria.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class SunatRucResponseDTO {
    @JsonProperty("razon_social")
    private String razonSocial;

    @JsonProperty("numero_documento")
    private String numeroDocumento;

    private String estado;
    private String condicion;
    private String direccion;
    private String distrito;
    private String provincia;
    private String departamento;
}
