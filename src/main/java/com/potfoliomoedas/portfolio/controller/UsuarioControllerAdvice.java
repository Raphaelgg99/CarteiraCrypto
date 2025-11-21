package com.potfoliomoedas.portfolio.controller;

import com.potfoliomoedas.portfolio.dto.ApiErrorResponse;
import com.potfoliomoedas.portfolio.exception.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class UsuarioControllerAdvice {

    @ExceptionHandler({EmailNullException.class, NomeNullException.class, SenhaNullException.class})
    public ResponseEntity<ApiErrorResponse> handleValidacaoManual(
            RuntimeException ex, HttpServletRequest request) { // Pega qualquer uma das 3

        ApiErrorResponse response = new ApiErrorResponse(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(), // 400
                HttpStatus.BAD_REQUEST.getReasonPhrase(), // "Bad Request"
                ex.getMessage(), // "Email não pode ser vazio", "Nome não pode ser vazio", etc.
                request.getRequestURI()
        );
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(EmailExistenteException.class)
    public ResponseEntity<ApiErrorResponse> handleEmailExistente(
            EmailExistenteException ex, HttpServletRequest request) {

        ApiErrorResponse response = new ApiErrorResponse(
                Instant.now(),
                HttpStatus.CONFLICT.value(), // 409
                HttpStatus.CONFLICT.getReasonPhrase(), // "Conflict"
                ex.getMessage(), // "Email já existente"
                request.getRequestURI()
        );
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(UsuarioNaoEncontradoException.class)
    public ResponseEntity<ApiErrorResponse> handleUsuarioNaoEncontrado(
            UsuarioNaoEncontradoException ex, HttpServletRequest request) {

        ApiErrorResponse response = new ApiErrorResponse(
                Instant.now(),
                HttpStatus.NOT_FOUND.value(), // 404
                HttpStatus.NOT_FOUND.getReasonPhrase(), // "Not Found"
                ex.getMessage(), // A mensagem que você definiu ("Usuario não encontrado")
                request.getRequestURI() // O path (ex: "/usuario/99")
        );
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MoedaNaoEncontradaException.class)
    public ResponseEntity<ApiErrorResponse> handleMoedaNaoEncontrada(
            MoedaNaoEncontradaException ex, HttpServletRequest request) {

        ApiErrorResponse response = new ApiErrorResponse(
                Instant.now(),
                HttpStatus.NOT_FOUND.value(), // 401
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidCredentials(
            InvalidCredentialsException ex, HttpServletRequest request) {

        ApiErrorResponse response = new ApiErrorResponse(
                Instant.now(),
                HttpStatus.UNAUTHORIZED.value(), // 401
                HttpStatus.UNAUTHORIZED.getReasonPhrase(), // "Unauthorized"
                ex.getMessage(), // "Credenciais inválidas"
                request.getRequestURI()
        );
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGenericException(
            Exception ex, HttpServletRequest request) {

        // IMPORTANTE: Logue o erro real no console para você (desenvolvedor)
        System.err.println("Erro inesperado: " + ex.getMessage());
        ex.printStackTrace(); // Para debugging

        ApiErrorResponse response = new ApiErrorResponse(
                Instant.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(), // 500
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), // "Internal Server Error"
                "Ocorreu um erro inesperado no servidor. Contate o administrador.",
                request.getRequestURI()
        );
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
