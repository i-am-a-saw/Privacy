begin;

create extension if not exists pgtap with schema extensions;

set local search_path = public, extensions;

select plan(32);

-- 1-5: Schema and RLS configuration.

select has_table(
    'public', 'conversations',
    'conversations table exists'
);

select has_table(
    'public', 'conversation_members',
    'conversation_members table exists'
);

select has_table(
    'public', 'messages',
    'messages table exists'
);
select ok(
    (
        select count(*) = 3 and bool_and(relrowsecurity)
        from pg_class
        where oid in (
            'public.conversations'::regclass,
            'public.conversation_members'::regclass,
            'public.messages'::regclass
        )
    ),
    'RLS is enabled on all three chat tables'
);

select col_type_is(
    'public', 'messages', 'payload', 'bytea',
    'message payload is binary'
);

-- Fixtures are inserted as the database administrator.
-- Everything is rolled back at the end.

insert into auth.users (id, email) values
    (
        '10000000-0000-4000-8000-000000000001',
        'chat-test-alice@example.invalid'
    ),
    (
        '10000000-0000-4000-8000-000000000002',
        'chat-test-bob@example.invalid'
    ),
    (
        '10000000-0000-4000-8000-000000000003',
        'chat-test-carol@example.invalid'
    );

insert into public.profiles (id, username, display_name) values
    (
        '10000000-0000-4000-8000-000000000001',
        'tap_m2_alice',
        'Alice'
    ),
    (
        '10000000-0000-4000-8000-000000000002',
        'tap_m2_bob',
        'Bob'
    ),
    (
        '10000000-0000-4000-8000-000000000003',
        'tap_m2_carol',
        'Carol'
    );

insert into public.conversations (
    id, type, direct_key, created_by
) values
    (
        '20000000-0000-4000-8000-000000000001',
        'direct',
        '10000000-0000-4000-8000-000000000001:10000000-0000-4000-8000-000000000002',
        '10000000-0000-4000-8000-000000000001'
    ),
    (
        '20000000-0000-4000-8000-000000000002',
        'direct',
        '10000000-0000-4000-8000-000000000002:10000000-0000-4000-8000-000000000003',
        '10000000-0000-4000-8000-000000000002'
    );

insert into public.conversation_members (
    conversation_id, user_id, role
) values
    (
        '20000000-0000-4000-8000-000000000001',
        '10000000-0000-4000-8000-000000000001',
        'admin'
    ),
    (
        '20000000-0000-4000-8000-000000000001',
        '10000000-0000-4000-8000-000000000002',
        'member'
    ),
    (
        '20000000-0000-4000-8000-000000000002',
        '10000000-0000-4000-8000-000000000002',
        'admin'
    ),
    (
        '20000000-0000-4000-8000-000000000002',
        '10000000-0000-4000-8000-000000000003',
        'member'
    );

insert into public.messages (
    id, conversation_id, sender_id, payload
) values
    (
        '30000000-0000-4000-8000-000000000001',
        '20000000-0000-4000-8000-000000000001',
        '10000000-0000-4000-8000-000000000001',
        decode('6869', 'hex')
    ),
    (
        '30000000-0000-4000-8000-000000000002',
        '20000000-0000-4000-8000-000000000002',
        '10000000-0000-4000-8000-000000000003',
        decode('6869', 'hex')
    );

-- 6-7: Uniqueness constraints.

select throws_ok(
    $$
        insert into public.conversations (
            type, direct_key, created_by
        ) values (
            'direct',
            '10000000-0000-4000-8000-000000000001:10000000-0000-4000-8000-000000000002',
            '10000000-0000-4000-8000-000000000001'
        )
    $$,
    '23505', null,
    'a direct pair cannot have two conversations'
);

select throws_ok(
    $$
        insert into public.conversation_members (
            conversation_id, user_id
        ) values (
            '20000000-0000-4000-8000-000000000001',
            '10000000-0000-4000-8000-000000000001'
        )
    $$,
    '23505', null,
    'a user cannot have duplicate membership'
);

