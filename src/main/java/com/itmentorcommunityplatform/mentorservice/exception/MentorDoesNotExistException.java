package com.itmentorcommunityplatform.mentorservice.exception;

public class MentorDoesNotExistException extends RuntimeException {
    public MentorDoesNotExistException(String message) {
        super(message);
    }
}
