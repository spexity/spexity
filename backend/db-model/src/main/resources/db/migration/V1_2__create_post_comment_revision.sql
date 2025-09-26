CREATE TABLE post_comment_revision
(
    id              UUID        DEFAULT uuidv7() PRIMARY KEY,
    comment_id      UUID        NOT NULL REFERENCES post_comment (id) ON DELETE CASCADE,
    body_json_prev  JSONB       NOT NULL,
    body_text_prev  TEXT        NOT NULL,
    edited_at       TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_post_comment_revision_comment ON post_comment_revision (comment_id);
