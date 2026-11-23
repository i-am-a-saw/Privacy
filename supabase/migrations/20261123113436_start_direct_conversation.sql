create function public.get_or_create_direct_conversation(other_user uuid)
returns uuid
language plpgsql
volatile
security definer
set search_path = ''
as $$
declare
    v_caller_id uuid := auth.uid();
    v_direct_key text;
    v_conversation_id uuid;
    v_created_by uuid;
begin
    if v_caller_id is null then
        raise exception using
            errcode = '42501',
            message = 'Authentication required';
    end if;

    if other_user is null then
        raise exception using
            errcode = '22023',
            message = 'Other user is required';
    end if;

    if other_user = v_caller_id then
        raise exception using
            errcode = '22023',
            message = 'Cannot start a direct conversation with yourself';
    end if;

    if not exists (
        select 1 from public.profiles where id = v_caller_id
    ) then
        raise exception using
            errcode = '42501',
            message = 'Create your profile before starting a conversation';
    end if;

    if not exists (
        select 1 from public.profiles where id = other_user
    ) then
        raise exception using
            errcode = '22023',
            message = 'Other user profile does not exist';
    end if;

    v_direct_key :=
        least(v_caller_id, other_user)::text || ':' ||
        greatest(v_caller_id, other_user)::text;

    insert into public.conversations as c (
        type, direct_key, created_by
    )
    values ('direct', v_direct_key, v_caller_id)
    on conflict on constraint conversations_direct_key_unique
    do update set direct_key = excluded.direct_key
    returning c.id, c.created_by
        into v_conversation_id, v_created_by;

    insert into public.conversation_members (
        conversation_id, user_id, role
    )
    values
        (
            v_conversation_id,
            v_caller_id,
            case when v_caller_id = v_created_by
                then 'admin' else 'member' end
        ),
        (
            v_conversation_id,
            other_user,
            case when other_user = v_created_by
                then 'admin' else 'member' end
        )
    on conflict (conversation_id, user_id) do nothing;

    return v_conversation_id;
end;
$$;

revoke execute
on function public.get_or_create_direct_conversation(uuid)
from public, anon;

grant execute
on function public.get_or_create_direct_conversation(uuid)
to authenticated;

notify pgrst, 'reload schema';
