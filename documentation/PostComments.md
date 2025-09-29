# Post Comments

## Overview
- Anyone can read comments; creating, editing, and deleting require an authenticated, verified contributor.
- Comments are rendered from a structured document into sanitized HTML to prevent XSS.
- Deleting a comment is soft-delete (kept in DB, hidden content shown as a placeholder).

## Validation and limits
- Content must contain meaningful text (non-blank after trimming serialized text).
- Max length: 2000 characters (based on text serialization of the document).
- Edit limit: up to 2 edits per comment; subsequent attempts return 409 Conflict.
- Rate limit (create): 1 comment per 30 seconds per contributor.
  - On limit: 429 Too Many Requests with Retry-After header.

## Permissions
- Create: authenticated, verified contributor.
- Edit: only the original author; not allowed on deleted comments.
- Delete: only the original author; soft-delete sets deletedAt.

## Client/UI notes
- “Edited” badge shown when editCount > 0 and comment not deleted.
- Deleted comments display a static placeholder instead of content.
- Pagination is offset-based; “Load more” fetches next page and appends unique items.
