begin;

create extension if not exists pgtap with schema extensions;
set local search_path = public, extensions;

select plan(30);

-- 1-3: Function contract and security mode.
select has_function(
    'public', 'fetch_messages_since',
    array['timestamp with time zone', 'uuid', 'integer'],
    'Catch-up function has the expected arguments'
);

select is(
    (select prosecdef from pg_proc
     where oid = 'public.fetch_messages_since(timestamptz,uuid,integer)'::regprocedure),
    false,
    'Catch-up runs with caller permissions'
);

select ok(
    (select proretset and prorettype = 'public.messages'::regtype
     from pg_proc
     where oid = 'public.fetch_messages_since(timestamptz,uuid,integer)'::regprocedure),
    'Catch-up returns a set of complete message rows'
);

insert into auth.users (id, email) values
    ('70000000-0000-4000-8000-000000000001', 'catchup-alice@example.invalid'),
    ('70000000-0000-4000-8000-000000000002', 'catchup-bob@example.invalid'),
    ('70000000-0000-4000-8000-000000000003', 'catchup-carol@example.invalid'),
    ('70000000-0000-4000-8000-000000000004', 'catchup-empty@example.invalid');

insert into public.profiles (id, username) values
    ('70000000-0000-4000-8000-000000000001', 'catchup_m2_alice'),
    ('70000000-0000-4000-8000-000000000002', 'catchup_m2_bob'),
    ('70000000-0000-4000-8000-000000000003', 'catchup_m2_carol'),
    ('70000000-0000-4000-8000-000000000004', 'catchup_m2_empty');

insert into public.conversations (id, type, direct_key, created_by)
values
    ('70000000-0000-4000-8000-000000000010', 'direct', '70000000-0000-4000-8000-000000000001:70000000-0000-4000-8000-000000000002', '70000000-0000-4000-8000-000000000001'),
    ('70000000-0000-4000-8000-000000000020', 'direct', '70000000-0000-4000-8000-000000000001:70000000-0000-4000-8000-000000000003', '70000000-0000-4000-8000-000000000001'),
    ('70000000-0000-4000-8000-000000000030', 'direct', '70000000-0000-4000-8000-000000000002:70000000-0000-4000-8000-000000000003', '70000000-0000-4000-8000-000000000002');

insert into public.conversation_members (conversation_id, user_id)
values
    ('70000000-0000-4000-8000-000000000010', '70000000-0000-4000-8000-000000000001'),
    ('70000000-0000-4000-8000-000000000010', '70000000-0000-4000-8000-000000000002'),
    ('70000000-0000-4000-8000-000000000020', '70000000-0000-4000-8000-000000000001'),
    ('70000000-0000-4000-8000-000000000020', '70000000-0000-4000-8000-000000000003'),
    ('70000000-0000-4000-8000-000000000030', '70000000-0000-4000-8000-000000000002'),
    ('70000000-0000-4000-8000-000000000030', '70000000-0000-4000-8000-000000000003');

-- Deliberately unordered; timestamps are supplied only by the test owner.
insert into public.messages (
    id, conversation_id, sender_id, payload, created_at
)
values
    ('70000000-0000-4000-8000-000000000104', '70000000-0000-4000-8000-000000000020', '70000000-0000-4000-8000-000000000001', decode('34', 'hex'), '2020-01-01T10:00:02Z'),
    ('70000000-0000-4000-8000-000000000102', '70000000-0000-4000-8000-000000000020', '70000000-0000-4000-8000-000000000003', decode('32', 'hex'), '2020-01-01T10:00:00.123456Z'),
    ('70000000-0000-4000-8000-000000000105', '70000000-0000-4000-8000-000000000030', '70000000-0000-4000-8000-000000000002', decode('35', 'hex'), '2020-01-01T10:00:00.5Z'),
    ('70000000-0000-4000-8000-000000000101', '70000000-0000-4000-8000-000000000010', '70000000-0000-4000-8000-000000000002', decode('68656c6c6f', 'hex'), '2020-01-01T10:00:00.123456Z'),
    ('70000000-0000-4000-8000-000000000103', '70000000-0000-4000-8000-000000000010', '70000000-0000-4000-8000-000000000001', decode('33', 'hex'), '2020-01-01T10:00:01Z');

