package com.itmentorcommunityplatform.mentorservice.exception;

public class MentorNotFoundException extends RuntimeException {
    public MentorNotFoundException() {
        super("Mentor not found");
    }
}
