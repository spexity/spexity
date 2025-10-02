# Posts

## Overview
- Publicly readable; creation requires an authenticated, verified contributor.
- Post bodies are structured documents stored as JSON and rendered to sanitized HTML for display.
- Previews use a plain‑text serialization (truncated to ~512 chars in lists).
- Community postsCount and per‑post commentsCount are maintained and exposed in views.

## Validation and limits
- Subject: 10–512 characters.
- Body: must contain meaningful text (validated via document text serialization).
- Terms and conditions checkbox must be accepted on creation.

## Permissions
- Read: anyone.
- Create: authenticated, verified contributor.


## Client/UI notes
- Form constraints mirror backend: subject minlength/maxlength, body required (editor), explicit T&C checkbox.
- On create success, client navigates to Post view.
