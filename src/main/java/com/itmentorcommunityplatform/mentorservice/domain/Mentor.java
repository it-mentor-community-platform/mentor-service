package com.itmentorcommunityplatform.mentorservice.domain;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;

import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "mentors")
public class Mentor {

    @Id
    private Long id;

    @Column("mentor_telegram_user_id")
    private Long mentorTelegramUserId;

    @Column("telegram_url")
    private String telegramUrl;

    @Column("is_active")
    private boolean isActive;

    @MappedCollection(idColumn = "mentor_id")
    private Set<GuaranteedReviewsPrices> prices = new HashSet<>();

    @MappedCollection(idColumn = "mentor_user_id")
    private MentorDescription mentorDescription;

    @MappedCollection(idColumn = "mentor_id", keyColumn = "programming_language_id")
    private Set<MentorProgrammingLanguage> programmingLanguages = new HashSet<>();

    @MappedCollection(idColumn = "mentor_id", keyColumn = "service_id")
    private Set<MentorService> services = new HashSet<>();
}
