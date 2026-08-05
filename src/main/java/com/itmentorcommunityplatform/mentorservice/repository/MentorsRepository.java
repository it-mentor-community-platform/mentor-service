package com.itmentorcommunityplatform.mentorservice.repository;

import com.itmentorcommunityplatform.mentorservice.domain.Mentor;
import com.itmentorcommunityplatform.mentorservice.dto.MentorUpsertResult;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MentorsRepository extends CrudRepository<Mentor, Long> {

    @Query("""
            INSERT INTO mentors (mentor_telegram_user_id, telegram_url, is_active)
                VALUES (:telegramUserId, :telegramUrl, TRUE)
                ON CONFLICT (mentor_telegram_user_id)
                DO UPDATE SET telegram_url = EXCLUDED.telegram_url,
                is_active   = TRUE
                RETURNING id, mentor_telegram_user_id, telegram_url, is_active, (xmax = 0) AS inserted;
            """)
    MentorUpsertResult upsertMentor(@Param("telegramUserId") Long telegramUserId,
                                    @Param("telegramUrl") String telegramUrl);

    @Query("""
            UPDATE mentors
            SET telegram_url = :telegramUrl,
                is_active = :isActive
            WHERE mentor_telegram_user_id = :telegramUserId
            RETURNING id, mentor_telegram_user_id, telegram_url, is_active;
            """)
    Mentor updateMentor(Long telegramUserId, String telegramUrl, boolean isActive);

    @Query("""
            INSERT INTO guaranteed_reviews_prices(mentor_id,project_type,language,price_usd)
            VALUES (:mentorId, :projectType, :language, :price)
            ON CONFLICT (mentor_id, project_type, language)
            DO NOTHING
            RETURNING id;
            """)
    Optional<Long> insertPriceForGuaranteedReviews(@Param("price") int price,
                                                   @Param("projectType") String projectType,
                                                   @Param("mentorId") Long mentorId,
                                                   @Param("language") String language);

    Optional<Mentor> getMentorByMentorTelegramUserId(Long mentorTelegramUserId);

    @Query("""
            SELECT * FROM mentors m
            LEFT JOIN guaranteed_reviews_prices grp ON m.id = grp.mentor_id
            WHERE grp.project_type = :projectType 
              AND grp.language = :language 
              AND m.is_active = TRUE
            """)
    List<Mentor> findActiveMentorsByProgrammingLanguageAndProjectType(@Param("language") String language,
                                                                      @Param("projectType") String projectType);
}