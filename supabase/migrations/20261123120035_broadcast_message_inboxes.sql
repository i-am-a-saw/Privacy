create function private.broadcast_message_to_inboxes()
returns trigger
language plpgsql
security definer
set search_path = ''
as $$
declare
    v_recipient_id uuid;
begin
    for v_recipient_id in
        select cm.user_id
        from public.conversation_members as cm
        where cm.conversation_id = new.conversation_id
            and cm.user_id <> new.sender_id
    loop
        perform realtime.send(
            jsonb_build_object(
                'id', new.id,
                'conversation_id', new.conversation_id
            ),
            'message_created',
            'user:' || v_recipient_id::text,
            true
        );
    end loop;

    return new;
end;
$$;

revoke execute on function private.broadcast_message_to_inboxes()
from public, anon, authenticated;

create trigger messages_broadcast_inbox
after insert on public.messages
for each row
execute function private.broadcast_message_to_inboxes();

create policy inbox_receive_own
on realtime.messages
for select
to authenticated
using (
    extension = 'broadcast'
    and topic = (select realtime.topic())
    and topic = 'user:' || (select auth.uid())::text
);
