package com.pack.exceptions;

import com.pack.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler(
            InvalidOtpException.class)
    public ResponseEntity<ApiResponse>
    handleInvalidOtp(
            InvalidOtpException ex) {

        return ResponseEntity.badRequest()
                .body(ApiResponse.error(400, ex.getMessage()));
    }

    @ExceptionHandler(
            IllegalArgumentException.class)
    public ResponseEntity<ApiResponse>
    handleValidation(
            IllegalArgumentException ex) {

        return ResponseEntity.badRequest()
                .body(ApiResponse.error(400,
                        ex.getMessage()));
    }

    @ExceptionHandler(ResourceNotFoundException .class)
    public ResponseEntity<ApiResponse>
    handleValidation(
    ResourceNotFoundException ex)
    {
        return ResponseEntity.badRequest().body(ApiResponse.error(400, ex.getMessage()));
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiResponse>
            handleValidation(
                    ConflictException ex){
        return ResponseEntity.badRequest().body(ApiResponse.error(400,ex.getMessage()));
    }
}