package com.itmentorcommunityplatform.mentorservice.exception;

public class UserIsNotMentorException extends RuntimeException {

    public UserIsNotMentorException() {
        super("Current user is not a mentor");
    }
}