CREATE TABLE authentication_data (
    id         SERIAL PRIMARY KEY,
    user_id   INT NOT NULL REFERENCES users(ID),
    auth_entity   VARCHAR(500) NOT NULL,
    ip   VARCHAR(500) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    expires_at TIMESTAMP NOT NULL
);