-- Token and tool-call counts of an assistant answer, as JSON from the processor.
ALTER TABLE public.chat_messages ADD COLUMN usage TEXT;

CREATE INDEX idx_chat_messages_role_created
    ON public.chat_messages(role, created_at);
