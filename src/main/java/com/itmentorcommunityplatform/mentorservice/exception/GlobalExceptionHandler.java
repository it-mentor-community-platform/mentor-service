package com.itmentorcommunityplatform.mentorservice.exception;

import com.itmentorcommunityplatform.mentorservice.dto.ApiMessageResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.relational.core.conversion.DbActionExecutionException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiMessageResponse> handleAnyException(Exception e) {

        log.error("Unexpected error", e);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiMessageResponse("Internal server error"));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiMessageResponse> illegalArgumentExceptionHandler(IllegalArgumentException e) {

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ApiMessageResponse(e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiMessageResponse> methodArgumentNotValidExceptionHandler(MethodArgumentNotValidException e) {

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ApiMessageResponse(e.getBindingResult().getFieldError().getDefaultMessage()));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiMessageResponse> handleStatusException(ResponseStatusException e) {

        return ResponseEntity
                .status(e.getStatusCode())
                .body(new ApiMessageResponse(e.getReason()));

    }

    @ExceptionHandler({DbActionExecutionException.class, DataAccessException.class})
    public ResponseEntity<ApiMessageResponse> handleDataIntegrityViolationException(Exception e) {
        Throwable cause = e;
        while (cause.getCause() != null) {
            cause = cause.getCause();
        }

        String rootMessage = cause.getMessage();
        log.info("Caught data integrity exception with root message: {}", rootMessage);

        if (rootMessage != null && rootMessage.contains("idx_mentors_unique")) {
            log.warn("Mentor creation conflict: duplicate telegramUserId or url found. Message: {}", rootMessage);
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(new ApiMessageResponse("Mentor with given telegramUserId already exists"));
        }

        log.error("Unhandled transaction or data access error", e);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiMessageResponse("Internal server error"));
    }
}
