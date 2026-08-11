package com.itmentorcommunityplatform.mentorservice.exception;

import com.itmentorcommunityplatform.mentorservice.dto.ApiMessageResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

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

    @ExceptionHandler({
            MentorNotFoundException.class,
            ProfileNotFoundException.class
    })
    public ResponseEntity<ApiMessageResponse> handleNotFoundException(RuntimeException e) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ApiMessageResponse(e.getMessage()));
    }

    @ExceptionHandler(GuaranteedReviewPriceAlreadyExistsException.class)
    public ResponseEntity<ApiMessageResponse> handleAlreadyExistsException(
            GuaranteedReviewPriceAlreadyExistsException e) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ApiMessageResponse(e.getMessage()));
    }

    @ExceptionHandler(MentorDuplicateException.class)
    public ResponseEntity<ApiMessageResponse> handleMentorDuplicateException(MentorDuplicateException e) {
        log.warn("Mentor creation conflict: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ApiMessageResponse(e.getMessage()));
    }
}
