package com.tus.maintainx.apihandler;


import com.tus.maintainx.dto.ErrorResponseDTO;
import com.tus.maintainx.exception.BadRequestException;
import com.tus.maintainx.exception.NotFoundException;
import com.tus.maintainx.exception.OverlapException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {


    @ExceptionHandler(OverlapException.class)
    public ResponseEntity<ErrorResponseDTO> conflict(OverlapException ex, HttpServletRequest req) {
        log.warn("Conflict on {}: {}", req.getRequestURI(), ex.getMessage());
        return build(HttpStatus.CONFLICT, "CONFLICT", ex.getMessage(), req.getRequestURI());
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> notFound(NotFoundException ex, HttpServletRequest req) {
        log.warn("Resource not found on {}: {}", req.getRequestURI(), ex.getMessage());
        return build(HttpStatus.NOT_FOUND, "NOT_FOUND", ex.getMessage(), req.getRequestURI());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> validation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(org.springframework.validation.FieldError::getDefaultMessage)
                .orElse("Validation failed");
        log.warn("Validation failed on {}: {}", req.getRequestURI(), message);
        return build(HttpStatus.BAD_REQUEST, "BAD_REQUEST", message, req.getRequestURI());
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponseDTO> badRequest(BadRequestException ex, HttpServletRequest req) {
        log.warn("Bad request on {}: {}", req.getRequestURI(), ex.getMessage());
        return build(HttpStatus.BAD_REQUEST, "BAD_REQUEST", ex.getMessage(), req.getRequestURI());
    }

    private ResponseEntity<ErrorResponseDTO> build(HttpStatus status, String code, String msg, String path) {
        ErrorResponseDTO dto = ErrorResponseDTO.builder()
                .timestamp(Instant.now())
                .status(status.value())
                .code(code)
                .message(msg)
                .path(path)
                .build();
        return ResponseEntity.status(status).body(dto);
    }
}
