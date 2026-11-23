create index conversation_members_user_conversation_idx
    on public.conversation_members (user_id, conversation_id);
