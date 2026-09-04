package com.itmentorcommunityplatform.mentorservice.exception;

public class MissingMentorRoleException extends RuntimeException {

    public MissingMentorRoleException() {
        super("Current user is not a mentor");
    }
}