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
            INSERT INTO guaranteed_reviews_prices (MENTOR_ID, PROJECT_TYPE, LANGUAGE, PRICE_USD)
                VALUES ((select id from mentors where mentor_telegram_user_id= :telegramUserId), :projectType, :language, :price)
                ON CONFLICT (mentor_id, project_type, language)
                DO UPDATE SET price_usd = EXCLUDED.price_usd
                RETURNING (xmax = 0) AS inserted;
            """)
    boolean updatePriceForGuaranteedReviews(@Param("price") int price,
                                            @Param("projectType") String projectType,
                                            @Param("telegramUserId") Long telegramUserId,
                                            @Param("language") String language);

    Optional<Mentor> getMentorByMentorTelegramUserId(Long mentorTelegramUserId);

    @Query("""
            SELECT * FROM mentors m
            LEFT JOIN guaranteed_reviews_prices grp ON m.id = grp.mentor_id
            WHERE grp.project_type = :projectType AND grp.language = :language
            """)
    List<Mentor> findAllMentorsByProgrammingLanguageAndProjectType(@Param("language") String language,
                                                                @Param("projectType") String projectType);
}