package com.itmentorcommunityplatform.mentorservice.exception;

public class GuaranteedReviewPriceAlreadyExistsException extends RuntimeException {
    public GuaranteedReviewPriceAlreadyExistsException() {
        super("Guaranteed review price already exists");
    }
}
