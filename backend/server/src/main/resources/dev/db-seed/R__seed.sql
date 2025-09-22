INSERT INTO public.user_account (id, created_at, is_verified_human, auth_correlation_id, email_address)
VALUES ('9c77ce56-4d3e-4622-aa54-32a4b63755a1', '2025-09-15 11:51:12.777617', true,
        '3561bd91-89f0-4bb3-bf2f-b745fc415b41',
        'test1@example.com')
ON CONFLICT DO NOTHING;

INSERT INTO public.contributor (id, created_at, user_account_id, alias, discriminator)
VALUES ('afb0f543-c454-4f6e-853b-dbfbbd65c005', '2025-09-15 11:51:12.777617', '9c77ce56-4d3e-4622-aa54-32a4b63755a1',
        'Ham', 1001)
ON CONFLICT DO NOTHING;

INSERT INTO public.community (id, name)
values ('2ea5c4a6-dc9a-4da0-bc95-7e9e147bc0e7', 'Toyota 86'),
       ('d9cfb3f4-c0e6-4f06-9106-8fe22a2a2284', 'Dentistry'),
       ('4a1a64be-960f-458d-a93b-9dd182960c3f', 'Togetherness')
ON CONFLICT DO NOTHING;

INSERT INTO public.post (id, created_at, subject, body_json, body_text, community_id, contributor_id)
VALUES ('8032dd4e-1abd-434d-b92c-7c39f8ca359d', '2025-09-17 16:13:27.162757', 'A bit of everything', '{
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
        '2ea5c4a6-dc9a-4da0-bc95-7e9e147bc0e7', 'afb0f543-c454-4f6e-853b-dbfbbd65c005')
ON CONFLICT DO NOTHING;



INSERT INTO public.post (id, created_at, subject, body_json, body_text, community_id, contributor_id)
VALUES ('85200022-1089-44c6-8a08-610ce321fa9f', '2025-09-18 10:38:05.745539', 'Headers Post', '{
  "type": "doc",
  "content": [
    {
      "type": "heading",
      "attrs": {
        "level": 1
      },
      "content": [
        {
          "text": "This is the header",
          "type": "text",
          "marks": null
        }
      ]
    },
    {
      "type": "paragraph",
      "content": [
        {
          "text": "Then we talk about what''s inside. There are 2 section",
          "type": "text",
          "marks": null
        }
      ]
    },
    {
      "type": "paragraph",
      "content": [
        {
          "text": "As the leader of headers said",
          "type": "text",
          "marks": null
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
              "text": "You should not play with headers if you don''t know your sections",
              "type": "text",
              "marks": null
            }
          ]
        },
        {
          "type": "paragraph",
          "content": [
            {
              "text": "- The leader of headers",
              "type": "text",
              "marks": null
            }
          ]
        }
      ]
    },
    {
      "type": "paragraph",
      "content": [
        {
          "text": "So ",
          "type": "text",
          "marks": null
        },
        {
          "text": "be",
          "type": "text",
          "marks": [
            {
              "type": "code"
            }
          ]
        },
        {
          "text": " prepared to be headless.",
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
          "text": "1 you want to do this",
          "type": "text",
          "marks": null
        }
      ]
    },
    {
      "type": "paragraph",
      "content": [
        {
          "text": "Ok so to do this you need to do this and this",
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
                  "text": "this",
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
                  "text": "this",
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
          "text": "And then you can finish it off with some code:",
          "type": "text",
          "marks": null
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
          "text": "This is the code you need.",
          "type": "text",
          "marks": null
        }
      ]
    },
    {
      "type": "paragraph",
      "content": [
        {
          "text": "And that should be that.",
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
          "text": "2 You also want to do that",
          "type": "text",
          "marks": null
        }
      ]
    },
    {
      "type": "paragraph",
      "content": [
        {
          "text": "Ok so to do that you need to do this and that",
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
                  "text": "this",
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
                  "text": "that",
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
      "type": "heading",
      "attrs": {
        "level": 3
      },
      "content": [
        {
          "text": "This",
          "type": "text",
          "marks": null
        }
      ]
    },
    {
      "type": "paragraph",
      "content": [
        {
          "text": "this is important",
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
          "text": "That",
          "type": "text",
          "marks": null
        }
      ]
    },
    {
      "type": "paragraph",
      "content": [
        {
          "text": "this is also important",
          "type": "text",
          "marks": null
        }
      ]
    },
    {
      "type": "paragraph",
      "content": null
    }
  ]
}',
        'This is the header Then we talk about what''s inside. There are 2 section As the leader of headers said You should not play with headers if you don''t know your sections - The leader of headers So  be  prepared to be headless. 1 you want to do this Ok so to do this you need to do this and this - this - this And then you can finish it off with some code: This is the code you need. And that should be that. 2 You also want to do that Ok so to do that you need to do this and that - this - that This this is important That this is also important ',
        '2ea5c4a6-dc9a-4da0-bc95-7e9e147bc0e7', 'afb0f543-c454-4f6e-853b-dbfbbd65c005')
ON CONFLICT DO NOTHING;
