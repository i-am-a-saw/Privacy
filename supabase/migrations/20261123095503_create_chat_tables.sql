create table public.conversations (
    id uuid primary key default gen_random_uuid(),
    type text not null,
    direct_key text,
    created_by uuid not null references public.profiles(id),
    created_at timestamptz not null default now(),

    constraint conversations_type_check
        check (type in ('direct', 'group')),

    constraint conversations_direct_key_check
        check (
            (
                type = 'direct'
                and direct_key is not null
                and direct_key <> ''
            )
            or (
                type = 'group'
                and direct_key is null
            )
        ),

    constraint conversations_direct_key_unique
        unique (direct_key)
);

create table public.conversation_members (
    conversation_id uuid not null
        references public.conversations(id) on delete cascade,
    user_id uuid not null
        references public.profiles(id),
    role text not null default 'member',
    joined_at timestamptz not null default now(),

    primary key (conversation_id, user_id),

    constraint conversation_member_role_check
        check (role in ('member', 'admin'))
);

create table public.messages (
    id uuid primary key,
    conversation_id uuid not null
        references public.conversations(id) on delete cascade,
    sender_id uuid not null
        references public.profiles(id),
    payload bytea not null,
    cipher_suite text not null default 'none-v0',
    created_at timestamptz not null default now(),

    constraint messages_payload_not_empty
        check (octet_length(payload) > 0),

    constraint messages_cipher_suite_not_empty
        check (cipher_suite <> '')
);

create index messages_conversation_created_id_idx
    on public.messages (conversation_id, created_at, id);

create index messages_created_id_idx
    on public.messages (created_at, id);

alter table public.conversations enable row level security;
alter table public.conversation_members enable row level security;
alter table public.messages enable row level security;

revoke all on table
    public.conversations,
    public.conversation_members,
    public.messages
from public, anon, authenticated;

grant select on table
    public.conversations,
    public.conversation_members,
    public.messages
to authenticated;

grant insert (
    id,
    conversation_id,
    sender_id,
    payload,
    cipher_suite
)
on public.messages
to authenticated;

grant all on table
    public.conversations,
    public.conversation_members,
    public.messages
to service_role;

create schema if not exists private;

revoke all on schema private from public, anon;

grant usage on schema private
to authenticated, service_role;

create function private.is_member(target_conversation_id uuid)
returns boolean
language sql
stable
security definer
set search_path = ''
as $$
    select exists (
        select 1
        from public.conversation_members as cm
        where cm.conversation_id = target_conversation_id
            and cm.user_id = (select auth.uid())
    );
$$;

revoke execute on function private.is_member(uuid)
from public, anon;

grant execute on function private.is_member(uuid)
to authenticated, service_role;

create policy conversations_select_member
on public.conversations
for select
to authenticated
using (private.is_member(id));

create policy conversation_members_select_member
on public.conversation_members
for select
to authenticated
using (private.is_member(conversation_id));

create policy messages_select_member
on public.messages
for select
to authenticated
using (private.is_member(conversation_id));

create policy messages_insert_member
on public.messages
for insert
to authenticated
with check (
    sender_id = (select auth.uid())
    and private.is_member(conversation_id)
);

create policy profiles_select_conversation_peers
on public.profiles
for select
to authenticated
using (
    exists (
        select 1
        from public.conversation_members as peer
        where peer.user_id = profiles.id
            and private.is_member(peer.conversation_id)
    )
);
