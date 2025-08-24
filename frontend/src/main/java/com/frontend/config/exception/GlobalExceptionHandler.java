package com.frontend.config.exception;

import com.frontend.config.message.ApiResponse;
import com.frontend.utils.ResponseUtils;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    // 處理資源不存在
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleResourceNotFound(ResourceNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ResponseUtils.error(404, ex.getMessage(), null));
    }

    // 處理自訂的 AccessDeniedException
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Object>> handleCustomAccessDenied(AccessDeniedException ex) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ResponseUtils.error(403, ex.getMessage(), null));
    }

    // 處理 Bean Validation 錯誤（@Valid / @Validated）
    @ExceptionHandler({ MethodArgumentNotValidException.class, ConstraintViolationException.class })
    public ResponseEntity<ApiResponse<Object>> handleValidationException(Exception ex) {
        String message = ex instanceof MethodArgumentNotValidException ?
                ((MethodArgumentNotValidException) ex).getBindingResult().getAllErrors().get(0).getDefaultMessage() :
                ex.getMessage();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ResponseUtils.error(400, message, null));
    }

    // 處理所有未捕獲的 RuntimeException
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Object>> handleRuntimeException(RuntimeException ex) {
        ex.printStackTrace(); // 可以換成日誌系統
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseUtils.error(500, ex.getMessage(), null));
    }

    // 處理所有其他 Exception
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleException(Exception ex) {
        ex.printStackTrace();
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseUtils.error(500, "系統發生錯誤", null));
    }
}
