package com.veterinaria.dtos;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResumenDTO {

    private BigDecimal totalVentasMes;
    private Long totalClientesActivos;
}
