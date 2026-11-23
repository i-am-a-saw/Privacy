begin;

create extension if not exists pgtap with schema extensions;
set local search_path = public, extensions;

select plan(30);

insert into auth.users (id, email) values
    ('50000000-0000-4000-8000-000000000001', 'rpc-alice@example.invalid'),
    ('50000000-0000-4000-8000-000000000002', 'rpc-bob@example.invalid'),
    ('50000000-0000-4000-8000-000000000003', 'rpc-carol@example.invalid'),
    ('50000000-0000-4000-8000-000000000004', 'rpc-no-profile@example.invalid'),
    ('50000000-0000-4000-8000-000000000006', 'rpc-atomic@example.invalid');

insert into public.profiles (id, username) values
    ('50000000-0000-4000-8000-000000000001', 'rpc_m2_alice'),
    ('50000000-0000-4000-8000-000000000002', 'rpc_m2_bob'),
    ('50000000-0000-4000-8000-000000000003', 'rpc_m2_carol'),
    ('50000000-0000-4000-8000-000000000006', 'rpc_m2_atomic');

-- 1-2: Public contract.
select has_function(
    'public', 'get_or_create_direct_conversation', array['uuid'],
    'RPC accepts one UUID'
);

select is(
    pg_get_function_result(
        'public.get_or_create_direct_conversation(uuid)'::regprocedure
    ),
    'uuid',
    'RPC returns one UUID'
);

-- 3-6: Reject invalid callers and targets.
set local role authenticated;
set local request.jwt.claim.sub = '';

select throws_ok(
    $$select public.get_or_create_direct_conversation('50000000-0000-4000-8000-000000000002')$$,
    '42501', null,
    'Missing caller identity is rejected'
);

set local request.jwt.claim.sub = '50000000-0000-4000-8000-000000000001';

select throws_ok(
    $$select public.get_or_create_direct_conversation(null)$$,
    '22023', null,
    'Null target is rejected'
);

select throws_ok(
    $$select public.get_or_create_direct_conversation('50000000-0000-4000-8000-000000000001')$$,
    '22023', null,
    'Self conversation is rejected'
);

select throws_ok(
    $$select public.get_or_create_direct_conversation('50000000-0000-4000-8000-000000000005')$$,
    '22023', null,
    'Missing target profile is rejected'
);

-- 7-11: First creation and immediate RLS access.
select lives_ok(
    $$select public.get_or_create_direct_conversation('50000000-0000-4000-8000-000000000002')$$,
    'Alice can start a conversation with Bob'
);

select results_eq(
    $$select direct_key from public.conversations
      where direct_key = '50000000-0000-4000-8000-000000000001:50000000-0000-4000-8000-000000000002'$$,
    array['50000000-0000-4000-8000-000000000001:50000000-0000-4000-8000-000000000002'::text],
    'Conversation uses the canonical pair key'
);

select is(
    (select count(*) from public.conversation_members as m
     join public.conversations as c on c.id = m.conversation_id
     where c.direct_key = '50000000-0000-4000-8000-000000000001:50000000-0000-4000-8000-000000000002'),
    2::bigint,
    'First creation inserts exactly two memberships'
);

select results_eq(
    $$select m.user_id from public.conversation_members as m
      join public.conversations as c on c.id = m.conversation_id
      where c.direct_key = '50000000-0000-4000-8000-000000000001:50000000-0000-4000-8000-000000000002'
      order by m.user_id$$,
    array['50000000-0000-4000-8000-000000000001'::uuid, '50000000-0000-4000-8000-000000000002'::uuid],
    'Memberships belong to Alice and Bob'
);

select is(
    private.is_member(
        (select id from public.conversations
         where direct_key = '50000000-0000-4000-8000-000000000001:50000000-0000-4000-8000-000000000002')
    ),
    true,
    'New conversation is immediately accessible through RLS'
);

-- Make timestamp preservation observable inside one transaction.
reset role;
update public.conversations
set created_at = '2000-01-01T00:00:00Z'
where direct_key = '50000000-0000-4000-8000-000000000001:50000000-0000-4000-8000-000000000002';

set local role authenticated;
set local request.jwt.claim.sub = '50000000-0000-4000-8000-000000000001';

-- 12-14: Repeated request from the same caller.
select is(
    public.get_or_create_direct_conversation('50000000-0000-4000-8000-000000000002'),
    (select id from public.conversations
     where direct_key = '50000000-0000-4000-8000-000000000001:50000000-0000-4000-8000-000000000002'),
    'Repeated request returns the original UUID'
);

select is(
    (select count(*) from public.conversations
     where direct_key = '50000000-0000-4000-8000-000000000001:50000000-0000-4000-8000-000000000002'),
    1::bigint,
    'Repeated request does not duplicate the conversation'
);

select is(
    (select created_at from public.conversations
     where direct_key = '50000000-0000-4000-8000-000000000001:50000000-0000-4000-8000-000000000002'),
    '2000-01-01T00:00:00Z'::timestamptz,
    'Repeated request preserves creation time'
);

-- 15-19: Reverse request from Bob.
set local request.jwt.claim.sub = '50000000-0000-4000-8000-000000000002';

