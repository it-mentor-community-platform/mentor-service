package com.itmentorcommunityplatform.mentorservice.repository;

import com.itmentorcommunityplatform.mentorservice.domain.Mentor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MentorsRepository extends CrudRepository<Mentor, Integer> {

    Mentor findByMentorTelegramUserId(Long telegramUserId);

}