-- Act as Alice.

set local role authenticated;
set local request.jwt.claim.sub =
    '10000000-0000-4000-8000-000000000001';

-- 8-9: Membership helper.

select is(
    private.is_member('20000000-0000-4000-8000-000000000001'),
    true,
    'Alice belongs to the Alice-Bob conversation'
);

select is(
    private.is_member('20000000-0000-4000-8000-000000000002'),
    false,
    'Alice does not belong to the Bob-Carol conversation'
);

-- 10-13: Alice reads only her conversation and peers.

select results_eq(
    $$
        select id from public.conversations
        where id in (
            '20000000-0000-4000-8000-000000000001',
            '20000000-0000-4000-8000-000000000002'
        )
        order by id
    $$,
    array['20000000-0000-4000-8000-000000000001'::uuid],
    'Alice reads only her conversation'
);


select results_eq(
    $$
        select user_id from public.conversation_members
        where conversation_id in (
            '20000000-0000-4000-8000-000000000001',
            '20000000-0000-4000-8000-000000000002'
        )
        order by user_id
    $$,
    array[
        '10000000-0000-4000-8000-000000000001'::uuid,
        '10000000-0000-4000-8000-000000000002'::uuid
    ],
    'Alice reads both members of her conversation'
);

select results_eq(
    $$
        select id from public.messages
        where conversation_id in (
            '20000000-0000-4000-8000-000000000001',
            '20000000-0000-4000-8000-000000000002'
        )
        order by id
    $$,
    array['30000000-0000-4000-8000-000000000001'::uuid],
    'Alice cannot read Bob-Carol messages'
);

select results_eq(
    $$
        select username from public.profiles
        where username in (
            'tap_m2_alice', 'tap_m2_bob', 'tap_m2_carol'
        )
        order by username
    $$,
    array['tap_m2_alice'::text, 'tap_m2_bob'::text],
    'Alice reads her profile and Bob but not Carol'
);

-- 14-15: Existing exact discovery still works.

select results_eq(
    $$
        select username
        from public.find_profile_by_username('tap_m2_carol')
    $$,
    array['tap_m2_carol'::text],
    'exact lookup still finds a non-peer'
);

select is_empty(
    $$
        select *
        from public.find_profile_by_username('tap_m2_car')
    $$,
    'username prefix lookup returns nothing'
);

-- 16-17: Valid message and server timestamp.

select lives_ok(
    $$
        insert into public.messages (
            id, conversation_id, sender_id, payload, cipher_suite
        ) values (
            '30000000-0000-4000-8000-000000000003',
            '20000000-0000-4000-8000-000000000001',
            '10000000-0000-4000-8000-000000000001',
            decode('6869', 'hex'),
            'none-v0'
        )
    $$,
    'Alice inserts a message into her conversation'
);

select is(
    (
        select created_at = now()
        from public.messages
        where id = '30000000-0000-4000-8000-000000000003'
    ),
    true,
    'database supplies the message timestamp'
);

-- 18-21: Forgery, outsider writes, timestamps, duplicates.

select throws_ok(
    $$
        insert into public.messages (
            id, conversation_id, sender_id, payload
        ) values (
            '30000000-0000-4000-8000-000000000004',
            '20000000-0000-4000-8000-000000000001',
            '10000000-0000-4000-8000-000000000002',
            decode('6869', 'hex')
        )
    $$,
    '42501', null,
    'Alice cannot impersonate Bob'
);

select throws_ok(
    $$
        insert into public.messages (
            id, conversation_id, sender_id, payload
        ) values (
            '30000000-0000-4000-8000-000000000004',
            '20000000-0000-4000-8000-000000000002',
            '10000000-0000-4000-8000-000000000001',
            decode('6869', 'hex')
        )
    $$,
    '42501', null,
    'Alice cannot insert into Bob-Carol'
);

