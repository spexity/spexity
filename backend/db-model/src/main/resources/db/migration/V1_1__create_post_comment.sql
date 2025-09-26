ALTER TABLE post
    ADD COLUMN comments_count INTEGER NOT NULL DEFAULT 0;

CREATE TABLE post_comment
(
    id               UUID        DEFAULT uuidv7() PRIMARY KEY,
    post_id          UUID        NOT NULL REFERENCES post (id) ON DELETE CASCADE,
    contributor_id   UUID        NOT NULL REFERENCES contributor (id) ON DELETE CASCADE,
    body_json        JSONB       NOT NULL,
    body_text        TEXT        NOT NULL,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    edited_at        TIMESTAMPTZ,
    edit_count       INTEGER     NOT NULL DEFAULT 0 CHECK (edit_count BETWEEN 0 AND 2),
    deleted_at       TIMESTAMPTZ,
    deleted_by_author BOOLEAN    NOT NULL DEFAULT FALSE,
    CONSTRAINT post_comment_body_text_length CHECK (
        char_length(btrim(body_text)) BETWEEN 1 AND 2000
    )
);

CREATE INDEX idx_post_comment_post_created_at ON post_comment (post_id, created_at);
CREATE INDEX idx_post_comment_contributor ON post_comment (contributor_id);
CREATE INDEX idx_post_comment_active ON post_comment (post_id) WHERE deleted_at IS NULL;

UPDATE post
SET comments_count = 0;
