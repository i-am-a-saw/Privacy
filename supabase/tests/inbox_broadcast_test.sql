begin;

create extension if not exists pgtap with schema extensions;
set local search_path = public, extensions;

select plan(24);

-- 1-3: Trigger and permission configuration.
select has_trigger(
    'public', 'messages', 'messages_broadcast_inbox',
    'Message insert trigger exists'
);

select ok(
    (select relrowsecurity from pg_class
     where oid = 'realtime.messages'::regclass),
    'Realtime message RLS is enabled'
);

select is(
    has_function_privilege(
        'authenticated',
        'private.broadcast_message_to_inboxes()',
        'EXECUTE'
    ),
    false,
    'Clients cannot execute the trigger function directly'
);

insert into auth.users (id, email) values
    ('60000000-0000-4000-8000-000000000001', 'inbox-alice@example.invalid'),
    ('60000000-0000-4000-8000-000000000002', 'inbox-bob@example.invalid'),
    ('60000000-0000-4000-8000-000000000003', 'inbox-carol@example.invalid');

insert into public.profiles (id, username) values
    ('60000000-0000-4000-8000-000000000001', 'inbox_m2_alice'),
    ('60000000-0000-4000-8000-000000000002', 'inbox_m2_bob'),
    ('60000000-0000-4000-8000-000000000003', 'inbox_m2_carol');

insert into public.conversations (id, type, direct_key, created_by)
values (
    '60000000-0000-4000-8000-000000000010',
    'direct',
    '60000000-0000-4000-8000-000000000001:60000000-0000-4000-8000-000000000002',
    '60000000-0000-4000-8000-000000000001'
);

insert into public.conversation_members (conversation_id, user_id)
values
    ('60000000-0000-4000-8000-000000000010', '60000000-0000-4000-8000-000000000001'),
    ('60000000-0000-4000-8000-000000000010', '60000000-0000-4000-8000-000000000002');

-- 4: Insert through the same permission boundary as the app.
set local role authenticated;
set local request.jwt.claim.sub = '60000000-0000-4000-8000-000000000001';

select lives_ok(
    $$insert into public.messages (
        id, conversation_id, sender_id, payload, cipher_suite
    ) values (
        '60000000-0000-4000-8000-000000000101',
        '60000000-0000-4000-8000-000000000010',
        '60000000-0000-4000-8000-000000000001',
        decode('68656c6c6f', 'hex'),
        'none-v0'
    )$$,
    'Authenticated member can insert and trigger a broadcast'
);

reset role;

-- 5-12: Routing, privacy flag, event and minimal payload.
select is(
    (select count(*) from realtime.messages
     where payload ->> 'id' = '60000000-0000-4000-8000-000000000101'),
    1::bigint,
    'Exactly one broadcast is created for this direct message'
);

select is(
    (select topic from realtime.messages
     where payload ->> 'id' = '60000000-0000-4000-8000-000000000101'),
    'user:60000000-0000-4000-8000-000000000002',
    'Broadcast targets Bob inbox'
);

select is(
    (select private from realtime.messages
     where payload ->> 'id' = '60000000-0000-4000-8000-000000000101'),
    true,
    'Broadcast uses a private channel'
);

select is(
    (select event from realtime.messages
     where payload ->> 'id' = '60000000-0000-4000-8000-000000000101'),
    'message_created',
    'Broadcast has the agreed event name'
);

select is(
    (select extension from realtime.messages
     where payload ->> 'id' = '60000000-0000-4000-8000-000000000101'),
    'broadcast',
    'Notification is a Broadcast event'
);

select is(
    (select payload from realtime.messages
     where payload ->> 'id' = '60000000-0000-4000-8000-000000000101'),
    jsonb_build_object(
        'id', '60000000-0000-4000-8000-000000000101'::uuid,
        'conversation_id', '60000000-0000-4000-8000-000000000010'::uuid
    ),
    'Payload contains exactly the message and conversation IDs'
);

select is_empty(
    $$select id from realtime.messages
      where payload ->> 'id' = '60000000-0000-4000-8000-000000000101'
        and topic = 'user:60000000-0000-4000-8000-000000000001'$$,
    'Sender does not receive an incoming-message broadcast'
);

select is_empty(
    $$select id from realtime.messages
      where payload ->> 'id' = '60000000-0000-4000-8000-000000000101'
        and topic = 'user:60000000-0000-4000-8000-000000000003'$$,
    'Non-member receives no broadcast'
);

-- 13-15: Duplicate inserts and updates do not send new events.
set local role authenticated;
set local request.jwt.claim.sub = '60000000-0000-4000-8000-000000000001';

select lives_ok(
    $$insert into public.messages (
        id, conversation_id, sender_id, payload, cipher_suite
    ) values (
        '60000000-0000-4000-8000-000000000101',
        '60000000-0000-4000-8000-000000000010',
        '60000000-0000-4000-8000-000000000001',
        decode('68656c6c6f', 'hex'),
        'none-v0'
    ) on conflict (id) do nothing$$,
    'Retry of an existing message UUID succeeds without insertion'
);

