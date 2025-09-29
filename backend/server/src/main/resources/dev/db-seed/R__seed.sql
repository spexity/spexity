INSERT INTO public.user_account (id, created_at, is_verified_human, auth_correlation_id, email_address)
VALUES ('3561bd91-89f0-4bb3-bf2f-b745fc415b41', '2025-09-15 11:51:12.777617', true,
        '3561bd91-89f0-4bb3-bf2f-b745fc415b41',
        'god@example.com')
ON CONFLICT DO NOTHING;
INSERT INTO public.contributor (id, created_at, user_account_id, alias, discriminator)
VALUES ('3561bd91-89f0-4bb3-bf2f-b745fc415b41', '2025-09-15 11:51:12.777617', '3561bd91-89f0-4bb3-bf2f-b745fc415b41',
        'God', 1000)
ON CONFLICT DO NOTHING;

INSERT INTO public.user_account (id, created_at, is_verified_human, auth_correlation_id, email_address)
VALUES ('266cfe96-52e6-48fa-a01c-c41583f55a58', '2025-09-15 11:51:12.777617', true,
        '266cfe96-52e6-48fa-a01c-c41583f55a58',
        'human@example.com')
ON CONFLICT DO NOTHING;
INSERT INTO public.contributor (id, created_at, user_account_id, alias, discriminator)
VALUES ('266cfe96-52e6-48fa-a01c-c41583f55a58', '2025-09-15 11:51:12.777617', '266cfe96-52e6-48fa-a01c-c41583f55a58',
        'Human', 2000)
ON CONFLICT DO NOTHING;

INSERT INTO public.user_account (id, created_at, is_verified_human, auth_correlation_id, email_address)
VALUES ('9a5f8d52-b188-49c2-8d8e-24d4e4406a8d', '2025-09-15 11:51:12.777617', false,
        '9a5f8d52-b188-49c2-8d8e-24d4e4406a8d',
        'bot@example.com')
ON CONFLICT DO NOTHING;

INSERT INTO public.community (id, name, created_by_contributor_id)
values ('2ea5c4a6-dc9a-4da0-bc95-7e9e147bc0e7', 'Toyota 86', '3561bd91-89f0-4bb3-bf2f-b745fc415b41'),
       ('d9cfb3f4-c0e6-4f06-9106-8fe22a2a2284', 'Dentistry', '266cfe96-52e6-48fa-a01c-c41583f55a58'),
       ('4a1a64be-960f-458d-a93b-9dd182960c3f', 'Togetherness', '3561bd91-89f0-4bb3-bf2f-b745fc415b41')
ON CONFLICT DO NOTHING;

