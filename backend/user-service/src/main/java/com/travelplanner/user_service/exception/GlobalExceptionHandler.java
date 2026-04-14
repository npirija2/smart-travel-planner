package com.travelplanner.user_service.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Model za uniforman odgovor o grešci
    @Data
    @AllArgsConstructor
    public static class ErrorDetails {
        private String error;
        private String message;
    }

    // Hvatanje grešaka validacije (@Valid u kontroleru)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDetails> handleValidationErrors(MethodArgumentNotValidException ex) {
        // Uzimamo prvu poruku o grešci koju smo definisali u DTO-u
        String errorMessage = ex.getBindingResult().getFieldErrors().get(0).getDefaultMessage();
        return new ResponseEntity<>(new ErrorDetails("validation", errorMessage), HttpStatus.BAD_REQUEST);
    }

    // Hvatanje ResourceNotFoundException (kada korisnik ne postoji)
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorDetails> handleNotFound(ResourceNotFoundException ex) {
        return new ResponseEntity<>(new ErrorDetails("not_found", ex.getMessage()), HttpStatus.NOT_FOUND);
    }
} 