reset role;

select is(
    (select count(*) from realtime.messages
     where payload ->> 'id' = '60000000-0000-4000-8000-000000000101'),
    1::bigint,
    'Duplicate insertion does not duplicate the broadcast'
);

-- Administrative update only: clients have no message UPDATE grant.
update public.messages
set payload = decode('627965', 'hex')
where id = '60000000-0000-4000-8000-000000000101';

select is(
    (select count(*) from realtime.messages
     where payload ->> 'id' = '60000000-0000-4000-8000-000000000101'),
    1::bigint,
    'Updating a message does not run the insert trigger'
);

-- 16-17: Rolled-back inserts leave no message or broadcast.
savepoint before_rolled_back_message;

set local role authenticated;
set local request.jwt.claim.sub = '60000000-0000-4000-8000-000000000001';

insert into public.messages (id, conversation_id, sender_id, payload)
values (
    '60000000-0000-4000-8000-000000000102',
    '60000000-0000-4000-8000-000000000010',
    '60000000-0000-4000-8000-000000000001',
    decode('74657374', 'hex')
);

reset role;
rollback to savepoint before_rolled_back_message;

select is_empty(
    $$select id from realtime.messages
      where payload ->> 'id' = '60000000-0000-4000-8000-000000000102'$$,
    'Rolled-back insertion leaves no broadcast'
);

select is_empty(
    $$select id from public.messages
      where id = '60000000-0000-4000-8000-000000000102'$$,
    'Rolled-back insertion leaves no stored message'
);

-- Owner-created probes let us test receive permissions.
insert into realtime.messages (topic, extension, payload, event, private)
values
    (
        'user:60000000-0000-4000-8000-000000000001',
        'broadcast',
        '{}'::jsonb,
        'inbox_test_probe',
        true
    ),
    (
        'user:60000000-0000-4000-8000-000000000002',
        'presence',
        '{}'::jsonb,
        'inbox_test_probe',
        true
    );

-- 18-21: Bob may receive Broadcast only on his own topic.
set local role authenticated;
set local request.jwt.claim.sub = '60000000-0000-4000-8000-000000000002';
set local realtime.topic = 'user:60000000-0000-4000-8000-000000000002';

select results_eq(
    $$select topic from realtime.messages
      where event in ('message_created', 'inbox_test_probe')
        and topic in (
            'user:60000000-0000-4000-8000-000000000001',
            'user:60000000-0000-4000-8000-000000000002'
        ) order by topic$$,
    array['user:60000000-0000-4000-8000-000000000002'::text],
    'Bob receives only his own Broadcast row'
);

set local realtime.topic = 'user:60000000-0000-4000-8000-000000000001';

select is_empty(
    $$select id from realtime.messages
      where topic = 'user:60000000-0000-4000-8000-000000000001'$$,
    'Bob cannot authorize a subscription to Alice inbox'
);

set local realtime.topic = '';

select is_empty(
    $$select id from realtime.messages
      where topic = 'user:60000000-0000-4000-8000-000000000002'$$,
    'Missing channel topic grants no access'
);

set local realtime.topic = 'user:60000000-0000-4000-8000-000000000002';

select is_empty(
    $$select id from realtime.messages
      where topic = 'user:60000000-0000-4000-8000-000000000002' and extension = 'presence'$$,
    'Inbox policy does not grant Presence access'
);

-- 22-24: Owner access, anonymous denial and no client publishing.
set local request.jwt.claim.sub = '60000000-0000-4000-8000-000000000001';
set local realtime.topic = 'user:60000000-0000-4000-8000-000000000001';

select results_eq(
    $$select topic from realtime.messages
      where topic = 'user:60000000-0000-4000-8000-000000000001'
        and event = 'inbox_test_probe'$$,
    array['user:60000000-0000-4000-8000-000000000001'::text],
    'Alice can authorize her own inbox'
);

reset role;
set local role anon;
set local request.jwt.claim.sub = '';

select is_empty(
    $$select id from realtime.messages
      where topic = 'user:60000000-0000-4000-8000-000000000001'$$,
    'Anonymous clients cannot read a private inbox'
);

reset role;
set local role authenticated;
set local request.jwt.claim.sub = '60000000-0000-4000-8000-000000000002';
set local realtime.topic = 'user:60000000-0000-4000-8000-000000000002';

select throws_ok(
    $$insert into realtime.messages (
        topic, extension, payload, event, private
    ) values (
        'user:60000000-0000-4000-8000-000000000002',
        'broadcast',
        '{}'::jsonb,
        'message_created',
        true
    )$$,
    '42501', null,
    'Clients cannot publish forged inbox notifications'
);

reset role;
select * from finish();
rollback;