set local role authenticated;
set local request.jwt.claim.sub = '70000000-0000-4000-8000-000000000001';

-- 4-9: Ordering, tied timestamps and cursor boundaries.
select results_eq(
    $$select id from public.fetch_messages_since(null, null, 2)$$,
    array['70000000-0000-4000-8000-000000000101'::uuid, '70000000-0000-4000-8000-000000000102'::uuid],
    'First page is ordered across both Alice conversations'
);

select results_eq(
    $$select id from public.fetch_messages_since(
        '2020-01-01T10:00:00.123456Z', '70000000-0000-4000-8000-000000000102', 2
    )$$,
    array['70000000-0000-4000-8000-000000000103'::uuid, '70000000-0000-4000-8000-000000000104'::uuid],
    'Next page starts strictly after the previous last row'
);

select results_eq(
    $$select id from public.fetch_messages_since(
        '2020-01-01T10:00:00.123456Z', '70000000-0000-4000-8000-000000000101', 200
    )$$,
    array['70000000-0000-4000-8000-000000000102'::uuid, '70000000-0000-4000-8000-000000000103'::uuid, '70000000-0000-4000-8000-000000000104'::uuid],
    'A larger UUID at the same timestamp is not skipped'
);

select is_empty(
    $$select id from public.fetch_messages_since(
        '2020-01-01T10:00:02Z', '70000000-0000-4000-8000-000000000104', 200
    )$$,
    'Cursor at the final row returns an empty page'
);

select is_empty(
    $$select id from public.fetch_messages_since(
        '2030-01-01T00:00:00Z', '70000000-0000-4000-8000-000000000104', 200
    )$$,
    'Cursor beyond available messages returns an empty page'
);

select results_eq(
    $$select id from public.fetch_messages_since(
        '2020-01-01T10:00:00.123456Z', '70000000-0000-4000-8000-000000000099', 200
    )$$,
    array[
        '70000000-0000-4000-8000-000000000101'::uuid, '70000000-0000-4000-8000-000000000102'::uuid,
        '70000000-0000-4000-8000-000000000103'::uuid, '70000000-0000-4000-8000-000000000104'::uuid
    ],
    'Cursor is a boundary and need not identify a stored row'
);

-- 10-14: Limits and returned data.
select is(
    (select count(*) from public.fetch_messages_since()),
    4::bigint,
    'Omitted arguments fetch the first page'
);

select results_eq(
    $$select id from public.fetch_messages_since(null, null, 1)$$,
    array['70000000-0000-4000-8000-000000000101'::uuid],
    'Page size one returns only the earliest visible row'
);

select is(
    (select count(*) from public.fetch_messages_since(null, null, 200)),
    4::bigint,
    'Maximum allowed page size is accepted'
);

select is(
    (select encode(payload, 'hex') from public.fetch_messages_since()
     where id = '70000000-0000-4000-8000-000000000101'),
    '68656c6c6f',
    'Message bytes are returned unchanged'
);

select is(
    (select created_at from public.fetch_messages_since()
     where id = '70000000-0000-4000-8000-000000000101'),
    '2020-01-01T10:00:00.123456Z'::timestamptz,
    'Server timestamp retains microsecond precision'
);

-- 15-19: Caller isolation and authentication.
set local request.jwt.claim.sub = '70000000-0000-4000-8000-000000000002';

select results_eq(
    $$select id from public.fetch_messages_since()$$,
    array['70000000-0000-4000-8000-000000000101'::uuid, '70000000-0000-4000-8000-000000000105'::uuid, '70000000-0000-4000-8000-000000000103'::uuid],
    'Bob sees only messages from his conversations'
);

set local request.jwt.claim.sub = '70000000-0000-4000-8000-000000000003';

select results_eq(
    $$select id from public.fetch_messages_since()$$,
    array['70000000-0000-4000-8000-000000000102'::uuid, '70000000-0000-4000-8000-000000000105'::uuid, '70000000-0000-4000-8000-000000000104'::uuid],
    'Carol sees only messages from her conversations'
);

