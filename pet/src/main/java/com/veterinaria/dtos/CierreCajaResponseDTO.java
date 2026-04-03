package com.veterinaria.dtos;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CierreCajaResponseDTO {
    private Long cajaId;
    private LocalDateTime fechaCierre;
    private Double saldoInicial;
    private Double totalVentas;
    private Double ingresosExtras;
    private Double egresosDevoluciones;
    private Double saldoFinal;
}