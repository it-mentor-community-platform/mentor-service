package com.itmentorcommunityplatform.mentorservice.consumer;

import com.itmentorcommunityplatform.mentorservice.domain.type.DataSourceType;
import com.itmentorcommunityplatform.mentorservice.dto.MentorDto;
import com.itmentorcommunityplatform.mentorservice.dto.event.MentorNotificationEvent;
import com.itmentorcommunityplatform.mentorservice.dto.event.ProjectCreatedEvent;
import com.itmentorcommunityplatform.mentorservice.producer.MentorNotificationProducer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
class ProjectCreatedConsumerIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private ProjectCreatedConsumer projectCreatedConsumer;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockitoBean
    private MentorNotificationProducer mentorNotificationProducer;

    @AfterEach
    void cleanTestData() {
        jdbcTemplate.execute("""
                TRUNCATE TABLE
                    guaranteed_reviews_prices,
                    mentors
                RESTART IDENTITY CASCADE
                """);
    }

    @Test
    void shouldSendNotificationOnlyToActiveMentorsMatchingProjectLanguageAndType() {
        Long javaSimulationMentorId = insertMentor(1001L, "https://t.me/java_simulation_mentor", true);
        Long pythonSimulationMentorId = insertMentor(1002L, "https://t.me/python_simulation_mentor", true);
        Long javaHangmanMentorId = insertMentor(1003L, "https://t.me/java_hangman_mentor", true);
        Long inactiveJavaSimulationMentorId = insertMentor(1004L, "https://t.me/inactive_java_simulation_mentor", false);

        insertGuaranteedReviewPrice(javaSimulationMentorId, "SIMULATION", "Java", 10);
        insertGuaranteedReviewPrice(pythonSimulationMentorId, "SIMULATION", "Python", 10);
        insertGuaranteedReviewPrice(javaHangmanMentorId, "HANGMAN", "Java", 10);
        insertGuaranteedReviewPrice(inactiveJavaSimulationMentorId, "SIMULATION", "Java", 10);

        ProjectCreatedEvent event = buildProjectCreatedEvent("Java", "SIMULATION");

        projectCreatedConsumer.consumerProjectCreatedEvent(event);

        MentorNotificationEvent notificationEvent = captureNotificationEvent();

        assertThat(notificationEvent.getProject()).isEqualTo(event);
        assertThat(notificationEvent.getMentors())
                .containsExactly(new MentorDto(1001L, "https://t.me/java_simulation_mentor"));
    }

    @Test
    void shouldSendEmptyMentorsListWhenNoMentorsMatchProjectLanguage() {
        Long pythonMentorId = insertMentor(1002L, "https://t.me/python_mentor", true);

        insertGuaranteedReviewPrice(pythonMentorId, "SIMULATION", "Python", 10);

        ProjectCreatedEvent event = buildProjectCreatedEvent("Java", "SIMULATION");

        projectCreatedConsumer.consumerProjectCreatedEvent(event);

        MentorNotificationEvent notificationEvent = captureNotificationEvent();

        assertThat(notificationEvent.getProject()).isEqualTo(event);
        assertThat(notificationEvent.getMentors()).isEmpty();
    }

    @Test
    void shouldSendEmptyMentorsListWhenAllMatchingMentorsAreInactive() {
        Long inactiveMentorId = insertMentor(1001L, "https://t.me/inactive_java_mentor", false);

        insertGuaranteedReviewPrice(inactiveMentorId, "SIMULATION", "Java", 10);

        ProjectCreatedEvent event = buildProjectCreatedEvent("Java", "SIMULATION");

        projectCreatedConsumer.consumerProjectCreatedEvent(event);

        MentorNotificationEvent notificationEvent = captureNotificationEvent();

        assertThat(notificationEvent.getProject()).isEqualTo(event);
        assertThat(notificationEvent.getMentors()).isEmpty();
    }

    @Test
    void shouldSendNotificationToMentorForSpecificProgrammingLanguage() {
        Long rustMentorId = insertMentor(1005L, "https://t.me/rust_mentor", true);

        insertGuaranteedReviewPrice(rustMentorId, "TASK_TRACKER", "Rust", 20);

        ProjectCreatedEvent event = buildProjectCreatedEvent("Rust", "TASK_TRACKER");

        projectCreatedConsumer.consumerProjectCreatedEvent(event);

        MentorNotificationEvent notificationEvent = captureNotificationEvent();

        assertThat(notificationEvent.getProject()).isEqualTo(event);
        assertThat(notificationEvent.getMentors())
                .containsExactly(new MentorDto(1005L, "https://t.me/rust_mentor"));
    }

    private Long insertMentor(Long telegramUserId, String telegramUrl, boolean isActive) {
        return jdbcTemplate.queryForObject(
                """
                INSERT INTO mentors (mentor_telegram_user_id, telegram_url, is_active)
                VALUES (?, ?, ?)
                RETURNING id
                """,
                Long.class,
                telegramUserId,
                telegramUrl,
                isActive
        );
    }

    private void insertGuaranteedReviewPrice(Long mentorId, String projectType, String language, int priceUsd) {
        jdbcTemplate.update(
                """
                INSERT INTO guaranteed_reviews_prices (mentor_id, project_type, language, price_usd)
                VALUES (?, ?, ?, ?)
                """,
                mentorId,
                projectType,
                language,
                priceUsd
        );
    }

    private ProjectCreatedEvent buildProjectCreatedEvent(String programmingLanguage, String roadmapProject) {
        return ProjectCreatedEvent.builder()
                .authorTelegramProfileUrl("https://t.me/student")
                .githubRepositoryUrl("https://github.com/student/project")
                .programmingLanguage(programmingLanguage)
                .roadmapProject(roadmapProject)
                .addedTimestamp(1777060225L)
                .projectSourceType(DataSourceType.FRONTEND)
                .build();
    }

    private MentorNotificationEvent captureNotificationEvent() {
        ArgumentCaptor<MentorNotificationEvent> captor =
                ArgumentCaptor.forClass(MentorNotificationEvent.class);

        verify(mentorNotificationProducer).notificateMentors(captor.capture());

        return captor.getValue();
    }
}