set local request.jwt.claim.sub = '70000000-0000-4000-8000-000000000004';

select is_empty(
    $$select id from public.fetch_messages_since()$$,
    'User without conversations receives no messages'
);

set local request.jwt.claim.sub = '';

select throws_ok(
    $$select * from public.fetch_messages_since()$$,
    '42501', null,
    'Missing authenticated identity is rejected'
);

reset role;
set local role anon;

select throws_ok(
    $$select * from public.fetch_messages_since()$$,
    '42501', null,
    'Anonymous role cannot execute catch-up'
);

-- 20-27: Input validation.
reset role;
set local role authenticated;
set local request.jwt.claim.sub = '70000000-0000-4000-8000-000000000001';

select throws_ok(
    $$select * from public.fetch_messages_since('2020-01-01T10:00:00.123456Z', null, 100)$$,
    '22023', null,
    'Timestamp without cursor UUID is rejected'
);

select throws_ok(
    $$select * from public.fetch_messages_since(null, '70000000-0000-4000-8000-000000000101', 100)$$,
    '22023', null,
    'Cursor UUID without timestamp is rejected'
);

select throws_ok(
    $$select * from public.fetch_messages_since(null, null, 0)$$,
    '22023', null,
    'Zero page size is rejected'
);

select throws_ok(
    $$select * from public.fetch_messages_since(null, null, -1)$$,
    '22023', null,
    'Negative page size is rejected'
);

select throws_ok(
    $$select * from public.fetch_messages_since(null, null, 201)$$,
    '22023', null,
    'Page size above the maximum is rejected'
);

select throws_ok(
    $$select * from public.fetch_messages_since(null, null, null)$$,
    '22023', null,
    'Explicit null page size is rejected'
);

select throws_ok(
    $$select * from public.fetch_messages_since('infinity', '70000000-0000-4000-8000-000000000101', 100)$$,
    '22023', null,
    'Positive infinite cursor timestamp is rejected'
);

select throws_ok(
    $$select * from public.fetch_messages_since('-infinity', '70000000-0000-4000-8000-000000000101', 100)$$,
    '22023', null,
    'Negative infinite cursor timestamp is rejected'
);

-- 28: Current membership, not past membership, controls access.
reset role;

delete from public.conversation_members
where conversation_id = '70000000-0000-4000-8000-000000000010' and user_id = '70000000-0000-4000-8000-000000000002';

set local role authenticated;
set local request.jwt.claim.sub = '70000000-0000-4000-8000-000000000002';

select results_eq(
    $$select id from public.fetch_messages_since()$$,
    array['70000000-0000-4000-8000-000000000105'::uuid],
    'Removed member can no longer fetch that conversation'
);

-- 29: Deleting the cursor row does not break the next page.
reset role;
delete from public.messages where id = '70000000-0000-4000-8000-000000000102';

set local role authenticated;
set local request.jwt.claim.sub = '70000000-0000-4000-8000-000000000001';

select results_eq(
    $$select id from public.fetch_messages_since(
        '2020-01-01T10:00:00.123456Z', '70000000-0000-4000-8000-000000000102', 200
    )$$,
    array['70000000-0000-4000-8000-000000000103'::uuid, '70000000-0000-4000-8000-000000000104'::uuid],
    'Next page still works after the cursor row is deleted'
);

-- 30: Verify the default limit with more than 100 visible rows.
reset role;

insert into public.messages (
    id, conversation_id, sender_id, payload, created_at
)
select
    ('70000000-0000-4000-8000-' || lpad((1000 + n)::text, 12, '0'))::uuid,
    '70000000-0000-4000-8000-000000000020'::uuid,
    '70000000-0000-4000-8000-000000000001'::uuid,
    decode('78', 'hex'),
    '2020-01-01T10:00:03Z'::timestamptz + n * interval '1 second'
from generate_series(1, 105) as series(n);

set local role authenticated;
set local request.jwt.claim.sub = '70000000-0000-4000-8000-000000000001';

select is(
    (select count(*) from public.fetch_messages_since()),
    100::bigint,
    'Omitted page size is bounded to 100 rows'
);

reset role;
select * from finish();
rollback;