select throws_ok(
    $$
        insert into public.messages (
            id, conversation_id, sender_id, payload, created_at
        ) values (
            '30000000-0000-4000-8000-000000000004',
            '20000000-0000-4000-8000-000000000001',
            '10000000-0000-4000-8000-000000000001',
            decode('6869', 'hex'),
            '2000-01-01T00:00:00Z'
        )
    $$,
    '42501', null,
    'Alice cannot supply a server timestamp'
);

select throws_ok(
    $$
        insert into public.messages (
            id, conversation_id, sender_id, payload
        ) values (
            '30000000-0000-4000-8000-000000000003',
            '20000000-0000-4000-8000-000000000001',
            '10000000-0000-4000-8000-000000000001',
            decode('6869', 'hex')
        )
    $$,
    '23505', null,
    'a message UUID cannot create a second row'
);

-- 22-25: Protected operations remain unavailable.

select throws_ok(
    $$
        insert into public.conversations (
            type, direct_key, created_by
        ) values (
            'direct',
            '10000000-0000-4000-8000-000000000001:10000000-0000-4000-8000-000000000003',
            '10000000-0000-4000-8000-000000000001'
        )
    $$,
    '42501', null,
    'clients cannot directly create conversations'
);

select throws_ok(
    $$
        insert into public.conversation_members (
            conversation_id, user_id
        ) values (
            '20000000-0000-4000-8000-000000000001',
            '10000000-0000-4000-8000-000000000003'
        )
    $$,
    '42501', null,
    'Alice cannot add Carol directly'
);

select throws_ok(
    $$
        update public.messages
        set payload = decode('00', 'hex')
        where id = '30000000-0000-4000-8000-000000000003'
    $$,
    '42501', null,
    'clients cannot rewrite messages'
);

select throws_ok(
    $$
        delete from public.messages
        where id = '30000000-0000-4000-8000-000000000003'
    $$,
    '42501', null,
    'clients cannot delete server messages directly'
);

-- Act as Carol.

reset role;
set local role authenticated;
set local request.jwt.claim.sub =
    '10000000-0000-4000-8000-000000000003';

-- 26-29: The same boundaries apply to another user.

select results_eq(
    $$
        select id from public.conversations
        where id in (
            '20000000-0000-4000-8000-000000000001',
            '20000000-0000-4000-8000-000000000002'
        )
        order by id
    $$,
    array['20000000-0000-4000-8000-000000000002'::uuid],
    'Carol reads only Bob-Carol'
);

select results_eq(
    $$
        select id from public.messages
        where conversation_id in (
            '20000000-0000-4000-8000-000000000001',
            '20000000-0000-4000-8000-000000000002'
        )
        order by id
    $$,
    array['30000000-0000-4000-8000-000000000002'::uuid],
    'Carol cannot read Alice-Bob messages'
);

select results_eq(
    $$
        select username from public.profiles
        where username in (
            'tap_m2_alice', 'tap_m2_bob', 'tap_m2_carol'
        )
        order by username
    $$,
    array['tap_m2_bob'::text, 'tap_m2_carol'::text],
    'Carol reads Bob but not Alice'
);

select throws_ok(
    $$
        insert into public.messages (
            id, conversation_id, sender_id, payload
        ) values (
            '30000000-0000-4000-8000-000000000004',
            '20000000-0000-4000-8000-000000000001',
            '10000000-0000-4000-8000-000000000003',
            decode('6869', 'hex')
        )
    $$,
    '42501', null,
    'Carol cannot insert into Alice-Bob'
);

-- Act as an anonymous client.

reset role;
set local role anon;
set local request.jwt.claim.sub = '';

-- 30-32: Anonymous access is denied.

select throws_ok(
    $$select id from public.conversations$$,
    '42501', null,
    'anonymous clients cannot read conversations'
);

select throws_ok(
    $$select id from public.messages$$,
    '42501', null,
    'anonymous clients cannot read messages'
);

select throws_ok(
    $$
        select private.is_member(
            '20000000-0000-4000-8000-000000000001'
        )
    $$,
    '42501', null,
    'anonymous clients cannot execute the membership helper'
);

reset role;

select * from finish();

rollback;
