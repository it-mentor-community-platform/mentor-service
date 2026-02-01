CREATE TABLE mentors
(
    id                      BIGSERIAL PRIMARY KEY,
    mentor_telegram_user_id BIGINT       NOT NULL,
    telegram_url            VARCHAR(255) NOT NULL,
    is_active               BOOLEAN      NOT NULL
);

CREATE TABLE guaranteed_reviews_prices
(
    id           BIGSERIAL PRIMARY KEY,
    mentor_id    BIGINT       NOT NULL,
    project_type VARCHAR(255) NOT NULL,
    language     VARCHAR(255) NOT NULL,
    price_usd    INTEGER      NOT NULL,

    CONSTRAINT fk_mentors_mentor
        FOREIGN KEY (mentor_id)
            REFERENCES mentors (id)
            ON DELETE CASCADE
);

CREATE UNIQUE INDEX idx_guaranteed_reviews_prices_unique
    ON guaranteed_reviews_prices (mentor_id, project_type, language);
