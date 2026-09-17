package com.itmentorcommunityplatform.mentorservice.exception;

public class GuaranteedReviewPriceNotFoundException extends RuntimeException {
    public GuaranteedReviewPriceNotFoundException() {
        super("Guaranteed review price not found");
    }
}
