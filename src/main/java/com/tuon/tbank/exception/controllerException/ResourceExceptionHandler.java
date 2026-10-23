package com.tuon.tbank.exception.controllerException;

import com.tuon.tbank.exception.serviceException.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

/**
 * Maps supported application and request-validation exceptions to API error responses.
 */
@RestControllerAdvice
public class ResourceExceptionHandler {

    /**
     * Handles request-validation failures with a {@code 400 Bad Request} response.
     *
     * @param e the validation exception
     * @param request the request that failed validation
     * @return the validation error response
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<StandardError> validation(MethodArgumentNotValidException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        StandardError err = new StandardError(Instant.now(), status.value(), "Invalid request",
                "Request validation failed.", request.getRequestURI());
        return ResponseEntity.status(status).body(err);
    }

    /**
     * Handles missing-resource failures with a {@code 404 Not Found} response.
     *
     * @param e the exception describing the missing resource
     * @param request the request that could not be fulfilled
     * @return the resource-not-found error response
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<StandardError> resourceNotFoundException(ResourceNotFoundException e, HttpServletRequest request) {
        String error = "Resource not found";
        HttpStatus status = HttpStatus.NOT_FOUND;
        StandardError err = new StandardError(Instant.now(), status.value(), error, e.getMessage(), request.getRequestURI());
        return ResponseEntity.status(status).body(err);
    }

    /**
     * Handles database-related failures with a {@code 400 Bad Request} response.
     *
     * @param e the database exception
     * @param request the request during which the failure occurred
     * @return the database error response
     */
    @ExceptionHandler(DatabaseException.class)
    public ResponseEntity<StandardError> database(DatabaseException e, HttpServletRequest request) {
        String error = "Database error";
        HttpStatus status = HttpStatus.BAD_REQUEST;
        StandardError err = new StandardError(Instant.now(), status.value(), error, e.getMessage(), request.getRequestURI());
        return ResponseEntity.status(status).body(err);
    }

    /**
     * Handles business-rule violations with a {@code 422 Unprocessable Content} response.
     *
     * @param e the business exception
     * @param request the request that violated a business rule
     * @return the business error response
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<StandardError> business(BusinessException e, HttpServletRequest request) {
        String error = "Business rule violation";
        HttpStatus status = HttpStatus.UNPROCESSABLE_CONTENT;
        StandardError err = new StandardError(Instant.now(), status.value(), error, e.getMessage(), request.getRequestURI());
        return ResponseEntity.status(status).body(err);
    }

    /**
     * Handles insufficient-balance failures with a {@code 422 Unprocessable Content} response.
     *
     * @param e the insufficient-balance exception
     * @param request the request that could not be completed
     * @return the insufficient-balance error response
     */
    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<StandardError> insufficientBalance(InsufficientBalanceException e, HttpServletRequest request){
        String error = "Insufficient balance error";
        HttpStatus status = HttpStatus.UNPROCESSABLE_CONTENT;
        StandardError err = new StandardError(Instant.now(), status.value(), error, e.getMessage(), request.getRequestURI());
        return ResponseEntity.status(status).body(err);
    }

    /**
     * Handles invalid investment operations with a {@code 422 Unprocessable Content} response.
     *
     * @param e the invalid-investment exception
     * @param request the request that could not be completed
     * @return the invalid-investment error response
     */
    @ExceptionHandler(InvalidInvestmentException.class)
    public ResponseEntity<StandardError> invalidInvestment(InvalidInvestmentException e, HttpServletRequest request){
        String error = "Invalid investment error";
        HttpStatus status = HttpStatus.UNPROCESSABLE_CONTENT;
        StandardError err = new StandardError(Instant.now(), status.value(), error, e.getMessage(), request.getRequestURI());
        return ResponseEntity.status(status).body(err);
    }

    /**
     * Handles invalid transfer operations with a {@code 422 Unprocessable Content} response.
     *
     * @param e the invalid-transfer exception
     * @param request the request that could not be completed
     * @return the invalid-transfer error response
     */
    @ExceptionHandler(InvalidTransferException.class)
    public ResponseEntity<StandardError> invalidTransfer(InvalidTransferException e, HttpServletRequest request){
        String error = "Invalid transfer error";
        HttpStatus status = HttpStatus.UNPROCESSABLE_CONTENT;
        StandardError err = new StandardError(Instant.now(), status.value(), error, e.getMessage(), request.getRequestURI());
        return ResponseEntity.status(status).body(err);
    }

    /**
     * Handles disallowed transaction reversals with a {@code 422 Unprocessable Content} response.
     *
     * @param e the reversal exception
     * @param request the request that could not be completed
     * @return the reversal error response
     */
    @ExceptionHandler(ReversalNotAllowedException.class)
    public ResponseEntity<StandardError> reversalNotAllowed(ReversalNotAllowedException e, HttpServletRequest request){
        String error = "Reversal transaction not allowed";
        HttpStatus status = HttpStatus.UNPROCESSABLE_CONTENT;
        StandardError err = new StandardError(Instant.now(), status.value(), error, e.getMessage(), request.getRequestURI());
        return ResponseEntity.status(status).body(err);
    }
}
