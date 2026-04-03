package com.veterinaria.dtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CajaRequestDTO {

    @NotNull(message = "El saldo inicial es obligatorio")
    @Min(value = 0, message = "El saldo inicial no puede ser negativo")
    private Double saldoInicial;

}