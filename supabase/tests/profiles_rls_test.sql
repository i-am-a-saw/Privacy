begin;

create extension if not exists pgtap with schema extensions;
select plan(2);

insert into auth.users (id, email) values
    ('00000000-0000-4000-8000-000000000011', 'profile-test-a@example.invalid'),
    ('00000000-0000-4000-8000-000000000022', 'profile-test-b@example.invalid');

insert into public.profiles (id, username, display_name) values
    ('00000000-0000-4000-8000-000000000011', 'tap_1102_a', 'Test A'),
    ('00000000-0000-4000-8000-000000000022', 'tap_1102_b', 'Test B');

set local role authenticated;
set local request.jwt.claim.sub = '00000000-0000-4000-8000-000000000011';

select results_eq(
    $$select username from public.profiles
        where username in ('tap_1102_a', 'tap_1102_b')
        order by username$$,
    array['tap_1102_a'::text],
    'user A reads only their profile'
);

select is_empty(
    $$update public.profiles
        set display_name = 'Changed'
        where username = 'tap_1102_b'
        returning username$$,
    'user A cannot update user B'
);

select * from finish();
rollback;
