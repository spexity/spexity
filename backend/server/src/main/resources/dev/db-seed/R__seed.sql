INSERT INTO public.user_account (id, created_at, auth_correlation_id, email_address)
VALUES ('9c77ce56-4d3e-4622-aa54-32a4b63755a1', '2025-09-15 11:51:12.777617', '3561bd91-89f0-4bb3-bf2f-b745fc415b41',
        'test1@example.com')
ON CONFLICT DO NOTHING;

INSERT INTO public.contributor (id, created_at, user_account_id, alias, discriminator)
VALUES ('afb0f543-c454-4f6e-853b-dbfbbd65c005', '2025-09-15 11:51:12.777617', '9c77ce56-4d3e-4622-aa54-32a4b63755a1',
        'Ham', 1001)
ON CONFLICT DO NOTHING;

INSERT INTO public.community (id, name, slug)
values ('2ea5c4a6-dc9a-4da0-bc95-7e9e147bc0e7', 'Toyota 86', 't86'),
       ('d9cfb3f4-c0e6-4f06-9106-8fe22a2a2284', 'Dentistry', 'dt'),
       ('4a1a64be-960f-458d-a93b-9dd182960c3f', 'Togetherness', 'tog')
ON CONFLICT DO NOTHING;

INSERT INTO public.post (subject, body, community_id, contributor_id)
VALUES ('Best car', 'What do you think?', '2ea5c4a6-dc9a-4da0-bc95-7e9e147bc0e7',
        'afb0f543-c454-4f6e-853b-dbfbbd65c005'),
       ('Best chair', 'What do you think?', 'd9cfb3f4-c0e6-4f06-9106-8fe22a2a2284',
        'afb0f543-c454-4f6e-853b-dbfbbd65c005'),
       ('Best togetherness', 'What do you think?', '4a1a64be-960f-458d-a93b-9dd182960c3f',
        'afb0f543-c454-4f6e-853b-dbfbbd65c005')
ON CONFLICT DO NOTHING;
