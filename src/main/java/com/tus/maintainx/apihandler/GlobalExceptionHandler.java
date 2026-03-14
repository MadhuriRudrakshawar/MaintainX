package com.tus.maintainx.apihandler;


import com.tus.maintainx.dto.ErrorResponseDTO;
import com.tus.maintainx.exception.OverlapException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler(OverlapException.class)
    public ResponseEntity<ErrorResponseDTO> conflict(OverlapException ex, HttpServletRequest req) {
        return build(HttpStatus.CONFLICT, "CONFLICT", ex.getMessage(), req.getRequestURI(), null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> validation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(error -> error.getDefaultMessage() == null ? "Validation failed" : error.getDefaultMessage())
                .orElse("Validation failed");
        return build(HttpStatus.BAD_REQUEST, "BAD_REQUEST", message, req.getRequestURI(), null);
    }

    private ResponseEntity<ErrorResponseDTO> build(HttpStatus status, String code, String msg, String path, Map<String, String> details) {
        ErrorResponseDTO dto = ErrorResponseDTO.builder()
                .timestamp(Instant.now())
                .status(status.value())
                .code(code)
                .message(msg)
                .path(path)
                .details(details)
                .build();
        return ResponseEntity.status(status).body(dto);
    }
}
