CREATE TABLE user_account
(
    id                  UUID                 DEFAULT uuidv7() PRIMARY KEY,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_verified_human   BOOLEAN     NOT NULL,
    auth_correlation_id TEXT        NOT NULL UNIQUE,
    email_address       TEXT        NOT NULL
);

CREATE TABLE contributor
(
    id              UUID                 DEFAULT uuidv7() PRIMARY KEY,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    user_account_id UUID        NOT NULL REFERENCES user_account (id),
    alias           TEXT        NOT NULL,
    discriminator   INT         NOT NULL CHECK ( discriminator > 0 ),
    handle          TEXT GENERATED ALWAYS AS (alias || '#' || discriminator) STORED UNIQUE
);

CREATE TABLE contributor_alias_meta
(
    alias      TEXT PRIMARY KEY,
    digits     INT         NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
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
    id                        UUID                 DEFAULT uuidv7() PRIMARY KEY,
    created_at                TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    name                      TEXT        NOT NULL UNIQUE,
    created_by_contributor_id UUID        NOT NULL REFERENCES contributor (id)
);
CREATE INDEX idx_community_created_at ON community (created_at);

CREATE TABLE post
(
    id             UUID                 DEFAULT uuidv7() PRIMARY KEY,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    subject        TEXT        NOT NULL,
    body_json      JSONB       NOT NULL,
    body_text      TEXT        NOT NULL,
    search_tsv     TSVECTOR
        GENERATED ALWAYS AS (
            setweight(to_tsvector('english', coalesce(subject, '')), 'A') ||
            setweight(to_tsvector('english', coalesce(body_text, '')), 'B')
            ) STORED,
    community_id   UUID        NOT NULL REFERENCES community (id),
    contributor_id UUID        NOT NULL REFERENCES contributor (id),
    comments_count INTEGER     NOT NULL DEFAULT 0
);
CREATE INDEX idx_post_created_at ON post (created_at);
CREATE INDEX post_search_tsv_gin ON post USING GIN (search_tsv);

CREATE TABLE post_comment
(
    id             UUID                 DEFAULT uuidv7() PRIMARY KEY,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    post_id        UUID        NOT NULL REFERENCES post (id) ON DELETE CASCADE,
    contributor_id UUID        NOT NULL REFERENCES contributor (id) ON DELETE CASCADE,
    body_json      JSONB       NOT NULL,
    edited_at      TIMESTAMPTZ,
    edit_count     INTEGER     NOT NULL DEFAULT 0,
    deleted_at     TIMESTAMPTZ
);
CREATE INDEX idx_post_comment_created_at ON post_comment (created_at);

CREATE TABLE post_comment_revision
(
    id          UUID DEFAULT uuidv7() PRIMARY KEY,
    comment_id  UUID        NOT NULL REFERENCES post_comment (id) ON DELETE CASCADE,
    authored_at TIMESTAMPTZ NOT NULL,
    body_json   JSONB       NOT NULL
);

CREATE OR REPLACE FUNCTION update_post_comments_count() RETURNS TRIGGER AS
$$
BEGIN
    IF TG_OP = 'INSERT' THEN
        IF NEW.deleted_at IS NULL THEN
            UPDATE post SET comments_count = comments_count + 1 WHERE id = NEW.post_id;
        END IF;

    ELSIF TG_OP = 'DELETE' THEN
        IF OLD.deleted_at IS NULL THEN
            UPDATE post SET comments_count = comments_count - 1 WHERE id = OLD.post_id;
        END IF;

    ELSIF TG_OP = 'UPDATE' THEN
        IF NEW.post_id IS DISTINCT FROM OLD.post_id THEN
            IF OLD.deleted_at IS NULL THEN
                UPDATE post SET comments_count = comments_count - 1 WHERE id = OLD.post_id;
            END IF;
            IF NEW.deleted_at IS NULL THEN
                UPDATE post SET comments_count = comments_count + 1 WHERE id = NEW.post_id;
            END IF;
        ELSE
            IF OLD.deleted_at IS NULL AND NEW.deleted_at IS NOT NULL THEN
                UPDATE post SET comments_count = comments_count - 1 WHERE id = NEW.post_id;
            ELSIF OLD.deleted_at IS NOT NULL AND NEW.deleted_at IS NULL THEN
                UPDATE post SET comments_count = comments_count + 1 WHERE id = NEW.post_id;
            END IF;
        END IF;
    END IF;

    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_post_comment_comments_count_aiud
    AFTER INSERT OR DELETE OR UPDATE OF deleted_at, post_id
    ON post_comment
    FOR EACH ROW
EXECUTE FUNCTION update_post_comments_count();