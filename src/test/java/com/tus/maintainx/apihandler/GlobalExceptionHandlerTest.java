package com.tus.maintainx.apihandler;

import com.tus.maintainx.dto.ErrorResponseDTO;
import com.tus.maintainx.exception.BadRequestException;
import com.tus.maintainx.exception.NotFoundException;
import com.tus.maintainx.exception.OverlapException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void buildsConflictResponse() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/v1/windows");

        ResponseEntity<ErrorResponseDTO> response =
                handler.conflict(new OverlapException("Overlap detected"), request);
        ErrorResponseDTO body = response.getBody();

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(body);
        assertEquals(409, body.getStatus());
        assertEquals("CONFLICT", body.getCode());
        assertEquals("Overlap detected", body.getMessage());
        assertEquals("/api/v1/windows", body.getPath());
        assertNotNull(body.getTimestamp());
    }

    @Test
    void buildsNotFoundResponse() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/v1/network-elements/99");

        ResponseEntity<ErrorResponseDTO> response =
                handler.notFound(new NotFoundException("Element not found"), request);
        ErrorResponseDTO body = response.getBody();

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(body);
        assertEquals("NOT_FOUND", body.getCode());
        assertEquals("Element not found", body.getMessage());
        assertEquals("/api/v1/network-elements/99", body.getPath());
    }

    @Test
    void buildsBadRequestResponse() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/v1/auth/login");

        ResponseEntity<ErrorResponseDTO> response =
                handler.badRequest(new BadRequestException("Invalid payload"), request);
        ErrorResponseDTO body = response.getBody();

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(body);
        assertEquals("BAD_REQUEST", body.getCode());
        assertEquals("Invalid payload", body.getMessage());
        assertEquals("/api/v1/auth/login", body.getPath());
    }

    @Test
    void validation_usesFirstFieldErrorMessage() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/v1/auth/login");

        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(
                new FieldError("loginRequest", "username", "Username must be a valid email address")
        ));

        ResponseEntity<ErrorResponseDTO> response = handler.validation(exception, request);
        ErrorResponseDTO body = response.getBody();

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(body);
        assertEquals("BAD_REQUEST", body.getCode());
        assertEquals("Username must be a valid email address", body.getMessage());
        assertEquals("/api/v1/auth/login", body.getPath());
    }

    @Test
    void validation_WhenNoFieldErrorsExist() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/v1/auth/login");

        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of());

        ResponseEntity<ErrorResponseDTO> response = handler.validation(exception, request);
        ErrorResponseDTO body = response.getBody();

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(body);
        assertEquals("Validation failed", body.getMessage());
    }
}