select is(
    public.get_or_create_direct_conversation('50000000-0000-4000-8000-000000000001'),
    (select id from public.conversations
     where direct_key = '50000000-0000-4000-8000-000000000001:50000000-0000-4000-8000-000000000002'),
    'Reverse request returns the same UUID'
);

select is(
    (select count(*) from public.conversation_members as m
     join public.conversations as c on c.id = m.conversation_id
     where c.direct_key = '50000000-0000-4000-8000-000000000001:50000000-0000-4000-8000-000000000002'),
    2::bigint,
    'Reverse request does not duplicate memberships'
);

select results_eq(
    $$select m.role from public.conversation_members as m
      join public.conversations as c on c.id = m.conversation_id
      where c.direct_key = '50000000-0000-4000-8000-000000000001:50000000-0000-4000-8000-000000000002'
      order by m.user_id$$,
    array['admin'::text, 'member'::text],
    'Original creator remains admin'
);

select is(
    (select created_by from public.conversations
     where direct_key = '50000000-0000-4000-8000-000000000001:50000000-0000-4000-8000-000000000002'),
    '50000000-0000-4000-8000-000000000001'::uuid,
    'Reverse request preserves the original creator'
);

select is(
    (select created_at from public.conversations
     where direct_key = '50000000-0000-4000-8000-000000000001:50000000-0000-4000-8000-000000000002'),
    '2000-01-01T00:00:00Z'::timestamptz,
    'Reverse request preserves creation time'
);

-- 20-22: Another pair does not reuse or expose Alice-Bob.
set local request.jwt.claim.sub = '50000000-0000-4000-8000-000000000003';

select lives_ok(
    $$select public.get_or_create_direct_conversation('50000000-0000-4000-8000-000000000002')$$,
    'Carol can start a separate conversation with Bob'
);

select results_eq(
    $$select direct_key from public.conversations
      where direct_key in (
          '50000000-0000-4000-8000-000000000001:50000000-0000-4000-8000-000000000002',
          '50000000-0000-4000-8000-000000000002:50000000-0000-4000-8000-000000000003'
      ) order by direct_key$$,
    array['50000000-0000-4000-8000-000000000002:50000000-0000-4000-8000-000000000003'::text],
    'Carol sees Carol-Bob but cannot see Alice-Bob'
);

reset role;

select is(
    (select count(*) from public.conversation_members as m
     join public.conversations as c on c.id = m.conversation_id
     where c.direct_key = '50000000-0000-4000-8000-000000000001:50000000-0000-4000-8000-000000000002'),
    2::bigint,
    'Creating another pair leaves Alice-Bob membership unchanged'
);

-- 23-24: Profile requirement and anonymous permission.
set local role authenticated;
set local request.jwt.claim.sub = '50000000-0000-4000-8000-000000000004';

select throws_ok(
    $$select public.get_or_create_direct_conversation('50000000-0000-4000-8000-000000000002')$$,
    '42501', null,
    'Caller without a profile is rejected'
);

reset role;
set local role anon;
set local request.jwt.claim.sub = '';

select throws_ok(
    $$select public.get_or_create_direct_conversation('50000000-0000-4000-8000-000000000002')$$,
    '42501', null,
    'Anonymous role cannot execute the RPC'
);

-- 25-30: Force a membership failure and verify full rollback.
reset role;

alter table public.conversation_members
add constraint direct_rpc_test_reject_atomic
check (user_id <> '50000000-0000-4000-8000-000000000006'::uuid);

set local role authenticated;
set local request.jwt.claim.sub = '50000000-0000-4000-8000-000000000001';

select throws_ok(
    $$select public.get_or_create_direct_conversation('50000000-0000-4000-8000-000000000006')$$,
    '23514', null,
    'Membership failure makes the RPC fail'
);

-- Inspect as administrator: RLS must not hide an orphan.
reset role;

select is_empty(
    $$select id from public.conversations
      where direct_key = '50000000-0000-4000-8000-000000000001:50000000-0000-4000-8000-000000000006'$$,
    'Failed RPC leaves no conversation'
);

select is_empty(
    $$select conversation_id from public.conversation_members
      where user_id = '50000000-0000-4000-8000-000000000006'$$,
    'Failed RPC leaves no target membership'
);

alter table public.conversation_members
drop constraint direct_rpc_test_reject_atomic;

set local role authenticated;
set local request.jwt.claim.sub = '50000000-0000-4000-8000-000000000001';

select lives_ok(
    $$select public.get_or_create_direct_conversation('50000000-0000-4000-8000-000000000006')$$,
    'The same pair succeeds after the forced failure is removed'
);

select is(
    (select count(*) from public.conversations
     where direct_key = '50000000-0000-4000-8000-000000000001:50000000-0000-4000-8000-000000000006'),
    1::bigint,
    'Successful retry creates exactly one conversation'
);

select is(
    (select count(*) from public.conversation_members as m
     join public.conversations as c on c.id = m.conversation_id
     where c.direct_key = '50000000-0000-4000-8000-000000000001:50000000-0000-4000-8000-000000000006'),
    2::bigint,
    'Successful retry creates both memberships'
);

reset role;
select * from finish();
rollback;
