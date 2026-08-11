package com.itmentorcommunityplatform.mentorservice.exception;

public class ProfileNotFoundException extends RuntimeException {
    public ProfileNotFoundException() {
        super("Profile with given Telegram URL not found");
    }
}
