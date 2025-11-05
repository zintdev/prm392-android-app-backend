package com.example.backend.exception;

import com.example.backend.exception.custom.AddressNotFoundException;
import com.example.backend.exception.custom.AddressAlreadyExistsException;
import com.example.backend.exception.custom.CartNotFoundException;
import com.example.backend.exception.custom.OrderNotFoundException;
import com.example.backend.exception.custom.PaymentNotFoundException;
import com.example.backend.exception.custom.PaymentAlreadyExistsException;
import com.example.backend.exception.custom.UserAlreadyExistsException;
import com.example.backend.exception.custom.UserNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> badRequest(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(body(400, ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> validation(MethodArgumentNotValidException ex) {
        var msg = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField()+": "+e.getDefaultMessage())
                .findFirst().orElse("Validation error");
        return ResponseEntity.unprocessableEntity().body(body(422, msg));
    }
    
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<?> dataIntegrityViolation(DataIntegrityViolationException ex) {
        String message = "Data integrity violation";
        if (ex.getMessage().contains("uq_publishers_publisher_name")) {
            message = "Publisher name already exists";
        }
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body(409, message));
    }
    
    @ExceptionHandler(CartNotFoundException.class)
    public ResponseEntity<?> cartNotFound(CartNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body(404, ex.getMessage()));
    }
    
    @ExceptionHandler(PaymentNotFoundException.class)
    public ResponseEntity<?> paymentNotFound(PaymentNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body(404, ex.getMessage()));
    }
    
    @ExceptionHandler(PaymentAlreadyExistsException.class)
    public ResponseEntity<?> paymentAlreadyExists(PaymentAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body(409, ex.getMessage()));
    }
    
    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<?> orderNotFound(OrderNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body(404, ex.getMessage()));
    }
    
    @ExceptionHandler(AddressNotFoundException.class)
    public ResponseEntity<?> addressNotFound(AddressNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body(404, ex.getMessage()));
    }
    
    @ExceptionHandler(AddressAlreadyExistsException.class)
    public ResponseEntity<?> addressAlreadyExists(AddressAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body(409, ex.getMessage()));
    }
    
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<?> userNotFound(UserNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body(404, ex.getMessage()));
    }
    
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<?> userAlreadyExists(UserAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body(409, ex.getMessage()));
    }
    
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> runtimeException(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(body(500, "An unexpected error occurred: " + ex.getMessage()));
    }

    private Map<String,Object> body(int status, String message){
        return Map.of("timestamp", Instant.now().toString(),
                      "status", status, "message", message);
    }
}
