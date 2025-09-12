CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE user_account
(
    id                  UUID               DEFAULT uuid_generate_v4() PRIMARY KEY,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    auth_correlation_id TEXT      NOT NULL UNIQUE,
    email_address       TEXT      NOT NULL
);

CREATE TABLE contributor
(
    id              UUID               DEFAULT uuid_generate_v4() PRIMARY KEY,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    user_account_id UUID      NOT NULL REFERENCES user_account (id),
    alias           TEXT      NOT NULL,
    discriminator   INT       NOT NULL CHECK ( discriminator > 0 ),
    handle          TEXT GENERATED ALWAYS AS (alias || '#' || discriminator) STORED UNIQUE
);

CREATE TABLE contributor_alias_meta
(
    alias      TEXT PRIMARY KEY,
    digits     INT       NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

create or replace function pick_discriminator(
    p_alias text,
    p_digits int
) returns int
    language plpgsql
    volatile
    parallel unsafe
as
$$
declare
    minv_big    bigint;
    maxv_big    bigint;
    range_sz    bigint;
    ring_offset bigint;
    i           bigint;
    idx         bigint;
    v           bigint;
    disc        int;
begin
    -- per-alias transaction lock
    perform pg_advisory_xact_lock(hashtextextended(p_alias, 0));

    -- bounds
    minv_big := power(10, p_digits - 1)::bigint;
    maxv_big := power(10, p_digits)::bigint - 1;
    range_sz := maxv_big - minv_big + 1;

    -- ring offset (per-call variability)
    ring_offset := hashtextextended(p_alias, extract(epoch from clock_timestamp())::bigint);

    -- walk the ring once, exit on first free
    i := 0;
    while i < range_sz
        loop
            idx := ((i + ring_offset) % range_sz + range_sz) % range_sz;
            v := minv_big + idx;
            disc := v::int;

            -- free?
            if not exists (select 1
                           from contributor uh
                           where uh.alias = p_alias
                             and uh.discriminator = disc) then
                return disc;
            end if;

            i := i + 1;
        end loop;

    -- saturated
    return null;
end;
$$;

CREATE TABLE community
(
    id         UUID               DEFAULT uuid_generate_v4() PRIMARY KEY,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    name       TEXT      NOT NULL UNIQUE,
    slug       TEXT      NOT NULL UNIQUE
);

CREATE TABLE post
(
    id             UUID               DEFAULT uuid_generate_v4() PRIMARY KEY,
    created_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    subject        TEXT      NOT NULL,
    body           TEXT      NOT NULL,
    community_id   UUID      NOT NULL REFERENCES community (id),
    contributor_id UUID      NOT NULL REFERENCES contributor (id)
);
