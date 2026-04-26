package com.veterinaria.excepciones;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import com.veterinaria.dtos.ErrorResponseDTO;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. Atrapa los errores de validación de los DTOs (@Valid, @NotBlank, @NotNull)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> manejarValidaciones(MethodArgumentNotValidException ex) {
        List<String> errores = new ArrayList<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errores.add(error.getField() + ": " + error.getDefaultMessage());
        }

        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                HttpStatus.BAD_REQUEST.value(),
                "Error de Validación",
                "Uno o más campos son incorrectos",
                errores);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    // 2. Atrapa los errores de Concurrencia (Dos personas tocando el mismo stock)
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ErrorResponseDTO> manejarConcurrencia(ObjectOptimisticLockingFailureException ex) {
        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                HttpStatus.CONFLICT.value(),
                "Conflicto de Datos",
                "El registro que intentas modificar acaba de ser actualizado por otro usuario. Por favor, recarga la página.",
                null);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    // 3. Atrapa los ResponseStatusException que ya programaste en tus servicios
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponseDTO> manejarResponseStatusException(ResponseStatusException ex) {
        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                ex.getStatusCode().value(),
                "Error en la petición",
                ex.getReason(),
                null);
        return ResponseEntity.status(ex.getStatusCode()).body(errorResponse);
    }

    // 4. Atrapa errores de autorización (el usuario no tiene permiso) → 403 FORBIDDEN
    // IMPORTANTE: debe ir ANTES del handler genérico de Exception para que no lo tape
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponseDTO> manejarAccesoDenegado(AccessDeniedException ex) {
        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                HttpStatus.FORBIDDEN.value(),
                "Acceso Denegado",
                "No tienes permisos para realizar esta acción.",
                null);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

    // 5. Tipos de parámetro inválidos (query/path params) -> 400
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponseDTO> manejarTypeMismatch(MethodArgumentTypeMismatchException ex) {
        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                HttpStatus.BAD_REQUEST.value(),
                "Error de Validación",
                "Parámetro inválido",
                List.of(ex.getName() + ": valor inválido"));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    // 6. Autenticación fallida -> 401
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponseDTO> manejarAuthenticationException(AuthenticationException ex) {
        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                HttpStatus.UNAUTHORIZED.value(),
                "No Autenticado",
                "Debes iniciar sesión para acceder a este recurso.",
                null);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    // 7. Recurso inexistente (JPA) -> 404
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> manejarEntityNotFound(EntityNotFoundException ex) {
        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                HttpStatus.NOT_FOUND.value(),
                "No Encontrado",
                "Recurso no encontrado.",
                null);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    // 8. Estado inválido de negocio -> 409
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponseDTO> manejarIllegalState(IllegalStateException ex) {
        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                HttpStatus.CONFLICT.value(),
                "Conflicto de Datos",
                ex.getMessage() != null ? ex.getMessage() : "Operación inválida.",
                null);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> manejarErroresInesperados(Exception ex) {
        ex.printStackTrace(); // Log the actual error to console
        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Error Interno del Servidor",
                ex.toString() + " : " + ex.getMessage(),
                null
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}