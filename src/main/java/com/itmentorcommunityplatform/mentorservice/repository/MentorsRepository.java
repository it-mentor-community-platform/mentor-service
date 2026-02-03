package com.itmentorcommunityplatform.mentorservice.repository;

import com.itmentorcommunityplatform.mentorservice.domain.Mentor;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MentorsRepository extends CrudRepository<Mentor, Long> {

    @Modifying
    @Query("""
            INSERT INTO mentors (mentor_telegram_user_id, telegram_url, is_active)
                VALUES (:telegramUserId, :telegramUrl, TRUE)
                ON CONFLICT (mentor_telegram_user_id)
                DO UPDATE SET telegram_url = EXCLUDED.telegram_url,
                is_active   = TRUE
            """)
    void upsertMentor(@Param("telegramUserId") Long telegramUserId,
                      @Param("telegramUrl") String telegramUrl);



    @Query("""
            INSERT INTO guaranteed_reviews_prices (MENTOR_ID, PROJECT_TYPE, LANGUAGE, PRICE_USD)
                VALUES ((select id from mentors where mentor_telegram_user_id= :telegramUserId), :projectType, :language, :price)
                ON CONFLICT (mentor_id, project_type, language)
                DO UPDATE SET price_usd = EXCLUDED.price_usd
                RETURNING (xmax = 0) AS inserted
            """)
    boolean updatePriceForGuaranteedReviews(@Param("price") int price,
                                         @Param("projectType") String projectType,
                                         @Param("telegramUserId") Long telegramUserId,
                                         @Param("language") String language);
}