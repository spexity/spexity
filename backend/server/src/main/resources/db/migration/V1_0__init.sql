CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE TABLE space
(
    id uuid DEFAULT uuid_generate_v4() PRIMARY KEY,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    name TEXT NOT NULL
);

CREATE TABLE post
(
    id uuid DEFAULT uuid_generate_v4() PRIMARY KEY,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    subject TEXT NOT NULL,
    body TEXT NOT NULL
);
