create function public.fetch_messages_since(
    after_created_at timestamptz default null,
    after_id uuid default null,
    page_size integer default 100
)
returns setof public.messages
language plpgsql
stable
security invoker
set search_path = ''
as $$
begin
    if auth.uid() is null then
        raise exception using
            errcode = '42501',
            message = 'Authentication required';
    end if;

    if (after_created_at is null) <> (after_id is null) then
        raise exception using
            errcode = '22023',
            message = 'Provide both cursor fields or neither';
    end if;

    if page_size is null or page_size < 1 or page_size > 200 then
        raise exception using
            errcode = '22023',
            message = 'Page size must be between 1 and 200';
    end if;

    if after_created_at is not null and not isfinite(after_created_at) then
        raise exception using
            errcode = '22023',
            message = 'Cursor timestamp must be finite';
    end if;

    if after_created_at is null then
        return query
        select m.*
        from public.messages as m
        order by m.created_at, m.id
        limit page_size;
    else
        return query
        select m.*
        from public.messages as m
        where (m.created_at, m.id) > (after_created_at, after_id)
        order by m.created_at, m.id
        limit page_size;
    end if;
end;
$$;

revoke execute
on function public.fetch_messages_since(timestamptz, uuid, integer)
from public, anon;

grant execute
on function public.fetch_messages_since(timestamptz, uuid, integer)
to authenticated;

notify pgrst, 'reload schema';
