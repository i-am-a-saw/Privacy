create table public.profiles (
    id uuid primary key references auth.users(id) on delete cascade,
    username text not null
        constraint profiles_username_format
        check (username ~ '^[a-z0-9_]{3,20}$'),
    display_name text,
    avatar_path text
);

create unique index profiles_username_unique
    on public.profiles (username);

alter table public.profiles enable row level security;

revoke all on table public.profiles from anon, authenticated;
grant select, insert, update on table public.profiles to authenticated;

create policy profiles_select_own
on public.profiles for select to authenticated
using ((select auth.uid()) = id);

create policy profiles_insert_own
on public.profiles for insert to authenticated
with check ((select auth.uid()) = id);

create policy profiles_update_own
on public.profiles for update to authenticated
using ((select auth.uid()) = id)
with check ((select auth.uid()) = id);

create function public.find_profile_by_username(lookup_username text)
returns table (id uuid, username text, display_name text)
language sql
stable
security definer
set search_path = ''
as $$
    select p.id, p.username, p.display_name
    from public.profiles as p
    where p.username = lookup_username
        and (select auth.uid()) is not null
$$;

revoke execute on function public.find_profile_by_username(text)
from anon, public;

grant execute on function public.find_profile_by_username(text)
to authenticated;
