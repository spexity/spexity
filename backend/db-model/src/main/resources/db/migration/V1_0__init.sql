CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE contributor
(
    id            UUID      DEFAULT uuid_generate_v4() PRIMARY KEY,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    alias         TEXT NOT NULL,
    discriminator TEXT NOT NULL,
    handle        TEXT GENERATED ALWAYS AS (alias || '#' || discriminator) STORED UNIQUE
);

CREATE TABLE community
(
    id         UUID      DEFAULT uuid_generate_v4() PRIMARY KEY,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    name       TEXT NOT NULL UNIQUE,
    slug       TEXT NOT NULL UNIQUE
);

CREATE TABLE post
(
    id             UUID      DEFAULT uuid_generate_v4() PRIMARY KEY,
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    subject        TEXT NOT NULL,
    body           TEXT NOT NULL,
    community_id   UUID NOT NULL REFERENCES community (id),
    contributor_id UUID NOT NULL REFERENCES contributor (id)
);
