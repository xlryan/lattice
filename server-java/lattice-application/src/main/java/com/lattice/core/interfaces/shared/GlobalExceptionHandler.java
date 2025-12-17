package com.lattice.core.interfaces.shared;

import com.lattice.core.infrastructure.exception.BusinessException;
import com.lattice.core.infrastructure.exception.ErrorCode;
import com.lattice.core.infrastructure.exception.LatticeException;
import com.lattice.core.infrastructure.logging.TraceContextHolder;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * Maps exceptions to {@link ApiResponse} payloads.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusiness(BusinessException ex, HttpServletRequest request) {
        return build(ex.getErrorCode(), ex.getMessage(), HttpStatus.BAD_REQUEST, request, ex);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        return build(ErrorCode.VALIDATION_FAILURE, message, HttpStatus.BAD_REQUEST, request, ex);
    }

    @ExceptionHandler(LatticeException.class)
    public ResponseEntity<ApiResponse<Void>> handleLattice(LatticeException ex, HttpServletRequest request) {
        HttpStatus status = switch (ex.getErrorCode()) {
            case RESOURCE_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case UNAUTHORIZED -> HttpStatus.UNAUTHORIZED;
            case FORBIDDEN -> HttpStatus.FORBIDDEN;
            case EXTERNAL_SERVICE_ERROR -> HttpStatus.SERVICE_UNAVAILABLE;
            default -> HttpStatus.BAD_REQUEST;
        };
        return build(ex.getErrorCode(), ex.getMessage(), status, request, ex);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleFallback(Exception ex, HttpServletRequest request) {
        log.error("[traceId={}] Unhandled exception", TraceContextHolder.currentTraceId(), ex);
        return build(ErrorCode.INTERNAL_ERROR, "Internal server error", HttpStatus.INTERNAL_SERVER_ERROR, request, ex);
    }

    private ResponseEntity<ApiResponse<Void>> build(ErrorCode code,
                                                    String message,
                                                    HttpStatus status,
                                                    HttpServletRequest request,
                                                    Exception ex) {
        String traceId = (String) request.getAttribute(TraceContextHolder.TRACE_ID_KEY);
        log.warn("[traceId={}] {}", traceId, message, ex);
        return ResponseEntity.status(status).body(ApiResponse.failure(code.name(), message, traceId));
    }
}
