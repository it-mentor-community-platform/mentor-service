package com.itmentorcommunityplatform.mentorservice.repository;

import com.itmentorcommunityplatform.mentorservice.domain.ProgrammingLanguage;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface ProgrammingLanguagesRepository extends CrudRepository<ProgrammingLanguage, Integer> {

    @Query("""
            SELECT id 
            FROM programming_languages 
            WHERE lower(name) = lower(:name)
            """)
    Optional<Long> findIdByName(String name);
}
