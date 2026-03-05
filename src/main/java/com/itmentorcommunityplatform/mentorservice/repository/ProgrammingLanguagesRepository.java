package com.itmentorcommunityplatform.mentorservice.repository;

import com.itmentorcommunityplatform.mentorservice.domain.ProgrammingLanguage;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;

public interface ProgrammingLanguagesRepository extends CrudRepository<ProgrammingLanguage, Integer> {

    @Query("""
            INSERT INTO programming_languages (name)
            VALUES (:name)
            ON CONFLICT (lower(name))
            DO UPDATE 
            SET name = EXCLUDED.name
            RETURNING id;
            """)
    Long upsertProgrammingLanguage(String name);

    @Modifying
    @Query("""
            DELETE FROM mentors_programming_languages
            WHERE mentor_id = :mentorId
            """)
    void deleteMentorsProgrammingLanguages(Long mentorId);

    @Modifying
    @Query("""
            INSERT INTO mentors_programming_languages (mentor_id, programming_language_id)
            VALUES (:mentorId, :programmingLanguageId)
            """)
    void insertMentorsProgrammingLanguage(Long mentorId, Long programmingLanguageId);
}
