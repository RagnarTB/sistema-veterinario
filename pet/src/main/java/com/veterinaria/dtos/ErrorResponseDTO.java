package com.veterinaria.dtos;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

@Data
public class ErrorResponseDTO {
    private LocalDateTime timestamp;
    private Integer status;
    private String error;
    private String message;
    private List<String> detalles; // Útil para mandar la lista de campos que fallaron en el DTO

    public ErrorResponseDTO(Integer status, String error, String message, List<String> detalles) {
        this.timestamp = LocalDateTime.now();
        this.status = status;
        this.error = error;
        this.message = message;
        this.detalles = detalles;
    }
}