package com.yumeinaruu.nseutask.exception;

import com.yumeinaruu.nseutask.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import java.time.Instant;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class ApiExceptionHandler {

    private static final String FIELD_ERROR_SEPARATOR = "; ";
    private static final String INVALID_VALUE_MESSAGE = "invalid value";

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleApiException(ApiException exception, HttpServletRequest request) {
        ErrorCode errorCode = exception.getErrorCode();
        log.warn("Failed API request: path={}, code={}, type={}, message={}",
                request.getRequestURI(), errorCode.getCode(), exception.getClass().getSimpleName(),
                exception.getMessage());
        return buildErrorResponse(errorCode.getStatus(), errorCode, errorCode.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        String invalidFields = exception.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + ": " + describeFieldError(fieldError))
                .collect(Collectors.joining(FIELD_ERROR_SEPARATOR));
        String message = invalidFields.isBlank() ? ErrorCode.VALIDATION_ERROR.getMessage() : invalidFields;
        log.warn("Failed request validation: path={}, message={}", request.getRequestURI(), message);
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_ERROR, message, request);
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<ErrorResponse> handleOptimisticLockException(
            OptimisticLockingFailureException exception,
            HttpServletRequest request
    ) {
        ErrorCode errorCode = ErrorCode.TASK_CONCURRENT_MODIFICATION;
        log.warn("Failed API request: path={}, code={}, type={}, message={}",
                request.getRequestURI(), errorCode.getCode(), exception.getClass().getSimpleName(),
                exception.getMessage());
        return buildErrorResponse(errorCode.getStatus(), errorCode, errorCode.getMessage(), request);
    }

    @ExceptionHandler({MethodArgumentTypeMismatchException.class, PropertyReferenceException.class})
    public ResponseEntity<ErrorResponse> handleInvalidParameterException(
            Exception exception,
            HttpServletRequest request
    ) {
        log.warn("Failed request parameters: path={}, type={}, message={}",
                request.getRequestURI(), exception.getClass().getSimpleName(), exception.getMessage());
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                ErrorCode.VALIDATION_ERROR,
                ErrorCode.VALIDATION_ERROR.getMessage(),
                request
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedException(Exception exception, HttpServletRequest request) {
        log.error("Failed to handle request: path={}, type={}, message={}",
                request.getRequestURI(), exception.getClass().getSimpleName(), exception.getMessage());
        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorCode.INTERNAL_ERROR,
                ErrorCode.INTERNAL_ERROR.getMessage(),
                request
        );
    }

    // сообщения о ошибке привязки типов содержат имена внутренних классов
    private String describeFieldError(FieldError fieldError) {
        return fieldError.isBindingFailure() ? INVALID_VALUE_MESSAGE : fieldError.getDefaultMessage();
    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(
            HttpStatusCode status,
            ErrorCode errorCode,
            String message,
            HttpServletRequest request
    ) {
        ErrorResponse body = new ErrorResponse(
                Instant.now(),
                status.value(),
                errorCode.getCode(),
                message,
                request.getRequestURI()
        );
        return ResponseEntity.status(status).body(body);
    }

}
