package com.itmentorcommunityplatform.mentorservice.exception;

public class AbsentTelegramIdException extends RuntimeException {
    public AbsentTelegramIdException(String message) {
        super(message);
    }
}
