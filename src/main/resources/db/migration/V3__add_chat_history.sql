CREATE TABLE public.chat_conversations (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id uuid NOT NULL,
    title character varying(255) NOT NULL,
    created_at timestamp(6) with time zone,
    updated_at timestamp(6) with time zone,
    CONSTRAINT fk_chat_conversations_user FOREIGN KEY (user_id)
        REFERENCES public.users(id) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE INDEX idx_chat_conversations_user_updated
    ON public.chat_conversations(user_id, updated_at DESC);

CREATE TABLE public.chat_messages (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    conversation_id uuid NOT NULL,
    role character varying(20) NOT NULL,
    content TEXT NOT NULL,
    sources TEXT,
    status character varying(20),
    model character varying(255),
    created_at timestamp(6) with time zone,
    CONSTRAINT chat_messages_role_check CHECK (role::text = ANY (ARRAY['USER','ASSISTANT']::text[])),
    CONSTRAINT fk_chat_messages_conversation FOREIGN KEY (conversation_id)
        REFERENCES public.chat_conversations(id) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE INDEX idx_chat_messages_conversation_created
    ON public.chat_messages(conversation_id, created_at);
