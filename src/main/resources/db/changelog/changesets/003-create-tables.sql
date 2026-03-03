CREATE TABLE programming_languages
(
    id   BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);

CREATE UNIQUE INDEX idx_programming_languages_name_unique_ci
    ON programming_languages (lower(name));

CREATE TABLE services
(
    id   BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);

CREATE UNIQUE INDEX idx_services_name_unique_ci
    ON services (lower(name));

CREATE TABLE mentors_programming_languages
(
    mentor_id               BIGINT NOT NULL,
    programming_language_id BIGINT NOT NULL,
    PRIMARY KEY (mentor_id, programming_language_id),
    CONSTRAINT fk_mpl_mentor
        FOREIGN KEY (mentor_id)
            REFERENCES mentors (id)
            ON DELETE CASCADE,
    CONSTRAINT fk_mpl_language
        FOREIGN KEY (programming_language_id)
            REFERENCES programming_languages (id)
            ON DELETE CASCADE
);

CREATE TABLE mentors_services
(
    mentor_id BIGINT NOT NULL,
    service_id BIGINT NOT NULL,
    PRIMARY KEY (mentor_id, service_id),
    CONSTRAINT fk_ms_mentor
        FOREIGN KEY (mentor_id)
            REFERENCES mentors (id)
            ON DELETE CASCADE,
    CONSTRAINT fk_ms_service
        FOREIGN KEY (service_id)
            REFERENCES services (id)
            ON DELETE CASCADE
);

CREATE TABLE mentor_descriptions
(
    id             BIGSERIAL PRIMARY KEY,
    mentor_user_id BIGINT NOT NULL,
    name           VARCHAR(255) NOT NULL,
    cost           VARCHAR(255) NOT NULL,
    description    TEXT,
    CONSTRAINT fk_mentor_descriptions_mentor
        FOREIGN KEY (mentor_user_id)
            REFERENCES mentors (id)
            ON DELETE CASCADE
);