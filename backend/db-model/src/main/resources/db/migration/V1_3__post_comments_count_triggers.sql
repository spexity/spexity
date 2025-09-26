CREATE OR REPLACE FUNCTION update_post_comments_count() RETURNS TRIGGER AS
$$
DECLARE
    target_post_id UUID;
BEGIN
    IF (TG_OP = 'DELETE') THEN
        target_post_id := OLD.post_id;
    ELSE
        target_post_id := NEW.post_id;
    END IF;

    UPDATE post
    SET comments_count = (
        SELECT COUNT(*)
        FROM post_comment pc
        WHERE pc.post_id = target_post_id
          AND pc.deleted_at IS NULL
    )
    WHERE id = target_post_id;

    IF (TG_OP = 'DELETE') THEN
        RETURN OLD;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER post_comment_after_insert
    AFTER INSERT
    ON post_comment
    FOR EACH ROW
EXECUTE FUNCTION update_post_comments_count();

CREATE TRIGGER post_comment_after_delete
    AFTER DELETE
    ON post_comment
    FOR EACH ROW
EXECUTE FUNCTION update_post_comments_count();

CREATE TRIGGER post_comment_after_update
    AFTER UPDATE OF deleted_at
    ON post_comment
    FOR EACH ROW
EXECUTE FUNCTION update_post_comments_count();