INSERT INTO public.post (id, created_at, subject, body_json, body_text, community_id, contributor_id)
VALUES ('17e1af07-467d-4cce-91fa-50e784064b2c', '2025-09-17 16:13:27.162757', 'A bit of everything', '{
  "type": "doc",
  "content": [
    {
      "type": "heading",
      "attrs": {
        "level": 1
      },
      "content": [
        {
          "text": "A bit of everything",
          "type": "text",
          "marks": null
        }
      ]
    },
    {
      "type": "heading",
      "attrs": {
        "level": 2
      },
      "content": [
        {
          "text": "Twice",
          "type": "text",
          "marks": null
        }
      ]
    },
    {
      "type": "heading",
      "attrs": {
        "level": 3
      },
      "content": [
        {
          "text": "Three",
          "type": "text",
          "marks": null
        }
      ]
    },
    {
      "type": "horizontalRule"
    },
    {
      "type": "paragraph",
      "content": [
        {
          "text": "Here is a list:",
          "type": "text",
          "marks": null
        }
      ]
    },
    {
      "type": "bulletList",
      "content": [
        {
          "type": "listItem",
          "content": [
            {
              "type": "paragraph",
              "content": [
                {
                  "text": "one ",
                  "type": "text",
                  "marks": null
                },
                {
                  "text": "1",
                  "type": "text",
                  "marks": [
                    {
                      "type": "code"
                    }
                  ]
                }
              ]
            }
          ]
        },
        {
          "type": "listItem",
          "content": [
            {
              "type": "paragraph",
              "content": [
                {
                  "text": "two",
                  "type": "text",
                  "marks": null
                }
              ]
            }
          ]
        }
      ]
    },
    {
      "type": "paragraph",
      "content": [
        {
          "text": "Remember",
          "type": "text",
          "marks": null
        },
        {
          "text": "bold",
          "type": "text",
          "marks": [
            {
              "type": "bold"
            }
          ]
        },
        {
          "text": "italic",
          "type": "text",
          "marks": [
            {
              "type": "bold"
            },
            {
              "type": "italic"
            }
          ]
        },
        {
          "text": "underline",
          "type": "text",
          "marks": [
            {
              "type": "bold"
            },
            {
              "type": "italic"
            },
            {
              "type": "underline"
            }
          ]
        },
        {
          "text": "strikenothing",
          "type": "text",
          "marks": [
            {
              "type": "bold"
            },
            {
              "type": "italic"
            },
            {
              "type": "strike"
            },
            {
              "type": "underline"
            }
          ]
        }
      ]
    },
    {
      "type": "paragraph",
      "content": [
        {
          "text": "Another one",
          "type": "text",
          "marks": [
            {
              "type": "link",
              "attrs": {
                "rel": "noopener noreferrer nofollow",
                "href": "https://google.com",
                "target": "_blank"
              }
            }
          ]
        }
      ]
    },
    {
      "type": "blockquote",
      "content": [
        {
          "type": "paragraph",
          "content": [
            {
              "text": "iamchosen",
              "type": "text",
              "marks": null
            }
          ]
        },
        {
          "type": "paragraph",
          "content": [
            {
              "text": "- random dude",
              "type": "text",
              "marks": null
            }
          ]
        }
      ]
    },
    {
      "type": "codeBlock",
      "attrs": {
        "language": null
      },
      "content": [
        {
          "text": "{ \"format\": \"json\" }",
          "type": "text",
          "marks": null
        }
      ]
    },
    {
      "type": "orderedList",
      "content": [
        {
          "type": "listItem",
          "content": [
            {
              "type": "paragraph",
              "content": [
                {
                  "text": "Mohicans",
                  "type": "text",
                  "marks": null
                }
              ]
            }
          ]
        },
        {
          "type": "listItem",
          "content": [
            {
              "type": "paragraph",
              "content": [
                {
                  "text": "Of",
                  "type": "text",
                  "marks": null
                }
              ]
            }
          ]
        },
        {
          "type": "listItem",
          "content": [
            {
              "type": "paragraph",
              "content": [
                {
                  "text": "Last",
                  "type": "text",
                  "marks": null
                }
              ]
            }
          ]
        }
      ]
    },
    {
      "type": "horizontalRule"
    },
    {
      "type": "paragraph",
      "content": null
    }
  ]
}',
        'A bit of everything Twice Three   Here is a list: - one 1 - two Remember bold italic underline strikenothing Another one iamchosen - random dude { "format": "json" } - Mohicans - Of - Last ',
        '2ea5c4a6-dc9a-4da0-bc95-7e9e147bc0e7', '3561bd91-89f0-4bb3-bf2f-b745fc415b41')
ON CONFLICT DO NOTHING;

INSERT INTO public.post (id, created_at, subject, body_json, body_text, community_id, contributor_id)
VALUES ('87471c08-ae55-443c-896c-ebbcab50453c', '2025-09-17 16:13:27.162757', 'No comments ever', '{
  "type": "doc",
  "content": [
    {
      "type": "heading",
      "attrs": {
        "level": 1
      },
      "content": [
        {
          "text": "No comments ever",
          "type": "text",
          "marks": null
        }
      ]
    }
  ]
}', 'No comments ever', '4a1a64be-960f-458d-a93b-9dd182960c3f', '3561bd91-89f0-4bb3-bf2f-b745fc415b41')
ON CONFLICT DO NOTHING;

INSERT INTO public.post (id, created_at, subject, body_json, body_text, community_id, contributor_id)
VALUES ('85200022-1089-44c6-8a08-610ce321fa9f', '2025-09-18 10:38:05.745539', 'No comments yet', '{
  "type": "doc",
  "content": [
    {
      "type": "paragraph",
      "content": [
        {
          "text": "No comments yet",
          "type": "text",
          "marks": null
        }
      ]
    }
  ]
}',
        'No comments yet',
        'd9cfb3f4-c0e6-4f06-9106-8fe22a2a2284', '266cfe96-52e6-48fa-a01c-c41583f55a58')
ON CONFLICT DO NOTHING;

INSERT INTO public.post_comment (id, post_id, contributor_id, body_json, created_at)
SELECT uuidv7(),
       '17e1af07-467d-4cce-91fa-50e784064b2c',
       '3561bd91-89f0-4bb3-bf2f-b745fc415b41',
       jsonb_build_object(
               'type', 'doc',
               'content', jsonb_build_array(
                       jsonb_build_object(
                               'type', 'paragraph',
                               'content', jsonb_build_array(
                                       jsonb_build_object('type', 'text', 'text', format('Seed comment %s', gs))
                                          )
                       )
                          )
       ),
       (timestamp with time zone '2025-09-17 18:00:00+00') + ((gs - 1) * interval '1 minute')
FROM generate_series(1, 110) AS gs
ON CONFLICT DO NOTHING;
