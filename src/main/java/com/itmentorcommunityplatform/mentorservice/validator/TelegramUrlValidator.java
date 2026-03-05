package com.itmentorcommunityplatform.mentorservice.validator;

import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class TelegramUrlValidator {
    private static final Pattern TELEGRAM_PATTERN = Pattern.compile("^https://t\\.me/[^\\s/]+$");

    public void validate(String value) {
        if (value == null || !TELEGRAM_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("Telegram profile url incorrect");
        }
    }
}
