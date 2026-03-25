ALTER TABLE mentor_descriptions
    ADD CONSTRAINT mentor_unique_id UNIQUE (mentor_user_id);