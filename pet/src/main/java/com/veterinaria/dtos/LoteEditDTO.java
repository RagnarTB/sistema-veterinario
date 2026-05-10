package com.veterinaria.dtos;

import lombok.Data;
import java.time.LocalDate;

@Data
public class LoteEditDTO {
    private String numeroLote;
    private LocalDate fechaVencimiento;
    private Long proveedorId;
    private String motivo;
}
