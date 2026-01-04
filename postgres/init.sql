create extension if not exists "uuid-ossp";

create table if not exists notifications (
    id uuid primary key default uuid_generate_v4(),
    event_id uuid not null,
    recipient_user_id uuid not null,
    entity_type varchar(50) not null,
    entity_id uuid not null,
    notification_type varchar(50) not null,
    title varchar not null,
    message text not null,
    created_at timestamptz not null default now(),
    is_read boolean not null default false,

    constraint unique_notifications_event_id_recipient_id unique (event_id, recipient_user_id)
);

create index if not exists idx_notifications_recipient_id_created_at on notifications(recipient_user_id, created_at);
create index if not exists idx_notifications_recipient_id_is_read on notifications(recipient_user_id, is_read);

create table if not exists email_outbox (
    id uuid primary key default uuid_generate_v4(),
    notification_id uuid not null references notifications(id) on delete cascade,
    event_id uuid not null,
    recipient_user_id uuid not null,
    to_email varchar not null,
    subject varchar not null,
    body text not null,
    status varchar(20) not null,
    attempts integer not null default 0,
    next_retry_at timestamptz,
    last_error text,
    created_at timestamptz not null default now(),
    sent_at timestamptz,

    constraint unique_notifications_event_id_recipient_id_to_email unique (event_id, recipient_user_id, to_email)
);

create index if not exists idx_email_outbox_status_next_retry_at on email_outbox(status, next_retry_at);